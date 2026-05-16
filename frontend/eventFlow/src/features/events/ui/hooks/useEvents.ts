import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useEventRepository } from '@/core/di/EventContext'
import { ListEventsUseCase } from '../../application/use-cases/ListEventsUseCase'

export const useEvents = () => {
  const [page, setPage]             = useState(0)
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined)
  const [searchInput, setSearchInput] = useState('')
  const [cityInput, setCityInput]     = useState('')
  const [venueInput, setVenueInput]   = useState('')
  const [search, setSearch] = useState('')
  const [city, setCity]     = useState('')
  const [venue, setVenue]   = useState('')

  const eventRepository = useEventRepository()

  // Debounce: espera 400 ms después de que el usuario deja de escribir
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearch(searchInput)
      setCity(cityInput)
      setVenue(venueInput)
      setPage(0)
    }, 400)
    return () => clearTimeout(timer)
  }, [searchInput, cityInput, venueInput])

  const query = useQuery({
    queryKey: ['events', { page, categoryId, search, city, venue }],
    queryFn: () => new ListEventsUseCase(eventRepository).execute({ page, categoryId, search, city, venue, size: 12 }),
  })

  const onPageChange = (newPage: number) => setPage(newPage)

  const onCategoryChange = (id: number | undefined) => {
    setCategoryId(id)
    setPage(0)
  }

  const onSearchChange  = (v: string) => setSearchInput(v)
  const onCityChange    = (v: string) => setCityInput(v)
  const onVenueChange   = (v: string) => setVenueInput(v)

  const clearFilters = () => {
    setSearchInput('')
    setCityInput('')
    setVenueInput('')
    setCategoryId(undefined)
    setPage(0)
  }

  const hasFilters = !!(searchInput || cityInput || venueInput || categoryId)

  return {
    ...query,
    page,
    categoryId,
    searchInput,
    cityInput,
    venueInput,
    onPageChange,
    onCategoryChange,
    onSearchChange,
    onCityChange,
    onVenueChange,
    clearFilters,
    hasFilters,
  }
}
