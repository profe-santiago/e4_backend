import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useUserRepository } from '@/core/di/UserContext'
import { useAuthStore } from '@/store/auth.store'
import { DeleteAccountUseCase } from '../../application/use-cases/DeleteAccountUseCase'

export const useDeleteAccount = () => {
  const navigate = useNavigate()
  const logout = useAuthStore((s) => s.logout)
  const userRepository = useUserRepository()

  return useMutation({
    mutationFn: () => new DeleteAccountUseCase(userRepository).execute(),
    onSuccess: () => {
      logout()
      navigate('/login')
      toast.success('Cuenta eliminada')
    },
    onError: () => toast.error('Error al eliminar la cuenta'),
  })
}
