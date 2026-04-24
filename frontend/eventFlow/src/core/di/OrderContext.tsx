import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpOrderAdapter } from '@/features/orders/infrastructure/adapters/HttpOrderAdapter'
import type { OrderRepository } from '@/features/orders/domain/ports/OrderRepository'

const OrderRepositoryCtx = createContext<OrderRepository | null>(null)

const orderAdapter = new HttpOrderAdapter(apiClient)

export const OrderContextProvider = ({ children }: { children: ReactNode }) => (
  <OrderRepositoryCtx.Provider value={orderAdapter}>
    {children}
  </OrderRepositoryCtx.Provider>
)

export const useOrderRepository = (): OrderRepository => {
  const ctx = useContext(OrderRepositoryCtx)
  if (!ctx) throw new Error('useOrderRepository must be used inside OrderContextProvider')
  return ctx
}
