import type { AxiosInstance } from 'axios'
import axios from 'axios'
import toast from 'react-hot-toast'
import { TokenStorage } from '@/core/storage/TokenStorage'

export const applyErrorInterceptor = (client: AxiosInstance): void => {
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      if (!axios.isAxiosError(error)) return Promise.reject(error)

      const status = error.response?.status

      if (status === 401) {
        const url = error.config?.url ?? ''
        // Don't redirect on the login/register calls themselves — let the form handle that error
        if (!url.includes('/auth/')) {
          TokenStorage.remove()
          localStorage.removeItem('auth_user')
          window.location.href = '/'
        }
        return Promise.reject(error)
      }

      if (status === 403) {
        toast.error('No tenés permiso para realizar esta acción')
        return Promise.reject(error)
      }

      if (status && status >= 500) {
        // El toast lo muestra cada mutación con contexto propio para evitar mensajes duplicados
        return Promise.reject(error)
      }

      return Promise.reject(error)
    }
  )
}
