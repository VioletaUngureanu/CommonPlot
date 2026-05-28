import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Publice ────────────────────────────────────────────────
    { path: '/', name: 'home', component: HomeView },
    {
      path: '/login', name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register', name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/forgot-password', name: 'forgot-password',
      component: () => import('@/views/ForgotPasswordView.vue'),
    },
    {
      path: '/reset-password', name: 'reset-password',
      component: () => import('@/views/ResetPasswordView.vue'),
    },

    // ── Protejate ──────────────────────────────────────────────
    {
      path: '/', component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'meetups', name: 'meetups',
          component: () => import('@/views/MeetupsView.vue'),
        },
        {
          path: 'books', name: 'books',
          component: () => import('@/views/BooksView.vue'),
        },
        {
          path: 'books/:bookId/meetups', name: 'book-meetups',
          component: () => import('@/views/BookMeetupsView.vue'),
        },
        {
          path: 'statistics', name: 'statistics',
          component: () => import('@/views/StatisticsView.vue'),
        },
        {
          path: 'admin', name: 'admin',
          component: () => import('@/views/AdminView.vue'),
        },
        {
          path: 'network', name: 'network',
          component: () => import('@/views/NetworkView.vue'),
        },
        {
          path: 'chat', name: 'chat',
          component: () => import('@/views/ChatView.vue'),
        },
        {
          path: 'settings', name: 'settings',
          component: () => import('@/views/MeetupsView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const hasSession = document.cookie
    .split(';')
    .some(c => c.trim().startsWith('commonplot_session='))

  if (to.meta.requiresAuth && !hasSession) return { name: 'login' }
  if (to.meta.guestOnly && hasSession)      return { name: 'meetups' }
})

export default router
