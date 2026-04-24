import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import { useAuthStore } from '@/store/auth.store'
import { useAuthRepository, useUserCreationPort } from '@/core/di/AuthContext'
import { RegisterUseCase } from '../../application/use-cases/RegisterUseCase'

interface RegisterData {
  email: string
  password: string
  firstName: string
  lastName: string
}

export const useRegister = () => {
  const [isLoading, setIsLoading] = useState(false)
  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()
  const authRepository = useAuthRepository()
  const userCreationPort = useUserCreationPort()

  const register = async (data: RegisterData) => {
    setIsLoading(true)
    try {
      const useCase = new RegisterUseCase(authRepository, userCreationPort)
      const result = await useCase.execute(data)
      setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token)
      navigate('/')
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 409 || status === 400) {
          toast.error('El email ya está registrado')
        } else {
          toast.error('Error al registrarse')
        }
      }
    } finally {
      setIsLoading(false)
    }
  }

  return { register, isLoading }
}
