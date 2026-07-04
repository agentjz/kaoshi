package com.kaoshi.question.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionSetResourceIntegrityTests {
    private static final String CET4_SET_1 = "question-sets/cet4/2023-03/set-1.json";
    private static final Pattern GARBLED_MARKER = Pattern.compile(".*(\\uFFFD|\\?\\?|[\\p{IsHan}]\\?|\\?[\\p{IsHan}]).*");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cet4Set1ContainsNoGarbledQuestionMarkers() throws Exception {
        JsonNode root;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CET4_SET_1)) {
            assertThat(input).as(CET4_SET_1).isNotNull();
            root = objectMapper.readTree(input);
        }

        List<String> suspectFields = new ArrayList<>();
        collectSuspectText(root, "$", suspectFields);

        assertThat(countItems(root)).isEqualTo(57);
        assertThat(suspectFields).isEmpty();
    }

    @Test
    void examQuestionTablesDoNotStoreRepeatedGroupContextColumns() throws Exception {
        String sql;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db/migration/MIGRATION003__exam_business_core.sql")) {
            assertThat(input).as("MIGRATION003__exam_business_core.sql").isNotNull();
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(sql).contains(
                "create table question_nodes",
                "create table exam_draft_nodes",
                "create table exam_published_nodes",
                "create table exam_attempt_nodes"
        );
        assertThat(sql).doesNotContain(
                "section_code",
                "section_title",
                "group_code",
                "group_title",
                "group_direction",
                "group_material",
                "group_sort_order"
        );
    }

    private int countItems(JsonNode root) {
        int count = 0;
        for (JsonNode section : root.path("sections")) {
            for (JsonNode group : section.path("groups")) {
                count += group.path("items").size();
            }
        }
        return count;
    }

    private void collectSuspectText(JsonNode node, String path, List<String> suspectFields) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> collectSuspectText(entry.getValue(), path + "." + entry.getKey(), suspectFields));
            return;
        }
        if (node.isArray()) {
            for (int index = 0; index < node.size(); index++) {
                collectSuspectText(node.get(index), path + "[" + index + "]", suspectFields);
            }
            return;
        }
        if (node.isTextual() && GARBLED_MARKER.matcher(node.asText()).matches()) {
            suspectFields.add(path);
        }
    }
}
