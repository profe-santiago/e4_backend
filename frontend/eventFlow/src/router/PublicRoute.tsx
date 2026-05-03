import { Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import { AppLayout } from '@/shared/components/layout/AppLayout'
import { GuestLayout } from '@/shared/components/layout/GuestLayout'

export const PublicRoute = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return isAuthenticated
    ? <AppLayout><Outlet /></AppLayout>
    : <GuestLayout><Outlet /></GuestLayout>
}
