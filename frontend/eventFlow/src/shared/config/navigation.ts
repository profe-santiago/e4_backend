export interface NavItem {
  path: string
  label: string
  adminOnly?: boolean
}

export const NAV_ITEMS: NavItem[] = [
  { path: '/',                          label: 'Eventos'         },
  { path: '/my-events',                 label: 'Mis Eventos',      adminOnly: true },
  { path: '/orders',                    label: 'Mis Órdenes'     },
  { path: '/tickets',                   label: 'Mis Tickets'     },
  { path: '/profile',                   label: 'Perfil'          },
  { path: '/notifications',             label: 'Notificaciones'  },
  { path: '/admin/categories',          label: 'Categorías',       adminOnly: true },
  { path: '/admin/tickets/validate',    label: 'Validar Tickets',  adminOnly: true },
]
