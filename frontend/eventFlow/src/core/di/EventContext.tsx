import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpEventAdapter } from '@/features/events/infrastructure/adapters/HttpEventAdapter'
import type { EventRepository } from '@/features/events/domain/ports/EventRepository'

const EventRepositoryCtx = createContext<EventRepository | null>(null)

const eventAdapter = new HttpEventAdapter(apiClient)

export const EventContextProvider = ({ children }: { children: ReactNode }) => (
  <EventRepositoryCtx.Provider value={eventAdapter}>
    {children}
  </EventRepositoryCtx.Provider>
)

export const useEventRepository = (): EventRepository => {
  const ctx = useContext(EventRepositoryCtx)
  if (!ctx) throw new Error('useEventRepository must be used inside EventContextProvider')
  return ctx
}
