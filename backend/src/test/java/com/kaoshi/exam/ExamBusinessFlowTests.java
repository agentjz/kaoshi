package com.kaoshi.exam;

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
                                  "score": 10,
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
                                  "startTime": "2026-01-01T00:00:00",
                                  "endTime": "2026-12-31T23:59:59",
                                  "durationMinutes": 20,
                                  "status": "PUBLISHED"
                                }
                                """.formatted(paperId)))
                .andExpect(status().isOk())
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
                .andExpect(jsonPath("$.data.questions[0].attachments[0].mediaType").value("IMAGE"))
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

        mockMvc.perform(get("/api/admin/results")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].examTitle").value("阅读理解模拟考试"));
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
                                  "score": 5,
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

