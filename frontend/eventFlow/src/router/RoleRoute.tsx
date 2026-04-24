import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'

interface Props {
  requiredRole: 'ADMIN' | 'BUYER'
}

export const RoleRoute = ({ requiredRole }: Props) => {
  const user = useAuthStore((s) => s.user)

  if (!user) return <Navigate to="/login" replace />
  if (user.role !== requiredRole) return <Navigate to="/" replace />

  return <Outlet />
}
