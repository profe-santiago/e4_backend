import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import type { ReactNode } from 'react'
import { AuthContextProvider } from './AuthContext'
import { EventContextProvider } from './EventContext'
import { OrderContextProvider } from './OrderContext'
import { TicketContextProvider } from './TicketContext'
import { UserContextProvider } from './UserContext'
import { TicketTypeContextProvider } from './TicketTypeContext'
import { PaymentContextProvider } from './PaymentContext'

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
      <EventContextProvider>
        <OrderContextProvider>
          <TicketContextProvider>
            <UserContextProvider>
              <TicketTypeContextProvider>
                <PaymentContextProvider>
                  {children}
                </PaymentContextProvider>
              </TicketTypeContextProvider>
            </UserContextProvider>
          </TicketContextProvider>
        </OrderContextProvider>
      </EventContextProvider>
    </AuthContextProvider>
    <Toaster position="top-right" />
  </QueryClientProvider>
)
