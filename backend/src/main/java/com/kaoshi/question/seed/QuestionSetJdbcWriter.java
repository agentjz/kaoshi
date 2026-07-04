package com.kaoshi.question.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class QuestionSetJdbcWriter {
    private final JdbcTemplate jdbcTemplate;

    public QuestionSetJdbcWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void importSet(String resourcePath, QuestionSetResource resource) {
        if (isImported(resource.code())) {
            return;
        }
        Map<String, Long> categoryIds = importCategories(resource.categories());
        Map<String, Long> bankIds = importBanks(resource.banks(), categoryIds);
        Map<String, Long> questionIds = importQuestions(resource.sections(), bankIds);
        importExams(resource.exams(), questionIds);
        jdbcTemplate.update(
                "insert into question_set_imports (resource_code, resource_path) values (?, ?)",
                resource.code(),
                resourcePath
        );
    }

    private boolean isImported(String code) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from question_set_imports where resource_code = ?",
                Integer.class,
                code
        );
        return count != null && count > 0;
    }

    private Map<String, Long> importCategories(List<QuestionSetResource.CategoryResource> categories) {
        Map<String, Long> ids = new HashMap<>();
        for (QuestionSetResource.CategoryResource category : categories) {
            Long id = insert("""
                    insert into question_categories (name, description, sort_order)
                    values (?, ?, ?)
                    """, ps -> {
                ps.setString(1, category.name());
                ps.setString(2, category.description());
                ps.setInt(3, valueOrDefault(category.sortOrder(), 0));
            });
            ids.put(category.code(), id);
        }
        return ids;
    }

    private Map<String, Long> importBanks(List<QuestionSetResource.BankResource> banks, Map<String, Long> categoryIds) {
        Map<String, Long> ids = new HashMap<>();
        for (QuestionSetResource.BankResource bank : banks) {
            Long categoryId = required(categoryIds, bank.categoryCode(), "题库分类不存在");
            Long id = insert("""
                    insert into question_banks (category_id, name, description, status)
                    values (?, ?, ?, ?)
                    """, ps -> {
                ps.setLong(1, categoryId);
                ps.setString(2, bank.name());
                ps.setString(3, bank.description());
                ps.setString(4, valueOrDefault(bank.status(), "ACTIVE"));
            });
            ids.put(bank.code(), id);
        }
        return ids;
    }

    private Map<String, Long> importQuestions(List<QuestionSetResource.SectionResource> sections, Map<String, Long> bankIds) {
        Map<String, Long> ids = new HashMap<>();
        for (QuestionSetResource.SectionResource section : nullToEmpty(sections)) {
            for (QuestionSetResource.GroupResource group : nullToEmpty(section.groups())) {
                Long bankId = required(bankIds, group.bankCode(), "题库不存在");
                Long sectionNodeId = upsertSectionNode(bankId, section);
                Long groupNodeId = insertGroupNode(bankId, sectionNodeId, group);
                importNodeOptions(groupNodeId, group.sharedOptions());
                importNodeAttachments(sectionNodeId, section.attachments());
                importNodeAttachments(groupNodeId, group.attachments());
                for (QuestionSetResource.QuestionResource question : nullToEmpty(group.items())) {
                    Long questionId = insertQuestion(bankId, groupNodeId, question);
                    ids.put(question.code(), questionId);
                    importItemOptions(questionId, group.sharedOptions(), question);
                    importAnswerLabels(questionId, question.answerLabels());
                    importItemAttachments(questionId, question.attachments());
                }
            }
        }
        return ids;
    }

    private Long upsertSectionNode(Long bankId, QuestionSetResource.SectionResource section) {
        Long existing = queryLong("""
                select id from question_nodes
                where bank_id = ? and node_code = ? and node_type = 'SECTION'
                """, bankId, section.code());
        if (existing != null) {
            return existing;
        }
        return insert("""
                insert into question_nodes (bank_id, parent_id, node_code, node_type, title, direction, material, sort_order)
                values (?, null, ?, 'SECTION', ?, ?, ?, ?)
                """, ps -> {
            ps.setLong(1, bankId);
            ps.setString(2, section.code());
            ps.setString(3, section.title());
            ps.setString(4, section.direction());
            ps.setString(5, section.material());
            ps.setInt(6, valueOrDefault(section.sortOrder(), 0));
        });
    }

    private Long insertGroupNode(Long bankId, Long sectionNodeId, QuestionSetResource.GroupResource group) {
        return insert("""
                insert into question_nodes (bank_id, parent_id, node_code, node_type, title, direction, material, sort_order)
                values (?, ?, ?, 'GROUP', ?, ?, ?, ?)
                """, ps -> {
            ps.setLong(1, bankId);
            ps.setLong(2, sectionNodeId);
            ps.setString(3, group.groupCode());
            ps.setString(4, group.groupTitle());
            ps.setString(5, group.groupDirection());
            ps.setString(6, group.groupMaterial());
            ps.setInt(7, valueOrDefault(group.groupSortOrder(), 0));
        });
    }

    private Long insertQuestion(
            Long bankId,
            Long groupNodeId,
            QuestionSetResource.QuestionResource question
    ) {
        return insert("""
                insert into questions (
                  bank_id, node_id, type, stem, item_label, item_stem, analysis, difficulty, status
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ps -> {
            ps.setLong(1, bankId);
            ps.setLong(2, groupNodeId);
            ps.setString(3, question.type());
            ps.setString(4, question.stem());
            ps.setString(5, question.itemLabel());
            ps.setString(6, question.itemStem());
            ps.setString(7, question.analysis());
            ps.setString(8, valueOrDefault(question.difficulty(), "HARD"));
            ps.setString(9, valueOrDefault(question.status(), "ACTIVE"));
        });
    }

    private void importNodeOptions(Long nodeId, List<QuestionSetResource.OptionResource> options) {
        for (QuestionSetResource.OptionResource option : nullToEmpty(options)) {
            jdbcTemplate.update("""
                    insert into question_node_options (node_id, option_label, content, sort_order)
                    values (?, ?, ?, ?)
                    """,
                    nodeId,
                    option.label(),
                    option.content(),
                    valueOrDefault(option.sortOrder(), 0)
            );
        }
    }

    private void importNodeAttachments(Long nodeId, List<QuestionSetResource.AttachmentResource> attachments) {
        for (QuestionSetResource.AttachmentResource attachment : nullToEmpty(attachments)) {
            jdbcTemplate.update("""
                    insert into question_node_attachments (node_id, file_name, file_url, media_type, sort_order)
                    values (?, ?, ?, ?, ?)
                    """,
                    nodeId,
                    attachment.fileName(),
                    attachment.fileUrl(),
                    attachment.mediaType(),
                    valueOrDefault(attachment.sortOrder(), 0)
            );
        }
    }

    private void importItemOptions(
            Long questionId,
            List<QuestionSetResource.OptionResource> sharedOptions,
            QuestionSetResource.QuestionResource question
    ) {
        if (!nullToEmpty(sharedOptions).isEmpty()) {
            return;
        }
        for (QuestionSetResource.OptionResource option : nullToEmpty(question.options())) {
            jdbcTemplate.update("""
                    insert into question_options (question_id, option_label, content, is_correct, sort_order)
                    values (?, ?, ?, ?, ?)
                    """,
                    questionId,
                    option.label(),
                    option.content(),
                    option.correct(),
                    valueOrDefault(option.sortOrder(), 0)
            );
        }
    }

    private void importAnswerLabels(Long questionId, List<String> answerLabels) {
        int sort = 10;
        for (String label : nullToEmpty(answerLabels)) {
            jdbcTemplate.update("""
                    insert into question_answer_labels (question_id, answer_label, sort_order)
                    values (?, ?, ?)
                    """, questionId, label, sort);
            sort += 10;
        }
    }

    private void importItemAttachments(Long questionId, List<QuestionSetResource.AttachmentResource> attachments) {
        for (QuestionSetResource.AttachmentResource attachment : nullToEmpty(attachments)) {
            jdbcTemplate.update("""
                    insert into question_attachments (question_id, file_name, file_url, media_type, sort_order)
                    values (?, ?, ?, ?, ?)
                    """,
                    questionId,
                    attachment.fileName(),
                    attachment.fileUrl(),
                    attachment.mediaType(),
                    valueOrDefault(attachment.sortOrder(), 0)
            );
        }
    }

    private void importExams(List<QuestionSetResource.ExamResource> exams, Map<String, Long> questionIds) {
        for (QuestionSetResource.ExamResource exam : exams) {
            Long examId = insert("""
                    insert into exams (title, description, qualify_score, start_time, end_time, duration_minutes, time_limit, attempt_limit, display_mode, question_order_mode, open_type, status)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, ps -> {
                ps.setString(1, exam.title());
                ps.setString(2, exam.description());
                ps.setBigDecimal(3, exam.qualifyScore());
                ps.setObject(4, exam.startTime());
                ps.setObject(5, exam.endTime());
                ps.setInt(6, exam.durationMinutes());
                ps.setBoolean(7, Boolean.TRUE.equals(exam.timeLimit()));
                ps.setObject(8, exam.attemptLimit());
                ps.setString(9, exam.displayMode());
                ps.setString(10, exam.questionOrderMode());
                ps.setString(11, exam.openType());
                ps.setString(12, exam.status());
            });
            importDraftPaper(examId, exam.paperQuestions(), questionIds);
            if ("PUBLISHED".equals(exam.status())) {
                importPublishedPaper(examId);
            }
        }
    }

    private void importDraftPaper(
            Long examId,
            List<QuestionSetResource.PaperQuestionResource> paperQuestions,
            Map<String, Long> questionIds
    ) {
        for (QuestionSetResource.PaperQuestionResource paperQuestion : paperQuestions) {
            Long questionId = required(questionIds, paperQuestion.questionCode(), "试卷题目不存在");
            Map<String, Object> row = sourceQuestion(questionId);
            Long draftNodeId = copyDraftNodeFromSource(examId, numberOrNull(row.get("node_id")));
            Long draftQuestionId = insertDraftQuestion(examId, paperQuestion, row, draftNodeId);
            copyQuestionOptions("exam_draft_options", "draft_question_id", draftQuestionId, questionId);
            copyAnswerLabels("exam_draft_answer_labels", "draft_question_id", draftQuestionId, "question_answer_labels", "question_id", questionId);
            copyQuestionAttachments("exam_draft_attachments", "draft_question_id", draftQuestionId, questionId);
        }
    }

    private void importPublishedPaper(Long examId) {
        List<Map<String, Object>> draftRows = jdbcTemplate.queryForList("""
                select * from exam_draft_questions where exam_id = ? order by sort_order, id
                """, examId);
        for (Map<String, Object> draft : draftRows) {
            Long publishedNodeId = copyPublishedNodeFromDraft(examId, numberOrNull(draft.get("draft_node_id")));
            Long publishedQuestionId = insertPublishedQuestion(examId, draft, publishedNodeId);
            Long draftQuestionId = number(draft.get("id"));
            copySnapshotRows("exam_published_options", "published_question_id", publishedQuestionId, "exam_draft_options", "draft_question_id", draftQuestionId);
            copyAnswerLabels("exam_published_answer_labels", "published_question_id", publishedQuestionId, "exam_draft_answer_labels", "draft_question_id", draftQuestionId);
            copySnapshotAttachments("exam_published_attachments", "published_question_id", publishedQuestionId, "exam_draft_attachments", "draft_question_id", draftQuestionId);
        }
    }

    private Map<String, Object> sourceQuestion(Long questionId) {
        return jdbcTemplate.queryForMap("""
                select q.*, b.name as bank_name
                from questions q
                join question_banks b on b.id = q.bank_id
                where q.id = ?
                """, questionId);
    }

    private Long insertDraftQuestion(
            Long examId,
            QuestionSetResource.PaperQuestionResource paperQuestion,
            Map<String, Object> row,
            Long draftNodeId
    ) {
        return insert("""
                insert into exam_draft_questions (
                  exam_id, draft_node_id, source_question_id, bank_id, bank_name, type, stem,
                  item_label, item_stem, analysis, score, sort_order
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ps -> {
            ps.setLong(1, examId);
            setNullableLong(ps, 2, draftNodeId);
            ps.setLong(3, number(row.get("id")));
            ps.setLong(4, number(row.get("bank_id")));
            ps.setString(5, Objects.toString(row.get("bank_name"), ""));
            ps.setString(6, Objects.toString(row.get("type"), ""));
            ps.setString(7, Objects.toString(row.get("stem"), ""));
            ps.setString(8, Objects.toString(row.get("item_label"), null));
            ps.setString(9, Objects.toString(row.get("item_stem"), null));
            ps.setString(10, Objects.toString(row.get("analysis"), null));
            ps.setBigDecimal(11, paperQuestion.score());
            ps.setInt(12, valueOrDefault(paperQuestion.sortOrder(), 0));
        });
    }

    private Long insertPublishedQuestion(Long examId, Map<String, Object> row, Long publishedNodeId) {
        return insert("""
                insert into exam_published_questions (
                  exam_id, published_node_id, source_question_id, bank_id, bank_name, type, stem,
                  item_label, item_stem, analysis, score, sort_order
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ps -> {
            ps.setLong(1, examId);
            setNullableLong(ps, 2, publishedNodeId);
            ps.setLong(3, number(row.get("source_question_id")));
            ps.setLong(4, number(row.get("bank_id")));
            ps.setString(5, Objects.toString(row.get("bank_name"), ""));
            ps.setString(6, Objects.toString(row.get("type"), ""));
            ps.setString(7, Objects.toString(row.get("stem"), ""));
            ps.setString(8, Objects.toString(row.get("item_label"), null));
            ps.setString(9, Objects.toString(row.get("item_stem"), null));
            ps.setString(10, Objects.toString(row.get("analysis"), null));
            ps.setBigDecimal(11, (java.math.BigDecimal) row.get("score"));
            ps.setInt(12, numberOrDefault(row.get("sort_order"), 0));
        });
    }

    private Long copyDraftNodeFromSource(Long examId, Long sourceNodeId) {
        if (sourceNodeId == null) {
            return null;
        }
        Long existing = queryLong("select id from exam_draft_nodes where exam_id = ? and source_node_id = ?", examId, sourceNodeId);
        if (existing != null) {
            return existing;
        }
        Map<String, Object> source = jdbcTemplate.queryForMap("select * from question_nodes where id = ?", sourceNodeId);
        Long parentId = copyDraftNodeFromSource(examId, numberOrNull(source.get("parent_id")));
        Long draftNodeId = insert("""
                insert into exam_draft_nodes (exam_id, source_node_id, parent_id, node_code, node_type, title, direction, material, sort_order)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ps -> bindNode(ps, examId, sourceNodeId, parentId, source));
        copyNodeOptions("exam_draft_node_options", "draft_node_id", draftNodeId, "question_node_options", "node_id", sourceNodeId);
        copyNodeAttachments("exam_draft_node_attachments", "draft_node_id", draftNodeId, "question_node_attachments", "node_id", sourceNodeId);
        return draftNodeId;
    }

    private Long copyPublishedNodeFromDraft(Long examId, Long draftNodeId) {
        if (draftNodeId == null) {
            return null;
        }
        Map<String, Object> draft = jdbcTemplate.queryForMap("select * from exam_draft_nodes where id = ?", draftNodeId);
        Long sourceNodeId = number(draft.get("source_node_id"));
        Long existing = queryLong("select id from exam_published_nodes where exam_id = ? and source_node_id = ?", examId, sourceNodeId);
        if (existing != null) {
            return existing;
        }
        Long parentId = copyPublishedNodeFromDraft(examId, numberOrNull(draft.get("parent_id")));
        Long publishedNodeId = insert("""
                insert into exam_published_nodes (exam_id, source_node_id, parent_id, node_code, node_type, title, direction, material, sort_order)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, ps -> bindNode(ps, examId, sourceNodeId, parentId, draft));
        copyNodeOptions("exam_published_node_options", "published_node_id", publishedNodeId, "exam_draft_node_options", "draft_node_id", draftNodeId);
        copyNodeAttachments("exam_published_node_attachments", "published_node_id", publishedNodeId, "exam_draft_node_attachments", "draft_node_id", draftNodeId);
        return publishedNodeId;
    }

    private void bindNode(PreparedStatement ps, Long ownerId, Long sourceNodeId, Long parentId, Map<String, Object> row) throws java.sql.SQLException {
        ps.setLong(1, ownerId);
        ps.setLong(2, sourceNodeId);
        setNullableLong(ps, 3, parentId);
        ps.setString(4, Objects.toString(row.get("node_code"), ""));
        ps.setString(5, Objects.toString(row.get("node_type"), ""));
        ps.setString(6, Objects.toString(row.get("title"), null));
        ps.setString(7, Objects.toString(row.get("direction"), null));
        ps.setString(8, Objects.toString(row.get("material"), null));
        ps.setInt(9, numberOrDefault(row.get("sort_order"), 0));
    }

    private void copyQuestionOptions(String table, String column, Long targetQuestionId, Long sourceQuestionId) {
        jdbcTemplate.update("""
                insert into %s (%s, option_label, content, is_correct, sort_order)
                select ?, option_label, content, is_correct, sort_order
                from question_options
                where question_id = ?
                order by sort_order, id
                """.formatted(table, column), targetQuestionId, sourceQuestionId);
    }

    private void copySnapshotRows(String table, String column, Long targetId, String sourceTable, String sourceColumn, Long sourceId) {
        jdbcTemplate.update("""
                insert into %s (%s, option_label, content, is_correct, sort_order)
                select ?, option_label, content, is_correct, sort_order
                from %s
                where %s = ?
                order by sort_order, id
                """.formatted(table, column, sourceTable, sourceColumn), targetId, sourceId);
    }

    private void copyAnswerLabels(String table, String column, Long targetId, String sourceTable, String sourceColumn, Long sourceId) {
        jdbcTemplate.update("""
                insert into %s (%s, answer_label, sort_order)
                select ?, answer_label, sort_order
                from %s
                where %s = ?
                order by sort_order, id
                """.formatted(table, column, sourceTable, sourceColumn), targetId, sourceId);
    }

    private void copyQuestionAttachments(String table, String column, Long targetQuestionId, Long sourceQuestionId) {
        jdbcTemplate.update("""
                insert into %s (%s, file_name, file_url, media_type, sort_order)
                select ?, file_name, file_url, media_type, sort_order
                from question_attachments
                where question_id = ?
                order by sort_order, id
                """.formatted(table, column), targetQuestionId, sourceQuestionId);
    }

    private void copySnapshotAttachments(String table, String column, Long targetId, String sourceTable, String sourceColumn, Long sourceId) {
        jdbcTemplate.update("""
                insert into %s (%s, file_name, file_url, media_type, sort_order)
                select ?, file_name, file_url, media_type, sort_order
                from %s
                where %s = ?
                order by sort_order, id
                """.formatted(table, column, sourceTable, sourceColumn), targetId, sourceId);
    }

    private void copyNodeOptions(String table, String column, Long targetNodeId, String sourceTable, String sourceColumn, Long sourceNodeId) {
        jdbcTemplate.update("""
                insert into %s (%s, option_label, content, sort_order)
                select ?, option_label, content, sort_order
                from %s
                where %s = ?
                order by sort_order, id
                """.formatted(table, column, sourceTable, sourceColumn), targetNodeId, sourceNodeId);
    }

    private void copyNodeAttachments(String table, String column, Long targetNodeId, String sourceTable, String sourceColumn, Long sourceNodeId) {
        jdbcTemplate.update("""
                insert into %s (%s, file_name, file_url, media_type, sort_order)
                select ?, file_name, file_url, media_type, sort_order
                from %s
                where %s = ?
                order by sort_order, id
                """.formatted(table, column, sourceTable, sourceColumn), targetNodeId, sourceNodeId);
    }

    private Long insert(String sql, StatementBinder binder) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            binder.bind(statement);
            return statement;
        }, keyHolder);
        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            return number(keyHolder.getKeys().get("id"));
        }
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Long queryLong(String sql, Object... args) {
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1), args);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private <T> List<T> nullToEmpty(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private <T> T required(Map<String, T> values, String key, String message) {
        T value = values.get(key);
        if (value == null) {
            throw new IllegalStateException(message + ": " + key);
        }
        return value;
    }

    private int valueOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private long number(Object value) {
        return ((Number) value).longValue();
    }

    private Long numberOrNull(Object value) {
        return value == null ? null : number(value);
    }

    private int numberOrDefault(Object value, int defaultValue) {
        return value == null ? defaultValue : ((Number) value).intValue();
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            ps.setObject(index, null);
            return;
        }
        ps.setLong(index, value);
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws java.sql.SQLException;
    }
}
