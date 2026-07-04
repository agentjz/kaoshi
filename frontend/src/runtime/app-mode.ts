export type AppMode = 'real' | 'demo'

export const appMode: AppMode = import.meta.env.VITE_APP_MODE === 'demo' ? 'demo' : 'real'

export const isDemoMode = appMode === 'demo'
