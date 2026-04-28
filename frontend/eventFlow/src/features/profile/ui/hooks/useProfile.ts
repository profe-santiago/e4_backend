import { useQuery } from '@tanstack/react-query'
import { useUserRepository } from '@/core/di/UserContext'
import { GetProfileUseCase } from '../../application/use-cases/GetProfileUseCase'

export const useProfile = () => {
  const userRepository = useUserRepository()

  return useQuery({
    queryKey: ['profile'],
    queryFn: () => new GetProfileUseCase(userRepository).execute(),
  })
}
