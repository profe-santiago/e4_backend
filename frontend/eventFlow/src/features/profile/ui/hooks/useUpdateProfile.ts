import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useUserRepository } from '@/core/di/UserContext'
import { UpdateProfileUseCase } from '../../application/use-cases/UpdateProfileUseCase'
import type { UpdateUserRequest } from '../../domain/entities/User'

export const useUpdateProfile = () => {
  const queryClient = useQueryClient()
  const userRepository = useUserRepository()

  return useMutation({
    mutationFn: (data: UpdateUserRequest) =>
      new UpdateProfileUseCase(userRepository).execute(data),
    onSuccess: (updatedUser) => {
      queryClient.setQueryData(['profile'], updatedUser)
      toast.success('Perfil actualizado correctamente')
    },
    onError: () => toast.error('Error al actualizar el perfil'),
  })
}
