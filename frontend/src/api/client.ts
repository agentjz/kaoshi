import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

import router from '@/router'
import { useAuthStore } from '@/stores/auth'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
})

apiClient.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<{ code?: number; message?: string }>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '请求失败'
    if (import.meta.env.DEV && !error.response) {
      console.debug('[kaoshi 接口请求失败]', {
        method: error.config?.method,
        url: error.config?.url,
        status,
        message,
      })
    }
    if (status === 401) {
      const auth = useAuthStore()
      auth.clearSession()
      await router.replace({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

