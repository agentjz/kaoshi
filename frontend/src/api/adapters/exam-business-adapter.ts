import type { ExcelImportResult } from '../admin'
import type {
  Exam,
  ExamAttemptEvent,
  ExamParticipant,
  ExamPayload,
  ExamReport,
  ExamReviewRecheck,
  ExamReviewRubric,
  ExamReviewTask,
  ExamResult,
  ExamResultDetail,
  ExamResultPolicy,
  ExamSecurityEvent,
  ExamSecurityPolicy,
  ExamSession,
  FileAsset,
  NamedCategory,
  PageResult,
  Question,
  QuestionAttachmentPayload,
  QuestionBank,
  QuestionBankPayload,
  QuestionCategoryPayload,
  QuestionPayload,
} from '../exam-business-types'

export interface ExamBusinessAdapter {
  fetchQuestionCategories(): Promise<NamedCategory[]>
  createQuestionCategory(payload: QuestionCategoryPayload): Promise<NamedCategory>
  updateQuestionCategory(id: number, payload: QuestionCategoryPayload): Promise<NamedCategory>
  deleteQuestionCategory(id: number): Promise<void>
  fetchQuestionBanks(params: { page: number; size: number; keyword?: string }): Promise<PageResult<QuestionBank>>
  createQuestionBank(payload: QuestionBankPayload): Promise<QuestionBank>
  updateQuestionBank(id: number, payload: QuestionBankPayload): Promise<QuestionBank>
  fetchQuestions(params: { page: number; size: number; keyword?: string; bankId?: number }): Promise<PageResult<Question>>
  fetchQuestionDetail(id: number): Promise<Question>
  createQuestion(payload: QuestionPayload): Promise<Question>
  updateQuestion(id: number, payload: QuestionPayload): Promise<Question>
  downloadQuestionImportTemplate(): Promise<Blob>
  importQuestions(file: File): Promise<ExcelImportResult>
  downloadQuestionExport(): Promise<Blob>
  uploadFile(file: File): Promise<QuestionAttachmentPayload>
  fetchFileAssets(): Promise<FileAsset[]>
  fetchAdminExams(params: { page: number; size: number; keyword?: string }): Promise<PageResult<Exam>>
  fetchAdminExamDetail(id: number): Promise<Exam>
  createExam(payload: ExamPayload): Promise<Exam>
  updateExam(id: number, payload: ExamPayload): Promise<Exam>
  publishExam(id: number): Promise<Exam>
  copyExam(id: number): Promise<Exam>
  downloadExamPaper(id: number): Promise<Blob>
  revokeExam(id: number): Promise<Exam>
  closeExam(id: number): Promise<Exam>
  deleteExam(id: number): Promise<void>
  fetchAdminResults(params?: { examId?: number }): Promise<ExamResult[]>
  fetchAdminResultDetail(resultId: number): Promise<ExamResultDetail>
  fetchExamParticipants(examId: number): Promise<ExamParticipant[]>
  replaceExamParticipants(examId: number, userIds: number[]): Promise<ExamParticipant[]>
  updateExamAllowance(examId: number, userId: number, payload: { extraMinutes: number; extraAttempts: number; reason: string }): Promise<ExamParticipant>
  grantExamRetake(examId: number, userId: number, reason: string): Promise<ExamParticipant>
  fetchExamResultPolicy(examId: number): Promise<ExamResultPolicy>
  updateExamResultPolicy(examId: number, payload: { visibleToStudents: boolean; showAnswers: boolean; showAnalysis: boolean; releaseTime: string | null }): Promise<ExamResultPolicy>
  fetchExamReport(examId: number): Promise<ExamReport>
  fetchExamEvents(examId: number): Promise<ExamAttemptEvent[]>
  fetchExamSecurityPolicy(examId: number): Promise<ExamSecurityPolicy>
  updateExamSecurityPolicy(examId: number, payload: Omit<ExamSecurityPolicy, 'examId' | 'updatedAt'>): Promise<ExamSecurityPolicy>
  fetchExamSecurityEvents(examId: number): Promise<ExamSecurityEvent[]>
  recordExamSecurityEvent(examId: number, payload: { attemptId?: number | null; eventType: string; severity?: string; detail?: string }): Promise<void>
  fetchExamReviewRubrics(examId: number): Promise<ExamReviewRubric[]>
  replaceExamReviewRubrics(examId: number, payload: Array<{ title: string; description: string; maxScore: number; sortOrder: number }>): Promise<ExamReviewRubric[]>
  fetchExamReviewTasks(examId: number): Promise<ExamReviewTask[]>
  generateExamReviewTasks(examId: number): Promise<ExamReviewTask[]>
  claimExamReviewTask(examId: number, taskId: number): Promise<ExamReviewTask[]>
  updateExamReviewTask(examId: number, taskId: number, status: ExamReviewTask['status']): Promise<ExamReviewTask[]>
  fetchExamReviewRechecks(examId: number): Promise<ExamReviewRecheck[]>
  requestExamReviewRecheck(examId: number, taskId: number, reason: string): Promise<ExamReviewRecheck[]>
  updateExamReviewRecheck(examId: number, recheckId: number, status: ExamReviewRecheck['status'], resolution: string): Promise<ExamReviewRecheck[]>
  fetchExamTasks(): Promise<Exam[]>
  startExam(examId: number): Promise<ExamSession>
  saveExamAnswers(examId: number, answers: Array<{ questionId: number; selectedLabels?: string[]; answerText?: string }>): Promise<ExamSession>
  submitExam(examId: number, answers: Array<{ questionId: number; selectedLabels?: string[]; answerText?: string }>): Promise<ExamResult>
  reviewWritingQuestion(resultId: number, questionId: number, payload: { score: number; comment: string }): Promise<ExamResultDetail>
  completeResultReview(resultId: number): Promise<ExamResultDetail>
  fetchMyExamResults(): Promise<ExamResult[]>
  fetchMyExamResultDetail(resultId: number): Promise<ExamResultDetail>
}
