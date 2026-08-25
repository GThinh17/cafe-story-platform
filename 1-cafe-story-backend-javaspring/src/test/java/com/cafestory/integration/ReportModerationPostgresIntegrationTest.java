package com.cafestory.integration;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportModerationJob;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.ReportModerationJobRepository;
import com.cafestory.repository.ReportReasonRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceInterface.ReportModerationService;
import com.cafestory.until.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ReportModerationPostgresIntegrationTest extends PostgresIntegrationTestSupport {

    private static final ModerationWebhookStub WEBHOOK_STUB = ModerationWebhookStub.start();

    static {
        System.setProperty("cafestory.it.reportModerationWebhookUrl", WEBHOOK_STUB.url());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @Autowired
    private ContentReportRepository contentReportRepository;

    @Autowired
    private ReportModerationJobRepository reportModerationJobRepository;

    @Autowired
    private AiModerationResultRepository aiModerationResultRepository;

    @Autowired
    private ReportModerationService reportModerationService;

    @AfterAll
    static void stopWebhookStub() {
        WEBHOOK_STUB.stop();
        System.clearProperty("cafestory.it.reportModerationWebhookUrl");
    }

    @Test
    void reportBlogModeration_success_enqueuesProcessesAndAppearsInAdminQueue() throws Exception {
        Blog blog = publishedBlogBy("reported-author");
        ReportReason reason = activeBlogReason();
        Cookie reporterAccessCookie = registerAndGetAccessCookie("reporter01", "reporter01@example.com");

        mockMvc.perform(post("/api/reports")
                        .cookie(reporterAccessCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContentReportPayload(
                                ReportTargetType.BLOG,
                                blog.getId(),
                                reason.getId(),
                                "Bai viet co noi dung kich dong"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetType").value("BLOG"))
                .andExpect(jsonPath("$.data.targetId").value(blog.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        ContentReport report = contentReportRepository.findAll().get(0);
        ReportModerationJob pendingJob = reportModerationJobRepository.findByContentReportId(report.getId())
                .orElseThrow();
        assertThat(pendingJob.getStatus()).isEqualTo(ReportModerationJobStatus.PENDING);
        assertThat(pendingJob.getTargetId()).isEqualTo(blog.getId());

        int processed = reportModerationService.processDueJobs(1);

        assertThat(processed).isEqualTo(1);
        assertThat(WEBHOOK_STUB.requestCount()).isEqualTo(1);

        ReportModerationJob completedJob = reportModerationJobRepository.findByContentReportId(report.getId())
                .orElseThrow();
        assertThat(completedJob.getStatus()).isEqualTo(ReportModerationJobStatus.SUCCEEDED);
        assertThat(completedJob.getAttemptCount()).isEqualTo(1);
        assertThat(completedJob.getLastError()).isNull();

        ContentReport reviewingReport = contentReportRepository.findById(report.getId()).orElseThrow();
        assertThat(reviewingReport.getStatus()).isEqualTo(ReportStatus.REVIEWING);

        AiModerationResult result = aiModerationResultRepository
                .findTopByContentReportIdOrderByCreatedAtDesc(report.getId())
                .orElseThrow();
        assertThat(result.getDecision()).isEqualTo(ModerationDecision.VIOLATION);
        assertThat(result.getBlog().getId()).isEqualTo(blog.getId());
        assertThat(result.getTags()).containsExactly("hate-speech", "policy-risk");
        assertThat(result.getResolved()).isFalse();

        Cookie adminAccessCookie = registerAndGetAccessCookie("moderation-admin", "moderation-admin@example.com");
        grantRole("moderation-admin", UserRole.ADMIN);
        adminAccessCookie = loginAndGetAccessCookie("moderation-admin@example.com");

        mockMvc.perform(get("/api/admin/moderation/queue")
                        .cookie(adminAccessCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].contentReportId").value(report.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].decision").value("VIOLATION"))
                .andExpect(jsonPath("$.data.content[0].moderationJobStatus").value("SUCCEEDED"));
    }

    private Cookie registerAndGetAccessCookie(String username, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterPayload(
                                username,
                                "Integration Test User",
                                "123456",
                                email))))
                .andExpect(status().isCreated())
                .andReturn();
        return requiredCookie(result, JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE);
    }

    private Cookie loginAndGetAccessCookie(String identifier) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "%s",
                                  "password": "123456"
                                }
                                """.formatted(identifier)))
                .andExpect(status().isOk())
                .andReturn();
        return requiredCookie(result, JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE);
    }

    private Blog publishedBlogBy(String username) {
        User author = new User();
        author.setUserName(username);
        author.setUserFullName("Reported Author");
        author.setUserEmail(username + "@example.com");
        author.setUserPassword("encoded-password");
        author.setAccountStatus(true);
        author = userRepository.save(author);

        Blog blog = new Blog();
        blog.setAuthor(author);
        blog.setContent("Noi dung blog can duoc kiem duyet boi AI moderation");
        blog.setStatus(PostStatus.PUBLISHED);
        return blogRepository.save(blog);
    }

    private ReportReason activeBlogReason() {
        ReportReason reason = new ReportReason();
        reason.setCode("VIOLENT_CONTENT");
        reason.setLabelVi("Noi dung bao luc");
        reason.setTargetType(ReportTargetType.BLOG);
        reason.setSeverity(5);
        reason.setRequiresDescription(false);
        reason.setActive(true);
        reason.setSortOrder(1);
        return reportReasonRepository.save(reason);
    }

    private void grantRole(String username, UserRole roleName) {
        User user = userRepository.findByUserName(username).orElseThrow();
        Role role = roleRepository.findByName(roleName.name()).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName.name());
            return roleRepository.save(newRole);
        });

        if (userRoleAssignmentRepository.existsByUserUserIdAndRoleName(user.getUserId(), roleName.name())) {
            return;
        }

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleAssignmentRepository.save(assignment);
    }

    private Cookie requiredCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertThat(cookie).as(name + " cookie").isNotNull();
        return cookie;
    }

    private record RegisterPayload(String userName, String userFullName, String password, String userEmail) {
    }

    private record ContentReportPayload(
            ReportTargetType targetType,
            java.util.UUID targetId,
            java.util.UUID reasonId,
            String description) {
    }

    private static final class ModerationWebhookStub {

        private final HttpServer server;
        private final AtomicInteger requestCount = new AtomicInteger();

        private ModerationWebhookStub(HttpServer server) {
            this.server = server;
        }

        static ModerationWebhookStub start() {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                ModerationWebhookStub stub = new ModerationWebhookStub(server);
                server.createContext("/report-moderation", stub::handle);
                server.start();
                return stub;
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot start report moderation webhook stub", exception);
            }
        }

        String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/report-moderation";
        }

        int requestCount() {
            return requestCount.get();
        }

        void stop() {
            server.stop(0);
        }

        private void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            assertThat(new String(requestBody, StandardCharsets.UTF_8)).contains("Noi dung blog can duoc kiem duyet");

            byte[] response = """
                    {
                      "decision": "VIOLATION",
                      "score": 88.5,
                      "labels": ["hate-speech", "policy-risk"],
                      "explanation": "Stub moderation response for integration test",
                      "modelName": "cafestory-it-webhook-stub"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        }
    }
}
