import { seedExamBusiness } from './demo-exam-seed'
import { seedIdentity } from './demo-identity-seed'
import type { DemoState } from './demo-model'

export function seedState(): DemoState {
  const state: DemoState = {
    currentUserId: null,
    departments: [
      { id: 1, parentId: null, name: '默认组织', code: 'ROOT', description: '演示组织根节点', status: 'ACTIVE', children: [] },
      { id: 2, parentId: 1, name: '教学部', code: 'TEACHING', description: '演示教学部门', status: 'ACTIVE', children: [] },
    ],
    users: [],
    roles: [],
    permissions: [],
    menus: [],
    verificationCodes: [],
    registrationSettings: {
      selfRegistrationEnabled: true,
      emailVerificationRequired: true,
      adminApprovalRequired: false,
      defaultRoleCode: 'STUDENT',
      defaultDepartmentId: 2,
      allowedEmailDomains: [],
      termsText: '',
    },
    mailStatus: {
      enabled: true,
      configured: true,
      deliveryMode: 'LOG',
      from: 'demo@kaoshi.local',
      host: 'demo-log',
      port: null,
      message: '演示环境使用内存验证码，不发送真实邮件',
    },
    categories: [{ id: 101, code: 'cet4-demo', name: '四级样例', description: '大学英语四级演示题库分类', sortOrder: 10 }],
    banks: [],
    questions: [],
    correctLabelsByQuestionId: {},
    exams: [],
    attempts: [],
    results: [],
    participants: {},
    resultPolicies: {},
    examEvents: {},
    securityPolicies: {},
    securityEvents: {},
    reviewRubrics: {},
    reviewTasks: {},
    reviewRechecks: {},
    notifications: [
      {
        id: 900,
        recipientUserId: null,
        title: '平台治理能力已启用',
        content: '演示环境包含身份入口、考试治理、安全事件、阅卷任务和外部集成边界。',
        category: 'SYSTEM',
        read: false,
        createdAt: new Date().toISOString(),
      },
    ],
    externalIntegrations: [
      {
        id: 901,
        name: '默认 Webhook 边界',
        integrationType: 'WEBHOOK',
        endpointUrl: 'https://example.com/kaoshi/webhook',
        secretMask: '****demo',
        enabled: false,
        updatedAt: new Date().toISOString(),
      },
    ],
    externalIntegrationEvents: [],
    fileAssets: [],
    nextId: 1000,
  }
  state.departments[0].children = [state.departments[1]]
  seedIdentity(state)
  seedExamBusiness(state)
  return state
}
