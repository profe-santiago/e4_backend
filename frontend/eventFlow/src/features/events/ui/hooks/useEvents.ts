import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useEventRepository } from '@/core/di/EventContext'
import { ListEventsUseCase } from '../../application/use-cases/ListEventsUseCase'

export const useEvents = () => {
  const [page, setPage] = useState(0)
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined)
  const eventRepository = useEventRepository()

  const query = useQuery({
    queryKey: ['events', { page, categoryId }],
    queryFn: () => new ListEventsUseCase(eventRepository).execute({ page, categoryId, size: 12 }),
  })

  const onPageChange = (newPage: number) => setPage(newPage)

  const onCategoryChange = (id: number | undefined) => {
    setCategoryId(id)
    setPage(0)
  }

  return { ...query, page, categoryId, onPageChange, onCategoryChange }
}
