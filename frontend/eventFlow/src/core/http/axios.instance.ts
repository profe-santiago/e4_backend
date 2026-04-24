import axios from 'axios'
import { applyJwtInterceptor } from './interceptors/jwt.interceptor'
import { applyErrorInterceptor } from './interceptors/error.interceptor'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
})

applyJwtInterceptor(apiClient)
applyErrorInterceptor(apiClient)

export { apiClient }
