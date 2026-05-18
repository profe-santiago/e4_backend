export const toUtc = (iso: string) =>
  new Date(/Z|[+-]\d{2}:\d{2}$/.test(iso) ? iso : iso + 'Z')

const LOCALE = undefined

export const formatDate = (iso: string) =>
  toUtc(iso).toLocaleDateString(LOCALE, { day: '2-digit', month: 'short', year: 'numeric' })

export const formatDateTime = (iso: string) =>
  toUtc(iso).toLocaleString(LOCALE, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

export const formatDateLong = (iso: string) =>
  toUtc(iso).toLocaleDateString(LOCALE, { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })

export const formatDateShort = (iso: string) =>
  toUtc(iso).toLocaleDateString(LOCALE, { day: '2-digit', month: '2-digit', year: 'numeric' })

export const formatDateTimeShort = (iso: string) =>
  toUtc(iso).toLocaleString(LOCALE, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
