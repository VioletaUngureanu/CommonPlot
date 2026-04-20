// ============================================================
//  stores/users.ts
//  Pinia store pentru utilizatori.
//  Sesiunea e persistată prin cookies (cerința Silver).
//  Userii sunt stocați în RAM (sessionStorage).
// ============================================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// ── Tipuri ───────────────────────────────────────────────────
export interface User {
  id: number
  fullName: string
  username: string
  email: string
  password: string        // în producție ar fi hash — aici e plaintext pentru demo
  createdAt: string
}

export interface SessionUser {
  id: number
  fullName: string
  username: string
  email: string
}

// ── Cookie helpers ────────────────────────────────────────────
// Cerința Silver: monitorizare activitate prin cookies

function setCookie(name: string, value: string, days: number): void {
  const expires = new Date()
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${expires.toUTCString()};path=/;SameSite=Strict`
}

function getCookie(name: string): string | null {
  const match = document.cookie
    .split(';')
    .map(c => c.trim())
    .find(c => c.startsWith(`${name}=`))
  return match ? decodeURIComponent(match.split('=')[1]) : null
}

function deleteCookie(name: string): void {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/`
}

// ── Cookie keys ───────────────────────────────────────────────
const COOKIE_SESSION    = 'commonplot_session'
const COOKIE_LAST_VISIT = 'commonplot_last_visit'
const COOKIE_VISIT_COUNT = 'commonplot_visit_count'
const SESSION_KEY_USERS = 'commonplot_users'

// ── Date inițiale ─────────────────────────────────────────────
const INITIAL_USERS: User[] = [
  {
    id: 1,
    fullName: 'Admin User',
    username: 'admin',
    email: 'admin@commonplot.com',
    password: 'admin123',
    createdAt: '2026-01-01T00:00:00',
  },
  {
    id: 2,
    fullName: 'Alex Marinescu',
    username: 'alexm',
    email: 'alex@commonplot.com',
    password: 'password123',
    createdAt: '2026-02-01T00:00:00',
  },
]

// ── SessionStorage helpers ────────────────────────────────────
function loadUsers(): User[] {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY_USERS)
    return raw ? JSON.parse(raw) : [...INITIAL_USERS]
  } catch {
    return [...INITIAL_USERS]
  }
}

function saveUsers(users: User[]): void {
  try {
    sessionStorage.setItem(SESSION_KEY_USERS, JSON.stringify(users))
  } catch { /* ignore */ }
}

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

  const users      = ref<User[]>(loadUsers())
  const nextId     = ref<number>(100)
  const currentUser = ref<SessionUser | null>(loadSessionFromCookie())

  // ── Getters ──
  const isLoggedIn  = computed(() => currentUser.value !== null)
  const allUsers    = computed(() => users.value)

  // ── Activity tracking (Silver: cookies) ──────────────────────
  function trackVisit(): void {
    const now = new Date().toISOString()

    // Ultima vizită
    setCookie(COOKIE_LAST_VISIT, now, 30)

    // Număr de vizite
    const count = parseInt(getCookie(COOKIE_VISIT_COUNT) ?? '0', 10)
    setCookie(COOKIE_VISIT_COUNT, String(count + 1), 30)
  }

  function getActivityInfo(): { lastVisit: string | null; visitCount: number } {
    return {
      lastVisit: getCookie(COOKIE_LAST_VISIT),
      visitCount: parseInt(getCookie(COOKIE_VISIT_COUNT) ?? '0', 10),
    }
  }

  // ── Actions ──────────────────────────────────────────────────

  /**
   * Înregistrare user nou.
   */
  function register(payload: {
    fullName: string
    username: string
    email: string
    password: string
  }): { success: boolean; error?: string } {

    if (users.value.some(u => u.email === payload.email)) {
      return { success: false, error: 'Email already in use.' }
    }
    if (users.value.some(u => u.username === payload.username)) {
      return { success: false, error: 'Username already taken.' }
    }

    const newUser: User = {
      id: nextId.value++,
      fullName: payload.fullName,
      username: payload.username,
      email: payload.email,
      password: payload.password,
      createdAt: new Date().toISOString(),
    }

    users.value.push(newUser)
    saveUsers(users.value)

    // Auto-login după register
    _setSession(newUser)
    return { success: true }
  }

  /**
   * Autentificare.
   */
  function login(email: string, password: string): { success: boolean; error?: string } {
    const user = users.value.find(u => u.email === email && u.password === password)
    if (!user) return { success: false, error: 'Invalid email or password.' }

    _setSession(user)
    trackVisit()
    return { success: true }
  }

  /**
   * Deconectare.
   */
  function logout(): void {
    currentUser.value = null
    deleteCookie(COOKIE_SESSION)
  }

  /**
   * Salvează sesiunea în cookie (30 zile).
   */
  function _setSession(user: User): void {
    const session: SessionUser = {
      id: user.id,
      fullName: user.fullName,
      username: user.username,
      email: user.email,
    }
    currentUser.value = session
    setCookie(COOKIE_SESSION, JSON.stringify(session), 30)
  }

  function getUserById(id: number): User | undefined {
    return users.value.find(u => u.id === id)
  }

  return {
    users,
    currentUser,
    isLoggedIn,
    allUsers,
    register,
    login,
    logout,
    trackVisit,
    getActivityInfo,
    getUserById,
  }
})
