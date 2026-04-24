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
        TokenStorage.remove()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (status === 403) {
        toast.error('No tenés permiso para realizar esta acción')
        return Promise.reject(error)
      }

      if (status && status >= 500) {
        toast.error('Error del servidor. Intentá de nuevo más tarde.')
        return Promise.reject(error)
      }

      return Promise.reject(error)
    }
  )
}
