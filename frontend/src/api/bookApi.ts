// ============================================================
//  api/bookApi.ts
//  CRUD pentru Books + relația 1-to-many Book → Meetups
// ============================================================

import type { Meetup } from '../types/indexes.ts'

const BASE_URL = '/api/books'

export interface Book {
  id: number
  title: string
  author: string
  description?: string
  genre?: string
  year?: number
  coverUrl?: string
}

async function bookFetch<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!response.ok) {
    const err = await response.json().catch(() => ({ error: response.statusText }))
    throw err
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

// ── CRUD Books ────────────────────────────────────────────────

export async function fetchBooks(): Promise<Book[]> {
  return bookFetch(`${BASE_URL}`)
}

export async function fetchBookById(id: number): Promise<Book> {
  return bookFetch(`${BASE_URL}/${id}`)
}

export async function createBook(payload: Omit<Book, 'id'>): Promise<Book> {
  return bookFetch(`${BASE_URL}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateBook(id: number, payload: Partial<Book>): Promise<Book> {
  return bookFetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteBook(id: number): Promise<void> {
  return bookFetch(`${BASE_URL}/${id}`, { method: 'DELETE' })
}

// ── Relație 1-to-many: Book → Meetups ────────────────────────

export async function fetchMeetupsByBook(bookId: number): Promise<Meetup[]> {
  return bookFetch(`${BASE_URL}/${bookId}/meetups`)
}

export async function fetchMeetupsCountByBook(bookId: number): Promise<{ bookId: number; count: number }> {
  return bookFetch(`${BASE_URL}/${bookId}/meetups/count`)
}

export async function fetchMeetupsStatsByBook(bookId: number): Promise<{
  bookId: number
  totalMeetups: number
  avgRating: number
  avgDuration: number
}> {
  return bookFetch(`${BASE_URL}/${bookId}/meetups/stats`)
}
