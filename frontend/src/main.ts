import 'element-plus/dist/index.css'

import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import './styles/base.css'

const app = createApp(App)

function isIgnoredBrowserError(message: unknown) {
  return typeof message === 'string' && message.includes('ResizeObserver loop')
}

app.config.errorHandler = (error, instance, info) => {
  if (error instanceof Error && isIgnoredBrowserError(error.message)) {
    return
  }
  console.error('[kaoshi 前端运行错误]', { error, instance, info })
}

window.addEventListener('unhandledrejection', (event) => {
  console.error('[kaoshi 未处理的异步错误]', event.reason)
})

window.addEventListener('error', (event) => {
  if (isIgnoredBrowserError(event.message)) {
    return
  }
  console.error('[kaoshi 浏览器运行错误]', event.error || event.message)
})

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')

