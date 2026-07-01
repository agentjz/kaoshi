import { describe, expect, it, vi } from 'vitest'

import { apiClient } from './client'
import {
  createExam,
  createPaper,
  createQuestion,
  createQuestionBank,
  fetchAdminResults,
  fetchExamTasks,
  fetchPapers,
  fetchQuestionBanks,
  startExam,
  submitExam,
} from './exam-business'

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}))

const ok = <T>(data: T) => ({ data: { code: 0, message: 'OK', data, timestamp: '2026-07-01T00:00:00Z' } })

describe('exam business api', () => {
  it('loads paged management resources', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok({ records: [], total: 0, page: 1, size: 20 }))

    await fetchQuestionBanks({ page: 1, size: 20, keyword: 'english' })
    await fetchPapers({ page: 1, size: 20 })

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/question-banks', {
      params: { page: 1, size: 20, keyword: 'english' },
    })
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/papers', { params: { page: 1, size: 20 } })
  })

  it('writes question paper and exam payloads', async () => {
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))

    await createQuestionBank({ categoryId: 1, name: 'bank', description: '', status: 'ACTIVE' })
    await createQuestion({
      bankId: 1,
      type: 'SINGLE_CHOICE',
      stem: 'stem',
      analysis: '',
      score: 5,
      difficulty: 'EASY',
      status: 'ACTIVE',
      options: [
        { label: 'A', content: 'a', correct: true },
        { label: 'B', content: 'b', correct: false },
      ],
      attachments: [{ fileName: 'chart.png', fileUrl: 'https://example.com/chart.png', mediaType: 'IMAGE' }],
    })
    await createPaper({
      categoryId: 1,
      name: 'paper',
      description: '',
      durationMinutes: 30,
      status: 'ACTIVE',
      questions: [{ questionId: 1, score: 5 }],
    })
    await createExam({
      paperId: 1,
      title: 'exam',
      description: '',
      startTime: '2026-01-01T00:00:00',
      endTime: '2026-12-31T23:59:59',
      durationMinutes: 30,
      status: 'PUBLISHED',
    })

    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/question-banks', {
      categoryId: 1,
      name: 'bank',
      description: '',
      status: 'ACTIVE',
    })
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/questions', {
      bankId: 1,
      type: 'SINGLE_CHOICE',
      stem: 'stem',
      analysis: '',
      score: 5,
      difficulty: 'EASY',
      status: 'ACTIVE',
      options: [
        { label: 'A', content: 'a', correct: true },
        { label: 'B', content: 'b', correct: false },
      ],
      attachments: [{ fileName: 'chart.png', fileUrl: 'https://example.com/chart.png', mediaType: 'IMAGE' }],
    })
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/exams', {
      paperId: 1,
      title: 'exam',
      description: '',
      startTime: '2026-01-01T00:00:00',
      endTime: '2026-12-31T23:59:59',
      durationMinutes: 30,
      status: 'PUBLISHED',
    })
  })

  it('uses exam portal endpoints for task start submit and results', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok([]))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))

    await fetchExamTasks()
    await startExam(1)
    await submitExam(1, [{ questionId: 2, selectedLabels: ['A'] }])
    await fetchAdminResults()

    expect(apiClient.get).toHaveBeenCalledWith('/api/exam/tasks')
    expect(apiClient.post).toHaveBeenCalledWith('/api/exam/1/start')
    expect(apiClient.post).toHaveBeenCalledWith('/api/exam/1/submit', {
      answers: [{ questionId: 2, selectedLabels: ['A'] }],
    })
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/results')
  })
})
