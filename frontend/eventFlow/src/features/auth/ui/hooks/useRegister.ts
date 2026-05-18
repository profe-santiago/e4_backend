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
  const [serverError, setServerError] = useState(false)
  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()
  const authRepository = useAuthRepository()
  const userCreationPort = useUserCreationPort()

  const register = async (data: RegisterData) => {
    if (serverError) return
    setIsLoading(true)
    try {
      const useCase = new RegisterUseCase(authRepository, userCreationPort)
      const result = await useCase.execute(data)
      setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token, result.refreshToken)
      navigate('/')
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 409 || status === 400) {
          toast.error('Este email ya está registrado. Intenta iniciar sesión.')
        } else if (status && status >= 500) {
          setServerError(true)
          toast.error(
            'Hubo un problema en el servidor. Es posible que tu cuenta ya se haya creado — intenta iniciar sesión.',
            { duration: 8000 },
          )
        } else {
          toast.error('No se pudo completar el registro. Intenta de nuevo.')
        }
      }
    } finally {
      setIsLoading(false)
    }
  }

  return { register, isLoading, serverError }
}
