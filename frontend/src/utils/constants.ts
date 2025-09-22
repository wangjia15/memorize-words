export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const ENDPOINTS = {
  HEALTH: '/api/health',
  WORDS: '/api/words',
  PRACTICE: '/api/practice',
  PROGRESS: '/api/progress',
} as const

export const SPACED_REPETITION_INTERVALS = [
  { days: 1, label: '1 day' },
  { days: 3, label: '3 days' },
  { days: 7, label: '1 week' },
  { days: 14, label: '2 weeks' },
  { days: 30, label: '1 month' },
] as const