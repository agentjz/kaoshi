import type { ExamQuestion } from '@/api/exam-business'
import { isManualReviewType, isMultipleAnswerType } from '@/utils/question-types'

export interface SubmitAnswerPayload {
  questionId: number
  selectedLabels?: string[]
  answerText?: string
}

export type SingleAnswerMap = Record<number, string>
export type MultipleAnswerMap = Record<number, string[]>
export type TextAnswerMap = Record<number, string>

export function isQuestionAnswered(
  question: ExamQuestion,
  singleAnswers: SingleAnswerMap,
  multipleAnswers: MultipleAnswerMap,
  textAnswers: TextAnswerMap = {},
) {
  if (isManualReviewType(question.type)) {
    return Boolean(textAnswers[question.questionId]?.trim())
  }
  if (isMultipleAnswerType(question.type)) {
    return (multipleAnswers[question.questionId] || []).length > 0
  }
  return Boolean(singleAnswers[question.questionId])
}

export function countAnsweredQuestions(
  questions: ExamQuestion[],
  singleAnswers: SingleAnswerMap,
  multipleAnswers: MultipleAnswerMap,
  textAnswers: TextAnswerMap = {},
) {
  return questions.filter((question) => isQuestionAnswered(question, singleAnswers, multipleAnswers, textAnswers)).length
}

export function buildSubmitAnswers(
  questions: ExamQuestion[],
  singleAnswers: SingleAnswerMap,
  multipleAnswers: MultipleAnswerMap,
  textAnswers: TextAnswerMap = {},
): SubmitAnswerPayload[] {
  return questions.map((question) => {
    if (isManualReviewType(question.type)) {
      return { questionId: question.questionId, answerText: textAnswers[question.questionId] || '' }
    }
    return {
      questionId: question.questionId,
      selectedLabels:
        isMultipleAnswerType(question.type)
          ? [...(multipleAnswers[question.questionId] || [])].sort()
          : singleAnswers[question.questionId]
            ? [singleAnswers[question.questionId]]
            : [],
    }
  })
}

export function formatRemainingTime(totalSeconds: number) {
  const safeSeconds = Math.max(0, totalSeconds)
  const minutes = Math.floor(safeSeconds / 60)
  const seconds = safeSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}
