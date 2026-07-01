import { apiClient } from './client'
import type { ApiResponse } from './types'
import type { PageResult } from './admin'

export interface NamedCategory {
  id: number
  name: string
  description: string | null
  sortOrder: number
}

export interface QuestionBank {
  id: number
  categoryId: number
  categoryName: string
  name: string
  description: string | null
  status: 'ACTIVE' | 'DISABLED'
}

export interface QuestionBankPayload {
  categoryId: number
  name: string
  description: string
  status: QuestionBank['status']
}

export interface QuestionOptionPayload {
  label: string
  content: string
  correct: boolean
}

export interface QuestionOption {
  id: number
  label: string
  content: string
  correct: boolean
  sortOrder: number
}

export interface QuestionAttachmentPayload {
  fileName: string
  fileUrl: string
  mediaType: string
}

export interface QuestionAttachment extends QuestionAttachmentPayload {
  id: number
  sortOrder: number
}

export interface QuestionPayload {
  bankId: number
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE'
  stem: string
  analysis: string
  score: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  status: 'ACTIVE' | 'DISABLED'
  options: QuestionOptionPayload[]
  attachments: QuestionAttachmentPayload[]
}

export interface Question {
  id: number
  bankId: number
  bankName: string
  type: QuestionPayload['type']
  stem: string
  analysis: string | null
  score: number
  difficulty: QuestionPayload['difficulty']
  status: QuestionPayload['status']
  options: QuestionOption[]
  attachments: QuestionAttachment[]
}

export interface PaperQuestionPayload {
  questionId: number
  score: number
}

export interface PaperQuestion {
  id: number
  questionId: number
  questionType: QuestionPayload['type']
  stem: string
  score: number
  sortOrder: number
}

export interface PaperPayload {
  categoryId: number
  name: string
  description: string
  durationMinutes: number
  status: 'ACTIVE' | 'DISABLED'
  questions: PaperQuestionPayload[]
}

export interface Paper {
  id: number
  categoryId: number
  categoryName: string
  name: string
  description: string | null
  totalScore: number
  durationMinutes: number
  status: PaperPayload['status']
  questions: PaperQuestion[]
}

export interface ExamPayload {
  paperId: number
  title: string
  description: string
  startTime: string
  endTime: string
  durationMinutes: number
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
}

export interface Exam {
  id: number
  paperId: number
  paperName: string
  title: string
  description: string | null
  startTime: string
  endTime: string
  durationMinutes: number
  status: ExamPayload['status']
}

export interface ExamQuestionOption {
  id: number
  label: string
  content: string
  sortOrder: number
}

export interface ExamQuestion {
  questionId: number
  type: QuestionPayload['type']
  stem: string
  score: number
  sortOrder: number
  attachments: QuestionAttachment[]
  options: ExamQuestionOption[]
}

export interface ExamSession {
  examId: number
  attemptId: number
  title: string
  durationMinutes: number
  startedAt: string
  attemptStatus: 'IN_PROGRESS' | 'SUBMITTED'
  questions: ExamQuestion[]
}

export interface ExamResult {
  id: number
  attemptId: number
  examId: number
  examTitle: string
  userId: number
  totalScore: number
  obtainedScore: number
  correctCount: number
  questionCount: number
  submittedAt: string
}

export async function fetchQuestionCategories(): Promise<NamedCategory[]> {
  const response = await apiClient.get<ApiResponse<NamedCategory[]>>('/api/admin/question-banks/categories')
  return response.data.data
}

export async function fetchQuestionBanks(params: { page: number; size: number; keyword?: string }): Promise<PageResult<QuestionBank>> {
  const response = await apiClient.get<ApiResponse<PageResult<QuestionBank>>>('/api/admin/question-banks', { params })
  return response.data.data
}

export async function createQuestionBank(payload: QuestionBankPayload): Promise<QuestionBank> {
  const response = await apiClient.post<ApiResponse<QuestionBank>>('/api/admin/question-banks', payload)
  return response.data.data
}

export async function updateQuestionBank(id: number, payload: QuestionBankPayload): Promise<QuestionBank> {
  const response = await apiClient.put<ApiResponse<QuestionBank>>(`/api/admin/question-banks/${id}`, payload)
  return response.data.data
}

export async function fetchQuestions(params: { page: number; size: number; keyword?: string; bankId?: number }): Promise<PageResult<Question>> {
  const response = await apiClient.get<ApiResponse<PageResult<Question>>>('/api/admin/questions', { params })
  return response.data.data
}

export async function createQuestion(payload: QuestionPayload): Promise<Question> {
  const response = await apiClient.post<ApiResponse<Question>>('/api/admin/questions', payload)
  return response.data.data
}

export async function updateQuestion(id: number, payload: QuestionPayload): Promise<Question> {
  const response = await apiClient.put<ApiResponse<Question>>(`/api/admin/questions/${id}`, payload)
  return response.data.data
}

export async function fetchPaperCategories(): Promise<NamedCategory[]> {
  const response = await apiClient.get<ApiResponse<NamedCategory[]>>('/api/admin/papers/categories')
  return response.data.data
}

export async function fetchPapers(params: { page: number; size: number; keyword?: string }): Promise<PageResult<Paper>> {
  const response = await apiClient.get<ApiResponse<PageResult<Paper>>>('/api/admin/papers', { params })
  return response.data.data
}

export async function createPaper(payload: PaperPayload): Promise<Paper> {
  const response = await apiClient.post<ApiResponse<Paper>>('/api/admin/papers', payload)
  return response.data.data
}

export async function updatePaper(id: number, payload: PaperPayload): Promise<Paper> {
  const response = await apiClient.put<ApiResponse<Paper>>(`/api/admin/papers/${id}`, payload)
  return response.data.data
}

export async function fetchAdminExams(params: { page: number; size: number; keyword?: string }): Promise<PageResult<Exam>> {
  const response = await apiClient.get<ApiResponse<PageResult<Exam>>>('/api/admin/exams', { params })
  return response.data.data
}

export async function createExam(payload: ExamPayload): Promise<Exam> {
  const response = await apiClient.post<ApiResponse<Exam>>('/api/admin/exams', payload)
  return response.data.data
}

export async function updateExam(id: number, payload: ExamPayload): Promise<Exam> {
  const response = await apiClient.put<ApiResponse<Exam>>(`/api/admin/exams/${id}`, payload)
  return response.data.data
}

export async function fetchAdminResults(): Promise<ExamResult[]> {
  const response = await apiClient.get<ApiResponse<ExamResult[]>>('/api/admin/results')
  return response.data.data
}

export async function fetchExamTasks(): Promise<Exam[]> {
  const response = await apiClient.get<ApiResponse<Exam[]>>('/api/exam/tasks')
  return response.data.data
}

export async function startExam(examId: number): Promise<ExamSession> {
  const response = await apiClient.post<ApiResponse<ExamSession>>(`/api/exam/${examId}/start`)
  return response.data.data
}

export async function submitExam(examId: number, answers: Array<{ questionId: number; selectedLabels: string[] }>): Promise<ExamResult> {
  const response = await apiClient.post<ApiResponse<ExamResult>>(`/api/exam/${examId}/submit`, { answers })
  return response.data.data
}

export async function fetchMyExamResults(): Promise<ExamResult[]> {
  const response = await apiClient.get<ApiResponse<ExamResult[]>>('/api/exam/results')
  return response.data.data
}
