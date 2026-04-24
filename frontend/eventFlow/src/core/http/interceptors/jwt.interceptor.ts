import type { AxiosInstance } from 'axios'
import { TokenStorage } from '@/core/storage/TokenStorage'

export const applyJwtInterceptor = (client: AxiosInstance): void => {
  client.interceptors.request.use((config) => {
    const token = TokenStorage.get()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })
}
