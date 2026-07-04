import { isDemoMode } from '@/runtime/app-mode'
import { demoAdminAdapter } from './demo/admin-demo-adapter'
import { demoAuthAdapter } from './demo/auth-demo-adapter'
import { demoExamBusinessAdapter } from './demo/exam-business-demo-adapter'
import { realAdminAdapter } from './real/admin-real-adapter'
import { realAuthAdapter } from './real/auth-real-adapter'
import { realExamBusinessAdapter } from './real/exam-business-real-adapter'

export const authAdapter = isDemoMode ? demoAuthAdapter : realAuthAdapter
export const adminAdapter = isDemoMode ? demoAdminAdapter : realAdminAdapter
export const examBusinessAdapter = isDemoMode ? demoExamBusinessAdapter : realExamBusinessAdapter
