import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import axios from 'axios'
import toast from 'react-hot-toast'
import { TokenStorage } from '@/core/storage/TokenStorage'
import { RefreshTokenStorage } from '@/core/storage/RefreshTokenStorage'
import { useAuthStore } from '@/store/auth.store'

interface RefreshResponse {
  token: string
  refreshToken: string
}

let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (err: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)))
  failedQueue = []
}

const clearSession = () => {
  TokenStorage.remove()
  RefreshTokenStorage.remove()
  localStorage.removeItem('auth_user')
  useAuthStore.getState().logout()
  window.location.href = '/'
}

export const applyErrorInterceptor = (client: AxiosInstance): void => {
  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      if (!axios.isAxiosError(error)) return Promise.reject(error)

      const status = error.response?.status
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

      if (status === 401) {
        const url = originalRequest?.url ?? ''

        // No intentar refresh en llamadas de autenticación
        if (url.includes('/auth/')) {
          return Promise.reject(error)
        }

        if (originalRequest._retry) {
          clearSession()
          return Promise.reject(error)
        }

        const storedRefreshToken = RefreshTokenStorage.get()
        if (!storedRefreshToken) {
          clearSession()
          return Promise.reject(error)
        }

        if (isRefreshing) {
          // Encolar la request hasta que termine el refresh
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`
            return client(originalRequest)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const { data } = await client.post<RefreshResponse>('/api/v1/auth/refresh', {
            refreshToken: storedRefreshToken,
          })

          useAuthStore.getState().updateToken(data.token, data.refreshToken)
          processQueue(null, data.token)

          originalRequest.headers['Authorization'] = `Bearer ${data.token}`
          return client(originalRequest)
        } catch (refreshError) {
          processQueue(refreshError, null)
          clearSession()
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      if (status === 403) {
        toast.error('No tenés permiso para realizar esta acción')
        return Promise.reject(error)
      }

      if (status && status >= 500) {
        return Promise.reject(error)
      }

      return Promise.reject(error)
    }
  )
}
