import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { PrivateRoute } from './PrivateRoute'
import { PublicRoute } from './PublicRoute'
import { RoleRoute } from './RoleRoute'

const LoginPage = lazy(() => import('@/features/auth/ui/pages/LoginPage'))
const RegisterPage = lazy(() => import('@/features/auth/ui/pages/RegisterPage'))

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    element: <PublicRoute />,
    children: [
      {
        path: '/',
        lazy: () => import('@/features/events/ui/pages/EventListPage').then((m) => ({ Component: m.EventListPage })),
      },
      {
        path: '/events/:id',
        lazy: () => import('@/features/events/ui/pages/EventDetailPage').then((m) => ({ Component: m.EventDetailPage })),
      },
    ],
  },
  {
    element: <PrivateRoute />,
    children: [
      {
        path: '/events/:id/checkout',
        lazy: () => import('@/features/orders/ui/pages/CheckoutPage').then((m) => ({ Component: m.CheckoutPage })),
      },
      {
        path: '/orders',
        lazy: () => import('@/features/orders/ui/pages/MyOrdersPage').then((m) => ({ Component: m.MyOrdersPage })),
      },
      {
        path: '/orders/:id',
        lazy: () => import('@/features/orders/ui/pages/OrderDetailPage').then((m) => ({ Component: m.OrderDetailPage })),
      },
      {
        path: '/payments/:id',
        lazy: () => import('@/features/payments/ui/pages/PaymentDetailPage').then((m) => ({ Component: m.PaymentDetailPage })),
      },
      {
        path: '/tickets',
        lazy: () => import('@/features/tickets/ui/pages/MyTicketsPage').then((m) => ({ Component: m.MyTicketsPage })),
      },
      {
        path: '/tickets/:id',
        lazy: () => import('@/features/tickets/ui/pages/TicketDetailPage').then((m) => ({ Component: m.TicketDetailPage })),
      },
      {
        path: '/profile',
        lazy: () => import('@/features/profile/ui/pages/ProfilePage').then((m) => ({ Component: m.ProfilePage })),
      },
      {
        path: '/notifications',
        lazy: () => import('@/features/notifications/ui/pages/NotificationsPage').then((m) => ({ Component: m.NotificationsPage })),
      },
      {
        element: <RoleRoute requiredRole="ADMIN" />,
        children: [
          {
            path: '/my-events',
            lazy: () => import('@/features/events/ui/pages/MyEventsPage').then((m) => ({ Component: m.MyEventsPage })),
          },
          {
            path: '/events/new',
            lazy: () => import('@/features/events/ui/pages/CreateEventPage').then((m) => ({ Component: m.CreateEventPage })),
          },
          {
            path: '/events/:id/edit',
            lazy: () => import('@/features/events/ui/pages/EditEventPage').then((m) => ({ Component: m.EditEventPage })),
          },
          {
            path: '/events/:id/overview',
            lazy: () => import('@/features/events/ui/pages/EventOverviewPage').then((m) => ({ Component: m.EventOverviewPage })),
          },
          {
            path: '/admin/tickets/validate',
            lazy: () => import('@/features/tickets/ui/pages/ValidateTicketPage').then((m) => ({ Component: m.ValidateTicketPage })),
          },
          {
            path: '/admin/categories',
            lazy: () => import('@/features/events/ui/pages/AdminCategoriesPage').then((m) => ({ Component: m.AdminCategoriesPage })),
          },
        ],
      },
    ],
  },
])

export const AppRouter = () => (
  <Suspense fallback={<div>Cargando...</div>}>
    <RouterProvider router={router} />
  </Suspense>
)
