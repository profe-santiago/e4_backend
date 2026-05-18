import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/auth.store'
import { RefreshTokenStorage } from '@/core/storage/RefreshTokenStorage'
import { apiClient } from '@/core/http/axios.instance'

export const useLogout = () => {
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const handleLogout = async () => {
    const refreshToken = RefreshTokenStorage.get()
    if (refreshToken) {
      try {
        await apiClient.post('/api/v1/auth/logout', { refreshToken })
      } catch {
        // Si falla el backend, de todas formas limpiamos la sesión local
      }
    }
    queryClient.clear()
    logout()
    navigate('/')
  }

  return { handleLogout }
}
