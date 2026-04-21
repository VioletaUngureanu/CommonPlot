// ============================================================
//  api/bookApi.ts
//  Apeluri API pentru relația Book → Meetups (1-to-many)
// ============================================================

import type { Meetup } from '@/types'

const BASE_URL = 'http://localhost:8080/api'

/** GET /api/books/{bookId}/meetups */
export async function fetchMeetupsByBook(bookId: number): Promise<Meetup[]> {
  const response = await fetch(`${BASE_URL}/books/${bookId}/meetups`)
  if (!response.ok) throw new Error('Failed to fetch meetups for book')
  return response.json()
}

/** GET /api/books/{bookId}/meetups/count */
export async function fetchMeetupsCountByBook(bookId: number): Promise<{ bookId: number; count: number }> {
  const response = await fetch(`${BASE_URL}/books/${bookId}/meetups/count`)
  if (!response.ok) throw new Error('Failed to fetch count')
  return response.json()
}

/** GET /api/books/{bookId}/meetups/stats */
export async function fetchMeetupsStatsByBook(bookId: number): Promise<{
  bookId: number
  totalMeetups: number
  avgRating: number
  avgDuration: number
}> {
  const response = await fetch(`${BASE_URL}/books/${bookId}/meetups/stats`)
  if (!response.ok) throw new Error('Failed to fetch stats')
  return response.json()
}
