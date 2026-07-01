package com.kaoshi.admin;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminManagementControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminApisRequireLogin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void userManagementSupportsPageCreateUpdateAndStatus() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/admin/users?page=1&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].username").value("admin"))
                .andExpect(jsonPath("$.data.total").value(1));

        String createResponse = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "teacher",
                                  "displayName": "考务老师",
                                  "password": "password",
                                  "roleIds": [2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("teacher"))
                .andExpect(jsonPath("$.data.roles[0]").value("EXAM_MANAGER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = objectMapper.readTree(createResponse).at("/data/id").asLong();

        mockMvc.perform(put("/api/admin/users/{id}", userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "考务主管",
                                  "password": "new-password",
                                  "roleIds": [2, 3]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("考务主管"))
                .andExpect(jsonPath("$.data.roles.length()").value(2));

        mockMvc.perform(patch("/api/admin/users/{id}/status", userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DISABLED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    void roleManagementSupportsCreateUpdatePermissionsAndMenus() throws Exception {
        String token = adminToken();

        String createResponse = mockMvc.perform(post("/api/admin/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "AUDITOR",
                                  "name": "审计员",
                                  "description": "查看权限和菜单",
                                  "permissionIds": [1, 6],
                                  "menuIds": [1, 4, 5]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("AUDITOR"))
                .andExpect(jsonPath("$.data.permissions.length()").value(2))
                .andExpect(jsonPath("$.data.permissions[0].name").value("系统管理"))
                .andExpect(jsonPath("$.data.menus.length()").value(3))
                .andExpect(jsonPath("$.data.menus[0].title").value("工作台"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long roleId = objectMapper.readTree(createResponse).at("/data/id").asLong();

        mockMvc.perform(put("/api/admin/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "AUDITOR",
                                  "name": "审计管理员",
                                  "description": "查看权限、菜单和成绩",
                                  "permissionIds": [1, 6],
                                  "menuIds": [1, 2, 4, 5]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("审计管理员"))
                .andExpect(jsonPath("$.data.menus.length()").value(4));

        mockMvc.perform(get("/api/admin/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4));
    }

    @Test
    void permissionAndMenuListsAreAvailableForAdmin() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/admin/permissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("system:admin"));

        mockMvc.perform(get("/api/admin/menus")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].title").value("用户管理"));
    }

    private String adminToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        String token = root.at("/data/accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}

