// ============================================================
//  api/bookApi.ts — CRUD Books + relație 1-to-many
// ============================================================

import type { Book, Meetup, CreateBookPayload } from '../types/indexes.ts'
import { useUsersStore } from '@/stores/users.ts'

const BASE = '/api/books'

async function apiFetch<T>(url: string, options?: RequestInit): Promise<T> {
  const users = useUsersStore()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (users.currentUser) {
    headers['X-Username'] = users.currentUser.username
    headers['X-Role']     = users.currentUser.role
  }
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }))
    throw err
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

// ── CRUD ──────────────────────────────────────────────────────

export const fetchBooks = (): Promise<Book[]> =>
  apiFetch(BASE)

export const fetchBookById = (id: number): Promise<Book> =>
  apiFetch(`${BASE}/${id}`)

export const createBook = (payload: CreateBookPayload): Promise<Book> =>
  apiFetch(BASE, { method: 'POST', body: JSON.stringify(payload) })

export const updateBook = (id: number, payload: Partial<Book>): Promise<Book> =>
  apiFetch(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(payload) })

export const deleteBook = (id: number): Promise<void> =>
  apiFetch(`${BASE}/${id}`, { method: 'DELETE' })

// ── Relație 1-to-many ─────────────────────────────────────────

export const fetchMeetupsByBook = (bookId: number): Promise<Meetup[]> =>
  apiFetch(`${BASE}/${bookId}/meetups`)

export const fetchMeetupsCountByBook = (bookId: number): Promise<{ bookId: number; count: number }> =>
  apiFetch(`${BASE}/${bookId}/meetups/count`)

export const fetchMeetupsStatsByBook = (bookId: number): Promise<{
  bookId: number
  totalMeetups: number
  avgRating: number
  avgDuration: number
}> => apiFetch(`${BASE}/${bookId}/meetups/stats`)
