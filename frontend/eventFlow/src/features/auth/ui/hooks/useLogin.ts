import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import { useAuthStore } from '@/store/auth.store'
import { useAuthRepository } from '@/core/di/AuthContext'
import { LoginUseCase } from '../../application/use-cases/LoginUseCase'

export const useLogin = () => {
  const [isLoading, setIsLoading] = useState(false)
  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()
  const authRepository = useAuthRepository()

  const login = async (email: string, password: string) => {
    setIsLoading(true)
    try {
      const useCase = new LoginUseCase(authRepository)
      const result = await useCase.execute(email, password)
      setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token)
      navigate('/')
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        toast.error('Email o contraseña incorrectos')
      } else {
        toast.error('Error al iniciar sesión')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return { login, isLoading }
}
