import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpTicketTypeAdapter } from '@/features/events/infrastructure/adapters/HttpTicketTypeAdapter'
import type { TicketTypeRepository } from '@/features/events/domain/ports/TicketTypeRepository'

const TicketTypeRepositoryCtx = createContext<TicketTypeRepository | null>(null)

const ticketTypeAdapter = new HttpTicketTypeAdapter(apiClient)

export const TicketTypeContextProvider = ({ children }: { children: ReactNode }) => (
  <TicketTypeRepositoryCtx.Provider value={ticketTypeAdapter}>
    {children}
  </TicketTypeRepositoryCtx.Provider>
)

export const useTicketTypeRepository = (): TicketTypeRepository => {
  const ctx = useContext(TicketTypeRepositoryCtx)
  if (!ctx) throw new Error('useTicketTypeRepository must be used inside TicketTypeContextProvider')
  return ctx
}
