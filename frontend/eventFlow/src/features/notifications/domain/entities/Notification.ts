export type NotificationType =
  | 'PURCHASE_CONFIRMATION'
  | 'PAYMENT_CONFIRMED'
  | 'EVENT_CANCELLED'
  | 'REFUND_COMPLETED'
  | 'REFUND_FAILED'
  | 'GENERAL'

export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED'

export interface Notification {
  id: string
  userId: string
  type: NotificationType
  subject: string
  message: string
  status: NotificationStatus
  referenceId: string | null
  sentAt: string | null
  createdAt: string
}
