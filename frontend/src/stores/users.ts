// ============================================================
//  stores/users.ts
//  Pinia store pentru autentificare.
//  Login/Register comunică cu backend-ul Spring Boot.
//  Sesiunea e persistată prin cookie (commonplot_session).
// ============================================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { BACKEND_URL } from '@/config'

// ── Tipuri ───────────────────────────────────────────────────
export interface SessionUser {
  id: number
  username: string
  email: string
  fullName: string
  role: string                  // 'ADMIN' | 'USER'
  permissions: string[]         // ['MEETUP_CREATE', 'BOOK_READ', ...]
}

// ── Cookie helpers ────────────────────────────────────────────
function setCookie(name: string, value: string, days = 30): void {
  const expires = new Date()
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${expires.toUTCString()};path=/;SameSite=Strict`
}

function getCookie(name: string): string | null {
  const match = document.cookie
    .split(';')
    .map(c => c.trim())
    .find(c => c.startsWith(`${name}=`))
  return match ? decodeURIComponent(match.split('=')[1]!) : null
}

function deleteCookie(name: string): void {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/`
}

const COOKIE_SESSION = 'commonplot_session'

function loadSessionFromCookie(): SessionUser | null {
  try {
    const raw = getCookie(COOKIE_SESSION)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// ── Store ─────────────────────────────────────────────────────
export const useUsersStore = defineStore('users', () => {

  const currentUser = ref<SessionUser | null>(loadSessionFromCookie())
  const loading     = ref(false)
  const error       = ref<string | null>(null)

  // ── Getters ───────────────────────────────────────────────
  const isLoggedIn = computed(() => currentUser.value !== null)
  const isAdmin    = computed(() => currentUser.value?.role === 'ADMIN')

  const hasPermission = (permission: string) =>
    currentUser.value?.permissions.includes(permission) ?? false

  // ── Login ─────────────────────────────────────────────────
  async function login(
    username: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> {
    loading.value = true
    error.value   = null
    try {
      const res = await fetch(`${BACKEND_URL}/api/auth/login`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ username, password }),
      })

      const data = await res.json()

      if (!res.ok) {
        return { success: false, error: data.error ?? 'Login failed.' }
      }

      const session: SessionUser = {
        id:          data.id,
        username:    data.username,
        email:       data.email,
        fullName:    data.fullName,
        role:        data.role,
        permissions: data.permissions ?? [],
      }

      currentUser.value = session
      setCookie(COOKIE_SESSION, JSON.stringify(session))
      return { success: true }

    } catch {
      return { success: false, error: 'Cannot connect to server.' }
    } finally {
      loading.value = false
    }
  }

  // ── Register ──────────────────────────────────────────────
  async function register(payload: {
    username: string
    password: string
    email: string
    fullName: string
  }): Promise<{ success: boolean; error?: string }> {
    loading.value = true
    error.value   = null
    try {
      const res = await fetch(`${BACKEND_URL}/api/auth/register`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(payload),
      })

      const data = await res.json()

      if (!res.ok) {
        return { success: false, error: data.error ?? 'Registration failed.' }
      }

      // Auto-login după register
      return await login(payload.username, payload.password)

    } catch {
      return { success: false, error: 'Cannot connect to server.' }
    } finally {
      loading.value = false
    }
  }

  // ── Logout ────────────────────────────────────────────────
  function logout(): void {
    currentUser.value = null
    deleteCookie(COOKIE_SESSION)
  }

  return {
    currentUser,
    loading,
    error,
    isLoggedIn,
    isAdmin,
    hasPermission,
    login,
    register,
    logout,
  }
})
