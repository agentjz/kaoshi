import { describe, expect, it } from 'vitest'

import type { ExamQuestion } from '@/api/exam-business'
import { groupQuestionsForDisplay } from './exam-question-groups'

describe('exam question grouping', () => {
  it('promotes word bank options to a shared group option pool', () => {
    const groups = groupQuestionsForDisplay([
      question(26, 'O'),
      question(27, 'D'),
    ])

    expect(groups).toHaveLength(1)
    expect(groups[0].compactOptionItems).toBe(true)
    expect(groups[0].sharedOptions.map((option) => option.label)).toEqual(['A', 'B'])
  })
})

function question(questionId: number, selected: string): ExamQuestion {
  return {
    questionId,
    type: 'WORD_BANK',
    stem: `第 ${questionId} 题`,
    sectionCode: 'reading',
    sectionTitle: 'Part III Reading',
    groupCode: 'reading-word-bank',
    groupTitle: 'Section A Word Bank',
    groupDirection: 'Choose a word for each blank.',
    groupMaterial: 'Article material',
    itemLabel: String(questionId),
    itemStem: null,
    score: 3.55,
    sortOrder: questionId,
    selectedLabels: selected ? [selected] : [],
    answerText: null,
    attachments: [],
    options: [
      { id: questionId * 10 + 1, label: 'A', content: 'acknowledged', sortOrder: 10 },
      { id: questionId * 10 + 2, label: 'B', content: 'amazement', sortOrder: 20 },
    ],
  }
}
