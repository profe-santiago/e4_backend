import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpTicketAdapter } from '@/features/tickets/infrastructure/adapters/HttpTicketAdapter'
import type { TicketRepository } from '@/features/tickets/domain/ports/TicketRepository'

const TicketRepositoryCtx = createContext<TicketRepository | null>(null)

const ticketAdapter = new HttpTicketAdapter(apiClient)

export const TicketContextProvider = ({ children }: { children: ReactNode }) => (
  <TicketRepositoryCtx.Provider value={ticketAdapter}>
    {children}
  </TicketRepositoryCtx.Provider>
)

export const useTicketRepository = (): TicketRepository => {
  const ctx = useContext(TicketRepositoryCtx)
  if (!ctx) throw new Error('useTicketRepository must be used inside TicketContextProvider')
  return ctx
}
