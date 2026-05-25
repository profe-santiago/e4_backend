import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import axios from 'axios'
import { useAuthStore } from '@/store/auth.store'
import { useAuthRepository, useUserCreationPort } from '@/core/di/AuthContext'

interface RegisterAdminData {
  firstName: string
  lastName: string
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

      try {
        await userCreationPort.create(
          { email: data.email, firstName: data.firstName, lastName: data.lastName },
          result.token,
        )
      } catch (profileError) {
        if (axios.isAxiosError(profileError) && profileError.response?.status !== 409) {
          setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token, result.refreshToken)
          toast.success('Cuenta creada. Si algo no se muestra correctamente, recarga la página.', { duration: 6000 })
          navigate('/')
          return
        }
      }

      setAuth({ userId: result.userId, email: result.email, role: result.role }, result.token, result.refreshToken)
      toast.success('Cuenta de organizador creada')
      navigate('/')
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status
        if (status === 409) {
          toast.error('Este email ya está registrado.')
        } else if (status && status >= 500) {
          toast.error('Hubo un problema en el servidor. Es posible que tu cuenta ya se haya creado — intenta iniciar sesión.', { duration: 8000 })
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
