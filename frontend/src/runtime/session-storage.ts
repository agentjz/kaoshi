import { isDemoMode } from './app-mode'
import type { CurrentUser } from '@/api/types'

const TOKEN_KEY = 'kaoshi.accessToken'
const USER_KEY = 'kaoshi.currentUser'

let memoryToken = ''
let memoryUser: CurrentUser | null = null

function readUserFromLocalStorage(): CurrentUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as CurrentUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const sessionStorageAdapter = {
  readToken() {
    return isDemoMode ? memoryToken : localStorage.getItem(TOKEN_KEY) || ''
  },
  readUser() {
    return isDemoMode ? memoryUser : readUserFromLocalStorage()
  },
  writeSession(token: string, user: CurrentUser) {
    if (isDemoMode) {
      memoryToken = token
      memoryUser = user
      return
    }
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  writeUser(user: CurrentUser) {
    if (isDemoMode) {
      memoryUser = user
      return
    }
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  clear() {
    memoryToken = ''
    memoryUser = null
    if (!isDemoMode) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  },
}
