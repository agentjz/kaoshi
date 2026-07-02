import { describe, expect, it, vi } from 'vitest'

import { apiClient } from './client'
import {
  createExam,
  createPaper,
  createQuestion,
  createQuestionBank,
  downloadQuestionImportTemplate,
  fetchAdminResultDetail,
  fetchAdminResults,
  fetchExamTasks,
  fetchMyExamResultDetail,
  fetchMyExamResults,
  fetchPapers,
  fetchQuestionBanks,
  fetchQuestionDetail,
  importQuestions,
  startExam,
  submitExam,
  uploadFile,
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
      qualifyScore: 3,
      startTime: '2026-01-01T00:00:00',
      endTime: '2026-12-31T23:59:59',
      durationMinutes: 30,
      timeLimit: true,
      attemptLimit: null,
      displayMode: 'PAGED',
      openType: 'PUBLIC',
      departmentIds: [],
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
      qualifyScore: 3,
      startTime: '2026-01-01T00:00:00',
      endTime: '2026-12-31T23:59:59',
      durationMinutes: 30,
      timeLimit: true,
      attemptLimit: null,
      displayMode: 'PAGED',
      openType: 'PUBLIC',
      departmentIds: [],
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
    await fetchAdminResultDetail(9)
    await fetchMyExamResults()
    await fetchMyExamResultDetail(9)

    expect(apiClient.get).toHaveBeenCalledWith('/api/exam/tasks')
    expect(apiClient.post).toHaveBeenCalledWith('/api/exam/1/start')
    expect(apiClient.post).toHaveBeenCalledWith('/api/exam/1/submit', {
      answers: [{ questionId: 2, selectedLabels: ['A'] }],
    })
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/results')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/results/9')
    expect(apiClient.get).toHaveBeenCalledWith('/api/exam/results')
    expect(apiClient.get).toHaveBeenCalledWith('/api/exam/results/9')
  })

  it('uses question detail template and import endpoints', async () => {
    vi.mocked(apiClient.get).mockResolvedValue(ok({}))
    vi.mocked(apiClient.post).mockResolvedValue(ok({}))

    await fetchQuestionDetail(8)
    await downloadQuestionImportTemplate()
    await importQuestions(new File(['xlsx'], 'questions.xlsx'))
    await uploadFile(new File(['audio'], 'listening.mp3'))

    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/questions/8')
    expect(apiClient.get).toHaveBeenCalledWith('/api/admin/questions/import-template', { responseType: 'blob' })
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/questions/import', expect.any(FormData))
    expect(apiClient.post).toHaveBeenCalledWith('/api/admin/files', expect.any(FormData))
  })
})
