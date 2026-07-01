import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/admin',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { title: '管理端' },
    children: [
      {
        path: '',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/users/UsersView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'roles',
        name: 'admin-roles',
        component: () => import('@/views/admin/roles/RolesView.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'permissions',
        name: 'admin-permissions',
        component: () => import('@/views/admin/permissions/PermissionsView.vue'),
        meta: { title: '权限清单' },
      },
      {
        path: 'menus',
        name: 'admin-menus',
        component: () => import('@/views/admin/menus/MenusView.vue'),
        meta: { title: '菜单清单' },
      },
      {
        path: 'question-banks',
        name: 'admin-question-banks',
        component: () => import('@/views/admin/question-banks/QuestionBanksView.vue'),
        meta: { title: '题库管理' },
      },
      {
        path: 'questions',
        name: 'admin-questions',
        component: () => import('@/views/admin/questions/QuestionsView.vue'),
        meta: { title: '试题管理' },
      },
      {
        path: 'papers',
        name: 'admin-papers',
        component: () => import('@/views/admin/papers/PapersView.vue'),
        meta: { title: '试卷管理' },
      },
      {
        path: 'exams',
        name: 'admin-exams',
        component: () => import('@/views/admin/exams/ExamsView.vue'),
        meta: { title: '考试管理' },
      },
      {
        path: 'results',
        name: 'admin-results',
        component: () => import('@/views/admin/results/ResultsView.vue'),
        meta: { title: '成绩管理' },
      },
    ],
  },
  {
    path: '/exam',
    component: () => import('@/layouts/ExamLayout.vue'),
    meta: { title: '考试端' },
    children: [
      {
        path: '',
        name: 'exam-home',
        component: () => import('@/views/exam/ExamHomeView.vue'),
        meta: { title: '考试中心' },
      },
      {
        path: 'results/:resultId',
        name: 'exam-result',
        component: () => import('@/views/exam/ExamResultView.vue'),
        meta: { title: '提交结果' },
      },
      {
        path: ':examId',
        name: 'exam-session',
        component: () => import('@/views/exam/ExamSessionView.vue'),
        meta: { title: '在线作答' },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { public: true, title: '无权限' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  console.info('[kaoshi 路由跳转]', to.fullPath)
  document.title = `${String(to.meta.title || 'kaoshi')} - kaoshi`
  const auth = useAuthStore()
  if (to.meta.public) {
    if (to.name === 'login' && auth.isAuthenticated) {
      return { name: 'admin-dashboard' }
    }
    return true
  }
  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (!auth.user) {
    await auth.loadCurrentUser()
  }
  return true
})

router.onError((error, to) => {
  console.error('[kaoshi 路由加载失败]', { path: to.fullPath, error })
})

export default router

