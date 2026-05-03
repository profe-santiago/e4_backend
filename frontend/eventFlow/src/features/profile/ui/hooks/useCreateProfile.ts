import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useUserRepository } from '@/core/di/UserContext'
import type { CreateProfileRequest } from '../../domain/entities/User'

export const useCreateProfile = () => {
  const userRepository = useUserRepository()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateProfileRequest) => userRepository.createMe(data),
    onSuccess: (user) => {
      queryClient.setQueryData(['profile'], user)
      toast.success('Perfil creado correctamente.')
    },
    onError: () => toast.error('No se pudo crear el perfil. Intenta de nuevo.'),
  })
}
