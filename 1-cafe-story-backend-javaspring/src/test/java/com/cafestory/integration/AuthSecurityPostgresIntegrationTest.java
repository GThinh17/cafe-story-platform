package com.cafestory.integration;

import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.until.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSecurityPostgresIntegrationTest extends PostgresIntegrationTestSupport {

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

    @Test
    void registerMeAndRefresh_success_usesRealSecurityServiceRepositoryAndPostgres() throws Exception {
        MvcResult registerResult = register("ituser01", "ituser01@example.com")
                .andExpect(status().isCreated())
                .andExpect(cookie().exists(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(jsonPath("$.data.user.userName").value("ituser01"))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();

        Cookie accessTokenCookie = requiredCookie(registerResult, JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE);
        Cookie refreshTokenCookie = requiredCookie(registerResult, "refresh_token");

        mockMvc.perform(get("/api/auth/me").cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.userName").value("ituser01"))
                .andExpect(jsonPath("$.data.user.roles[0]").value(UserRole.USER.name()));

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE))
                .andExpect(jsonPath("$.data.accessToken").isString());

        assertThat(userRepository.findByUserName("ituser01")).isPresent();
    }

    @Test
    void adminEndpoint_fail_userRoleCannotAccessRealSecurityChain() throws Exception {
        MvcResult registerResult = register("ituser02", "ituser02@example.com")
                .andExpect(status().isCreated())
                .andReturn();

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .cookie(requiredCookie(registerResult, JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_success_adminRoleCanAccessRealServiceAndPostgres() throws Exception {
        register("itadmin01", "itadmin01@example.com")
                .andExpect(status().isCreated());
        grantRole("itadmin01", UserRole.ADMIN);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "itadmin01@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.roles").isArray())
                .andReturn();

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .cookie(requiredCookie(loginResult, JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(1))
                .andExpect(jsonPath("$.data.activeUsers").value(1));
    }

    private org.springframework.test.web.servlet.ResultActions register(String username, String email) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterPayload(
                        username,
                        "Integration Test User",
                        "123456",
                        email))));
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
}
