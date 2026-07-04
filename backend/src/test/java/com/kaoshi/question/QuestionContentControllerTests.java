package com.kaoshi.question;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class QuestionContentControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contentTreeSupportsNodeCrudSharedAnswersGroupImportAndPackageRoundTrip() throws Exception {
        String token = adminToken();
        long bankId = createBank(token, "结构化题库" + System.currentTimeMillis());

        long sectionId = createSection(token, bankId);
        long groupId = createGroup(token, bankId, sectionId);
        createWordBankQuestion(token, bankId, groupId);

        mockMvc.perform(get("/api/admin/question-banks/{bankId}/content-tree", bankId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[0].nodeCode").value("reading"))
                .andExpect(jsonPath("$.data.sections[0].children[0].nodeCode").value("reading-word-bank"))
                .andExpect(jsonPath("$.data.sections[0].children[0].sharedOptions.length()").value(4))
                .andExpect(jsonPath("$.data.sections[0].children[0].questions[0].options[1].correct").value(true))
                .andExpect(jsonPath("$.data.sections[0].children[0].attachments[0].fileUrl").value("/assets/group.mp3"));

        MockMultipartFile groupExcel = new MockMultipartFile(
                "file",
                "group-items.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                groupWorkbook()
        );
        mockMvc.perform(multipart("/api/admin/question-banks/nodes/{nodeId}/questions/import", groupId)
                        .file(groupExcel)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        byte[] exported = mockMvc.perform(get("/api/admin/question-banks/{bankId}/package", bankId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).contains("application/zip"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        MockMultipartFile packageFile = new MockMultipartFile(
                "file",
                "question-bank.zip",
                "application/zip",
                exported
        );
        String importResponse = mockMvc.perform(multipart("/api/admin/question-banks/package/import")
                        .file(packageFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankName").value(startsWith("结构化题库")))
                .andExpect(jsonPath("$.data.nodeCount").value(2))
                .andExpect(jsonPath("$.data.questionCount").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long importedBankId = objectMapper.readTree(importResponse).at("/data/bankId").asLong();

        mockMvc.perform(get("/api/admin/question-banks/{bankId}/content-tree", importedBankId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[0].children[0].sharedOptions.length()").value(4))
                .andExpect(jsonPath("$.data.sections[0].children[0].questions.length()").value(2))
                .andExpect(jsonPath("$.data.sections[0].children[0].questions[0].options[1].correct").value(true));

        mockMvc.perform(delete("/api/admin/question-banks/nodes/{nodeId}", groupId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private long createBank(String token, String name) throws Exception {
        String response = mockMvc.perform(post("/api/admin/question-banks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "%s",
                                  "description": "内容树测试",
                                  "status": "ACTIVE"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    private long createSection(String token, long bankId) throws Exception {
        String response = mockMvc.perform(post("/api/admin/question-banks/{bankId}/nodes", bankId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": null,
                                  "nodeCode": "reading",
                                  "nodeType": "SECTION",
                                  "title": "阅读理解",
                                  "direction": "Read the following passages.",
                                  "material": "",
                                  "sortOrder": 10,
                                  "sharedOptions": [],
                                  "attachments": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodeCode").value("reading"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    private long createGroup(String token, long bankId, long sectionId) throws Exception {
        String response = mockMvc.perform(post("/api/admin/question-banks/{bankId}/nodes", bankId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": %d,
                                  "nodeCode": "reading-word-bank",
                                  "nodeType": "GROUP",
                                  "title": "选词填空",
                                  "direction": "Fill in each blank with one word.",
                                  "material": "A passage shared by all blanks.",
                                  "sortOrder": 10,
                                  "sharedOptions": [
                                    {"label": "A", "content": "ability"},
                                    {"label": "B", "content": "benefit"},
                                    {"label": "C", "content": "career"},
                                    {"label": "D", "content": "direct"}
                                  ],
                                  "attachments": [
                                    {"fileName": "group.mp3", "fileUrl": "/assets/group.mp3", "mediaType": "AUDIO"}
                                  ]
                                }
                                """.formatted(sectionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sharedOptions.length()").value(4))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    private void createWordBankQuestion(String token, long bankId, long groupId) throws Exception {
        mockMvc.perform(post("/api/admin/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankId": %d,
                                  "nodeId": %d,
                                  "type": "WORD_BANK",
                                  "stem": "Blank 1",
                                  "itemLabel": "26",
                                  "itemStem": "Blank 1",
                                  "analysis": "The answer is benefit.",
                                  "difficulty": "EASY",
                                  "status": "ACTIVE",
                                  "options": [],
                                  "correctLabels": ["B"],
                                  "attachments": []
                                }
                                """.formatted(bankId, groupId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.options[1].correct").value(true));
    }

    private byte[] groupWorkbook() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("items");
            Row header = sheet.createRow(0);
            String[] headers = {"题号", "题型", "难度", "题干", "选项A", "选项B", "选项C", "选项D", "正确答案", "解析", "状态"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("27");
            row.createCell(1).setCellValue("选词填空");
            row.createCell(2).setCellValue("简单");
            row.createCell(3).setCellValue("Blank 2");
            row.createCell(8).setCellValue("C");
            row.createCell(9).setCellValue("The answer is career.");
            row.createCell(10).setCellValue("启用");
            workbook.write(output);
            return output.toByteArray();
        }
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
