package com.cafestory.config;

import com.cafestory.entity.User;
import com.cafestory.until.security.JwtAuthenticationFilter;
import com.cafestory.until.security.JwtService;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void me_fail_withoutAccessToken_TC001() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_fail_userRoleCannotAccess_TC002() throws Exception {
        String accessToken = jwtService.createAccessToken(user(), List.of("USER"));

        mockMvc.perform(get("/api/admin/test")
                        .cookie(new Cookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE, accessToken)))
                .andExpect(status().isForbidden());
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserEmail("luan123@example.com");
        return user;
    }
}
