import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Publice (fără sidebar, fără auth) ──────────────────
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },

    // ── Protejate (cu sidebar, necesită autentificare) ──────
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'meetups',
          name: 'meetups',
          component: () => import('@/views/MeetupsView.vue'),
        },
        // {
        //   path: 'network',
        //   name: 'network',
        //   component: () => import('@/views/NetworkView.vue'),
        // },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('@/views/StatisticsView.vue'),
        },
        {
          path: 'books',
          name: 'books',
          component: () => import('@/views/MeetupsView.vue'), // placeholder
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/MeetupsView.vue'), // placeholder
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/MeetupsView.vue'), // placeholder
        },
      ],
    },
  ],
})

// ── Navigation Guards ─────────────────────────────────────────
router.beforeEach((to) => {
  const hasSession = document.cookie.includes('commonplot_session')

  if (to.meta.requiresAuth && !hasSession) {
    return { name: 'login' }
  }

  if (to.meta.guestOnly && hasSession) {
    return { name: 'meetups' }
  }
})

export default router
