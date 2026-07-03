import { describe, expect, it } from 'vitest'

import type { ExamQuestion } from '@/api/exam-business'
import { buildSubmitAnswers, countAnsweredQuestions, formatRemainingTime, isQuestionAnswered } from './exam-session'

const questions: ExamQuestion[] = [
  {
    questionId: 1,
    type: 'SINGLE_CHOICE',
    stem: 'Single',
    score: 5,
    sortOrder: 10,
    selectedLabels: [],
    attachments: [],
    options: [],
  },
  {
    questionId: 2,
    type: 'MULTIPLE_CHOICE',
    stem: 'Multiple',
    score: 5,
    sortOrder: 20,
    selectedLabels: [],
    attachments: [],
    options: [],
  },
]

describe('exam session helpers', () => {
  it('counts answered questions by question type', () => {
    expect(isQuestionAnswered(questions[0], { 1: 'A' }, {})).toBe(true)
    expect(isQuestionAnswered(questions[1], {}, { 2: ['C', 'A'] })).toBe(true)
    expect(countAnsweredQuestions(questions, { 1: 'A' }, { 2: [] })).toBe(1)
  })

  it('builds stable submit payloads', () => {
    expect(buildSubmitAnswers(questions, { 1: 'B' }, { 2: ['C', 'A'] })).toEqual([
      { questionId: 1, selectedLabels: ['B'] },
      { questionId: 2, selectedLabels: ['A', 'C'] },
    ])
  })

  it('formats non-negative remaining time', () => {
    expect(formatRemainingTime(65)).toBe('01:05')
    expect(formatRemainingTime(-1)).toBe('00:00')
  })
})
