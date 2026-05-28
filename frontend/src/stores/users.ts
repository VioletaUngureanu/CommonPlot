// ============================================================
//  stores/users.ts
//  Pinia store pentru autentificare.
//  - 3-way authentication (challenge → response → JWT)
//  - JWT stocat în cookie
//  - Inactivity logout după 15 minute
//  - Toate requesturile trimit Authorization: Bearer <token>
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
  role: string
  permissions: string[]
}

// ── Cookie helpers ────────────────────────────────────────────
function setCookie(name: string, value: string, days = 1): void {
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
const COOKIE_TOKEN   = 'commonplot_token'

function loadSessionFromCookie(): SessionUser | null {
  try {
    const raw = getCookie(COOKIE_SESSION)
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}

// ── SHA-256 helper (browser native) ──────────────────────────
async function sha256(message: string): Promise<string> {
  const msgBuffer = new TextEncoder().encode(message)
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer)
  const hashArray  = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

// ── Store ─────────────────────────────────────────────────────
export const useUsersStore = defineStore('users', () => {

  const currentUser  = ref<SessionUser | null>(loadSessionFromCookie())
  const token        = ref<string | null>(getCookie(COOKIE_TOKEN))
  const loading      = ref(false)
  const error        = ref<string | null>(null)

  // ── Inactivity logout — 15 minute ─────────────────────────
  const INACTIVITY_MS = 15 * 60 * 1000
  let inactivityTimer: ReturnType<typeof setTimeout> | null = null

  function resetInactivityTimer(): void {
    if (!currentUser.value) return
    if (inactivityTimer) clearTimeout(inactivityTimer)
    inactivityTimer = setTimeout(() => {
      logout()
      window.location.href = '/login?reason=inactivity'
    }, INACTIVITY_MS)
  }

  function startInactivityTracking(): void {
    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart']
    events.forEach(e => window.addEventListener(e, resetInactivityTimer))
    resetInactivityTimer()
  }

  function stopInactivityTracking(): void {
    if (inactivityTimer) clearTimeout(inactivityTimer)
    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart']
    events.forEach(e => window.removeEventListener(e, resetInactivityTimer))
  }

  // Pornește tracking dacă userul e deja logat (reload pagină)
  if (currentUser.value) startInactivityTracking()

  // ── Getters ───────────────────────────────────────────────
  const isLoggedIn    = computed(() => currentUser.value !== null)
  const isAdmin       = computed(() => currentUser.value?.role === 'ADMIN')
  const hasPermission = (permission: string) =>
    currentUser.value?.permissions.includes(permission) ?? false

  // ── Headers helper ────────────────────────────────────────
  function authHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }
    if (token.value) {
      headers['Authorization'] = `Bearer ${token.value}`
    }
    if (currentUser.value) {
      headers['X-Username'] = currentUser.value.username
      headers['X-Role']     = currentUser.value.role
    }
    return headers
  }

  // ── 3-Way Login ───────────────────────────────────────────
  // Step 1: GET /api/auth/challenge → nonce
  // Step 2: response = SHA256(nonce + SHA256(password))
  // Step 3: POST /api/auth/login { username, response } → JWT
  async function login(
    username: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> {
    loading.value = true
    error.value   = null

    try {
      // Step 1 — cere challenge
      const challengeRes = await fetch(
        `${BACKEND_URL}/api/auth/challenge?username=${encodeURIComponent(username)}`
      )
      if (!challengeRes.ok) {
        return { success: false, error: 'Could not get challenge from server.' }
      }
      const { nonce } = await challengeRes.json()

      // Step 2 — calculează response
      const hashedPassword = await sha256(password)
      const response       = await sha256(nonce + hashedPassword)

      // Step 3 — trimite response, primește JWT
      const loginRes = await fetch(`${BACKEND_URL}/api/auth/login`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ username, response }),
      })

      const data = await loginRes.json()

      if (!loginRes.ok) {
        return { success: false, error: data.error ?? 'Login failed.' }
      }

      // Salvează sesiunea și token-ul
      const session: SessionUser = {
        id:          data.id,
        username:    data.username,
        email:       data.email,
        fullName:    data.fullName,
        role:        data.role,
        permissions: data.permissions ?? [],
      }

      currentUser.value = session
      token.value       = data.token

      setCookie(COOKIE_SESSION, JSON.stringify(session))
      setCookie(COOKIE_TOKEN,   data.token)

      startInactivityTracking()
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

      // Backend returnează JWT direct la register
      const session: SessionUser = {
        id:          data.id,
        username:    data.username,
        email:       data.email ?? '',
        fullName:    data.fullName ?? '',
        role:        data.role,
        permissions: data.permissions ?? [],
      }

      currentUser.value = session
      token.value       = data.token

      setCookie(COOKIE_SESSION, JSON.stringify(session))
      setCookie(COOKIE_TOKEN,   data.token)

      startInactivityTracking()
      return { success: true }

    } catch {
      return { success: false, error: 'Cannot connect to server.' }
    } finally {
      loading.value = false
    }
  }

  // ── Forgot password ───────────────────────────────────────
  async function forgotPassword(
    email: string
  ): Promise<{ success: boolean; error?: string }> {
    try {
      const res = await fetch(`${BACKEND_URL}/api/auth/forgot-password`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ email }),
      })
      const data = await res.json()
      return res.ok
        ? { success: true }
        : { success: false, error: data.error }
    } catch {
      return { success: false, error: 'Cannot connect to server.' }
    }
  }

  // ── Reset password ────────────────────────────────────────
  async function resetPassword(
    resetToken: string,
    newPassword: string
  ): Promise<{ success: boolean; error?: string }> {
    try {
      const res = await fetch(`${BACKEND_URL}/api/auth/reset-password`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ token: resetToken, newPassword }),
      })
      const data = await res.json()
      return res.ok
        ? { success: true }
        : { success: false, error: data.error }
    } catch {
      return { success: false, error: 'Cannot connect to server.' }
    }
  }

  // ── Logout ────────────────────────────────────────────────
  function logout(): void {
    currentUser.value = null
    token.value       = null
    deleteCookie(COOKIE_SESSION)
    deleteCookie(COOKIE_TOKEN)
    stopInactivityTracking()
  }

  return {
    currentUser,
    token,
    loading,
    error,
    isLoggedIn,
    isAdmin,
    hasPermission,
    authHeaders,
    login,
    register,
    forgotPassword,
    resetPassword,
    logout,
    resetInactivityTimer,
  }
})
