insert into questions (id, bank_id, type, stem, analysis, difficulty, status)
values
  (4, 1, 'MULTIPLE_CHOICE', 'Look at the chart and choose the correct statements.', 'The chart highlights daily reading and listening practice.', 'HARD', 'ACTIVE');

insert into question_options (question_id, option_label, content, is_correct, sort_order)
values
  (4, 'A', 'The learner practiced reading.', true, 10),
  (4, 'B', 'The learner practiced listening.', true, 20),
  (4, 'C', 'The learner skipped all practice.', false, 30),
  (4, 'D', 'The chart is about cooking.', false, 40);

insert into question_attachments (question_id, file_name, file_url, media_type, sort_order)
values
  (2, 'noun-example.png', '/local-assets/noun-example.png', 'IMAGE', 10),
  (3, 'improve-card.jpg', '/local-assets/improve-card.jpg', 'IMAGE', 10),
  (4, 'practice-chart.png', '/local-assets/practice-chart.png', 'IMAGE', 10),
  (4, 'dog-wolf-friendship.mp3', '/local-assets/dog-wolf-friendship.mp3', 'AUDIO', 20);

insert into paper_questions (paper_id, question_id, score, sort_order)
values (1, 4, 5.00, 40);

update papers
set total_score = 20.00,
    updated_at = current_timestamp
where id = 1;
