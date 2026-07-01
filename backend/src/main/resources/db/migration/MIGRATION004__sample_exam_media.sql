insert into questions (id, bank_id, type, stem, analysis, score, difficulty, status)
values
  (4, 1, 'MULTIPLE_CHOICE', 'Look at the chart and choose the correct statements.', 'The chart highlights daily reading and listening practice.', 5.00, 'MEDIUM', 'ACTIVE');

insert into question_options (question_id, option_label, content, is_correct, sort_order)
values
  (4, 'A', 'The learner practiced reading.', true, 10),
  (4, 'B', 'The learner practiced listening.', true, 20),
  (4, 'C', 'The learner skipped all practice.', false, 30),
  (4, 'D', 'The chart is about cooking.', false, 40);

insert into question_attachments (question_id, file_name, file_url, media_type, sort_order)
values
  (2, 'noun-example.png', 'https://dummyimage.com/960x480/e8f0fe/1f2937.png&text=book+%2B+teacher+are+nouns', 'IMAGE', 10),
  (3, 'improve-card.png', 'https://dummyimage.com/960x480/fef3c7/1f2937.png&text=improve+%3D+make+better', 'IMAGE', 10),
  (4, 'practice-chart.png', 'https://dummyimage.com/960x480/ecfdf5/065f46.png&text=Reading+and+Listening+Practice', 'IMAGE', 10),
  (4, 'practice-audio.mp3', 'https://interactive-examples.mdn.mozilla.net/media/cc0-audio/t-rex-roar.mp3', 'AUDIO', 20);

insert into paper_questions (paper_id, question_id, score, sort_order)
values (1, 4, 5.00, 40);

update papers
set total_score = 20.00,
    updated_at = current_timestamp
where id = 1;
