import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import type { ReactNode } from 'react'
import { AuthContextProvider } from './AuthContext'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 1000 * 60 * 5,
    },
  },
})

interface Props {
  children: ReactNode
}

export const AppProviders = ({ children }: Props) => (
  <QueryClientProvider client={queryClient}>
    <AuthContextProvider>
      {children}
    </AuthContextProvider>
    <Toaster position="top-right" />
  </QueryClientProvider>
)
