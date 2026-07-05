package com.kaoshi.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void loginReturnsToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.at("/data/accessToken").asText()).isNotBlank();
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"bad-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void meRequiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void meReturnsCurrentUserWithToken() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    void emailCodeRegistrationCreatesApprovedStudentAccount() throws Exception {
        configureRegistration(false);
        String email = "student" + System.nanoTime() + "@example.com";
        String username = "stu" + System.nanoTime();
        String codeResponse = mockMvc.perform(post("/api/auth/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","purpose":"REGISTER"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.debugCode").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String code = objectMapper.readTree(codeResponse).at("/data/debugCode").asText();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "username": "%s",
                                  "displayName": "新考生",
                                  "password": "abcdef",
                                  "confirmPassword": "abcdef",
                                  "verificationCode": "%s"
                                }
                                """.formatted(email, username, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"abcdef"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value(username));
    }

    @Test
    void passwordResetCodeChangesPassword() throws Exception {
        configureRegistration(false);
        String email = "reset" + System.nanoTime() + "@example.com";
        String username = "reset" + System.nanoTime();
        String registerCode = sendCode(email, "REGISTER");
        registerApprovedUser(email, username, registerCode, "abcdef");
        String resetCode = sendCode(email, "RESET_PASSWORD");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "%s",
                                  "newPassword": "newpass1",
                                  "confirmPassword": "newpass1"
                                }
                                """.formatted(email, resetCode)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"newpass1"}
                                """.formatted(username)))
                .andExpect(status().isOk());
    }

    @Test
    void adminApprovalRequiredBlocksLoginUntilApproved() throws Exception {
        configureRegistration(true);
        String token = adminToken();

        String email = "pending" + System.nanoTime() + "@example.com";
        String username = "pending" + System.nanoTime();
        String code = sendCode(email, "REGISTER");
        registerApprovedUser(email, username, code, "abcdef")
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"abcdef"}
                                """.formatted(username)))
                .andExpect(status().isUnauthorized());

        String requests = mockMvc.perform(get("/api/admin/auth/registration-requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long userId = objectMapper.readTree(requests).at("/data/0/userId").asLong();

        mockMvc.perform(post("/api/admin/auth/registration-requests/{userId}/approve", userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"abcdef"}
                                """.formatted(username)))
                .andExpect(status().isOk());
        configureRegistration(false);
    }

    private String adminToken() throws Exception {
        String login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"password"}
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(login).at("/data/accessToken").asText();
    }

    private String sendCode(String email, String purpose) throws Exception {
        String response = mockMvc.perform(post("/api/auth/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","purpose":"%s"}
                                """.formatted(email, purpose)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/debugCode").asText();
    }

    private void configureRegistration(boolean adminApprovalRequired) throws Exception {
        mockMvc.perform(put("/api/admin/auth/registration-settings")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "selfRegistrationEnabled": true,
                                  "emailVerificationRequired": true,
                                  "adminApprovalRequired": %s,
                                  "defaultRoleCode": "STUDENT",
                                  "defaultDepartmentId": 2,
                                  "allowedEmailDomains": [],
                                  "termsText": ""
                                }
                                """.formatted(adminApprovalRequired)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions registerApprovedUser(String email, String username, String code, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "username": "%s",
                          "displayName": "测试考生",
                          "password": "%s",
                          "confirmPassword": "%s",
                          "verificationCode": "%s"
                        }
                        """.formatted(email, username, password, password, code)))
                .andExpect(status().isOk());
    }
}

