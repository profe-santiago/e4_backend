import { useMutation } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useTicketRepository } from '@/core/di/TicketContext'
import { ValidateTicketUseCase } from '../../application/use-cases/ValidateTicketUseCase'

export const useValidateTicket = () => {
  const ticketRepository = useTicketRepository()

  return useMutation({
    mutationFn: (qrCode: string) =>
      new ValidateTicketUseCase(ticketRepository).execute(qrCode),
    onSuccess: () => toast.success('Ticket validado correctamente'),
    onError: () => toast.error('Ticket inválido o ya utilizado'),
  })
}
