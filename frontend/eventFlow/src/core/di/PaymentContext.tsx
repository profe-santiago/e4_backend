import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpPaymentAdapter } from '@/features/payments/infrastructure/adapters/HttpPaymentAdapter'
import type { PaymentRepository } from '@/features/payments/domain/ports/PaymentRepository'

const PaymentRepositoryCtx = createContext<PaymentRepository | null>(null)

const paymentAdapter = new HttpPaymentAdapter(apiClient)

export const PaymentContextProvider = ({ children }: { children: ReactNode }) => (
  <PaymentRepositoryCtx.Provider value={paymentAdapter}>
    {children}
  </PaymentRepositoryCtx.Provider>
)

export const usePaymentRepository = (): PaymentRepository => {
  const ctx = useContext(PaymentRepositoryCtx)
  if (!ctx) throw new Error('usePaymentRepository must be used inside PaymentContextProvider')
  return ctx
}
