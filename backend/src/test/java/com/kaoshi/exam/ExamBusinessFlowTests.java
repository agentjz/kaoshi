package com.kaoshi.exam;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ExamBusinessFlowTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void examBusinessFlowCreatesPaperPublishesExamAndScoresSubmission() throws Exception {
        String token = adminToken();

        String bankResponse = mockMvc.perform(post("/api/admin/question-banks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "阅读理解题库",
                                  "description": "用于端到端测试",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("阅读理解题库"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long bankId = objectMapper.readTree(bankResponse).at("/data/id").asLong();

        String questionResponse = mockMvc.perform(post("/api/admin/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankId": %d,
                                  "type": "SINGLE_CHOICE",
                                  "stem": "Which word means fast?",
                                  "analysis": "Quick means fast.",
                                  "difficulty": "EASY",
                                  "status": "ACTIVE",
                                  "options": [
                                    {"label": "A", "content": "slow", "correct": false},
                                    {"label": "B", "content": "quick", "correct": true},
                                    {"label": "C", "content": "late", "correct": false}
                                  ],
                                  "attachments": [
                                    {"fileName": "quick.png", "fileUrl": "/assets/quick.png", "mediaType": "IMAGE"}
                                  ]
                                }
                                """.formatted(bankId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.options.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long questionId = objectMapper.readTree(questionResponse).at("/data/id").asLong();

        String paperResponse = mockMvc.perform(post("/api/admin/papers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "name": "阅读理解测试卷",
                                  "description": "用于端到端测试",
                                  "durationMinutes": 20,
                                  "status": "ACTIVE",
                                  "questions": [
                                    {"questionId": %d, "score": 10}
                                  ]
                                }
                                """.formatted(questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalScore").value(10))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long paperId = objectMapper.readTree(paperResponse).at("/data/id").asLong();

        String examResponse = mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paperId": %d,
                                  "title": "阅读理解模拟考试",
                                  "description": "用于端到端测试",
                                  "qualifyScore": 6,
                                  "startTime": "2026-01-01T00:00:00",
                                  "endTime": "2026-12-31T23:59:59",
                                  "durationMinutes": 20,
                                  "timeLimit": true,
                                  "attemptLimit": null,
                                  "displayMode": "PAGED",
                                  "openType": "PUBLIC",
                                  "departmentIds": [],
                                  "status": "PUBLISHED"
                                }
                                """.formatted(paperId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.qualifyScore").value(6))
                .andExpect(jsonPath("$.data.displayMode").value("PAGED"))
                .andExpect(jsonPath("$.data.openType").value("PUBLIC"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long examId = objectMapper.readTree(examResponse).at("/data/id").asLong();

        mockMvc.perform(get("/api/exam/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));

        mockMvc.perform(post("/api/exam/{examId}/start", examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayMode").value("PAGED"))
                .andExpect(jsonPath("$.data.questions[0].attachments[0].mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.questions[0].attachments[0].fileUrl").value("/assets/quick.png"))
                .andExpect(jsonPath("$.data.questions[0].options.length()").value(3));

        mockMvc.perform(post("/api/exam/{examId}/submit", examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": %d, "selectedLabels": ["B"]}
                                  ]
                                }
                                """.formatted(questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalScore").value(10))
                .andExpect(jsonPath("$.data.obtainedScore").value(10))
                .andExpect(jsonPath("$.data.correctCount").value(1));

        mockMvc.perform(post("/api/exam/{examId}/submit", examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": %d, "selectedLabels": ["B"]}
                                  ]
                                }
                                """.formatted(questionId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/exam/{examId}/start", examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].options.length()").value(3));

        String resultsResponse = mockMvc.perform(get("/api/admin/results")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].examTitle").value("阅读理解模拟考试"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long resultId = objectMapper.readTree(resultsResponse).at("/data/0/id").asLong();

        mockMvc.perform(get("/api/admin/results/{resultId}", resultId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].stem").value("Which word means fast?"))
                .andExpect(jsonPath("$.data.questions[0].selectedLabels[0]").value("B"))
                .andExpect(jsonPath("$.data.questions[0].correctLabels[0]").value("B"))
                .andExpect(jsonPath("$.data.questions[0].analysis").value("Quick means fast."));

        mockMvc.perform(get("/api/exam/results/{resultId}", resultId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].correct").value(true));
    }

    @Test
    void seededExamUsesLocalAudioPngAndJpgAttachments() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/exam/{examId}/start", 1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].attachments[0].fileUrl").value("/local-assets/dog-wolf-friendship.mp3"))
                .andExpect(jsonPath("$.data.questions[0].attachments[0].mediaType").value("AUDIO"))
                .andExpect(jsonPath("$.data.questions[1].attachments[0].fileUrl").value("/local-assets/noun-example.png"))
                .andExpect(jsonPath("$.data.questions[1].attachments[0].mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.questions[2].attachments[0].fileUrl").value("/local-assets/improve-card.jpg"))
                .andExpect(jsonPath("$.data.questions[2].attachments[0].mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.questions[3].attachments[0].fileUrl").value("/local-assets/practice-chart.png"))
                .andExpect(jsonPath("$.data.questions[3].attachments[1].fileUrl").value("/local-assets/dog-wolf-friendship.mp3"));
    }

    @Test
    void limitedAttemptExamRejectsRestartAfterSubmittedLimit() throws Exception {
        String token = adminToken();

        String examResponse = mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paperId": 1,
                                  "title": "限定次数考试",
                                  "description": "用于验证可考次数",
                                  "qualifyScore": 0,
                                  "startTime": "2026-01-01T00:00:00",
                                  "endTime": "2026-12-31T23:59:59",
                                  "durationMinutes": 20,
                                  "timeLimit": false,
                                  "attemptLimit": 1,
                                  "displayMode": "PAGED",
                                  "openType": "PUBLIC",
                                  "departmentIds": [],
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attemptLimit").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long examId = objectMapper.readTree(examResponse).at("/data/id").asLong();

        mockMvc.perform(post("/api/exam/{examId}/start", examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/exam/{examId}/submit", examId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "answers": [
                                    {"questionId": 1, "selectedLabels": ["A"]}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/exam/{examId}/start", examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void examDisplayModeIsConfiguredByAdminAndReturnedWithSession() throws Exception {
        String token = adminToken();

        String examResponse = mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paperId": 1,
                                  "title": "整卷显示考试",
                                  "description": "用于验证题目显示配置",
                                  "qualifyScore": 0,
                                  "startTime": "2026-01-01T00:00:00",
                                  "endTime": "2026-12-31T23:59:59",
                                  "durationMinutes": 20,
                                  "timeLimit": false,
                                  "attemptLimit": null,
                                  "displayMode": "ALL",
                                  "openType": "PUBLIC",
                                  "departmentIds": [],
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayMode").value("ALL"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long examId = objectMapper.readTree(examResponse).at("/data/id").asLong();

        mockMvc.perform(post("/api/exam/{examId}/start", examId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayMode").value("ALL"))
                .andExpect(jsonPath("$.data.questions.length()").value(4));
    }

    @Test
    void questionValidationRejectsSingleChoiceWithMultipleCorrectOptions() throws Exception {
        String token = adminToken();

        mockMvc.perform(post("/api/admin/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bankId": 1,
                                  "type": "SINGLE_CHOICE",
                                  "stem": "Invalid single choice",
                                  "analysis": "",
                                  "difficulty": "EASY",
                                  "status": "ACTIVE",
                                  "options": [
                                    {"label": "A", "content": "one", "correct": true},
                                    {"label": "B", "content": "two", "correct": true}
                                  ],
                                  "attachments": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void fileUploadStoresRealMediaAndReturnsAttachmentPayload() throws Exception {
        String token = adminToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "listening.mp3",
                "audio/mpeg",
                new byte[]{1, 2, 3, 4}
        );

        mockMvc.perform(multipart("/api/admin/files")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("listening.mp3"))
                .andExpect(jsonPath("$.data.fileUrl").value(org.hamcrest.Matchers.startsWith("/uploads/")))
                .andExpect(jsonPath("$.data.mediaType").value("AUDIO"));
    }

    @Test
    void questionExcelImportSupportsTemplateSuccessAndRowErrors() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/admin/questions/import-template")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).contains("spreadsheetml.sheet"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "questions.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                questionWorkbook("英语基础题库")
        );
        mockMvc.perform(multipart("/api/admin/questions/import")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        mockMvc.perform(get("/api/admin/questions?page=1&size=20&keyword=Excel import listening question")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].stem").value("Excel import listening question"))
                .andExpect(jsonPath("$.data.records[0].attachments.length()").value(0));

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "questions-invalid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                questionWorkbook("不存在的题库")
        );
        mockMvc.perform(multipart("/api/admin/questions/import")
                        .file(invalidFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.errors[0]").value(org.hamcrest.Matchers.containsString("题库不存在")));
    }

    private byte[] questionWorkbook(String bankName) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("questions");
            Row header = sheet.createRow(0);
            String[] headers = {"题库名称", "题型", "难度", "题干", "选项A", "选项B", "选项C", "选项D", "正确答案", "解析"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(bankName);
            row.createCell(1).setCellValue("多选");
            row.createCell(2).setCellValue("困难");
            row.createCell(3).setCellValue("Excel import listening question");
            row.createCell(4).setCellValue("listen");
            row.createCell(5).setCellValue("speak");
            row.createCell(6).setCellValue("sleep");
            row.createCell(7).setCellValue("walk");
            row.createCell(8).setCellValue("A，B");
            row.createCell(9).setCellValue("Listening and speaking are language skills.");
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

