import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import { useAuthStore } from '@/store/auth.store'
import { useAuthRepository, useUserCreationPort } from '@/core/di/AuthContext'

interface RegisterAdminData {
  email: string
  password: string
}

export const useRegisterAdmin = () => {
  const [isLoading, setIsLoading] = useState(false)
  const setAuth = useAuthStore((s) => s.setAuth)
  const navigate = useNavigate()
  const authRepository = useAuthRepository()
  const userCreationPort = useUserCreationPort()

  const registerAdmin = async (data: RegisterAdminData) => {
    setIsLoading(true)
    try {
      const result = await authRepository.registerAdmin(data.email, data.password)
      await userCreationPort.create({ email: data.email, firstName: 'Organizador', lastName: '' }, result.token)
      setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token, result.refreshToken)
      toast.success('Cuenta de organizador creada')
      navigate('/')
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 409) {
          toast.error('Este email ya está registrado.')
        } else {
          toast.error('No se pudo crear la cuenta. Intenta de nuevo.')
        }
      }
    } finally {
      setIsLoading(false)
    }
  }

  return { registerAdmin, isLoading }
}
