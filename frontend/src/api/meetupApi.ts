// ============================================================
//  api/meetupApi.ts
//  Layer de comunicare cu backend-ul Spring Boot.
//  Toate apelurile HTTP sunt centralizate aici —
//  separate de store și de UI (best practice).
// ============================================================

import type { Meetup, CreateMeetupPayload, UpdateMeetupPayload } from '../types/indexes.ts'

const BASE_URL = 'http://localhost:8080/api/meetups'

// ── Tipuri răspuns backend ────────────────────────────────────
export interface PagedResponse<T> {
  content: T[]
  page: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface ApiError {
  status: number
  error: string
  errors?: Record<string, string>
}

// ── Helper fetch cu error handling ───────────────────────────
async function apiFetch<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ status: response.status, error: response.statusText }))
    throw error as ApiError
  }

  // 204 No Content — nu are body
  if (response.status === 204) return undefined as T

  return response.json()
}

// ══════════════════════════════════════════════════════════════
//  CRUD API calls
// ══════════════════════════════════════════════════════════════

/** GET /api/meetups?page=0&size=10 */
export async function fetchMeetups(page = 0, size = 10): Promise<PagedResponse<Meetup>> {
  return apiFetch(`${BASE_URL}?page=${page}&size=${size}`)
}

/** GET /api/meetups/{id} */
export async function fetchMeetupById(id: number): Promise<Meetup> {
  return apiFetch(`${BASE_URL}/${id}`)
}

/** POST /api/meetups */
export async function createMeetup(payload: CreateMeetupPayload): Promise<Meetup> {
  return apiFetch(BASE_URL, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/** PUT /api/meetups/{id} */
export async function updateMeetup(id: number, payload: Partial<Meetup>): Promise<Meetup> {
  return apiFetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/** DELETE /api/meetups/{id} */
export async function deleteMeetup(id: number): Promise<void> {
  return apiFetch(`${BASE_URL}/${id}`, { method: 'DELETE' })
}

// ══════════════════════════════════════════════════════════════
//  Statistics
// ══════════════════════════════════════════════════════════════

export async function fetchCount(): Promise<{ total: number }> {
  return apiFetch(`${BASE_URL}/stats/count`)
}

export async function fetchStatsByLocation(): Promise<Record<string, number>> {
  return apiFetch(`${BASE_URL}/stats/by-location`)
}

export async function fetchStatsByBook(): Promise<Record<string, number>> {
  return apiFetch(`${BASE_URL}/stats/by-book`)
}

export async function fetchAverageRating(): Promise<{ averageRating: number }> {
  return apiFetch(`${BASE_URL}/stats/average-rating`)
}

// ══════════════════════════════════════════════════════════════
//  Generator (Silver)
// ══════════════════════════════════════════════════════════════

export async function startGenerator(interval = 2): Promise<{ status: string }> {
  return apiFetch(`${BASE_URL}/generator/start?interval=${interval}`, { method: 'POST' })
}

export async function stopGenerator(): Promise<{ status: string }> {
  return apiFetch(`${BASE_URL}/generator/stop`, { method: 'POST' })
}

export async function fetchGeneratorStatus(): Promise<{ running: boolean }> {
  return apiFetch(`${BASE_URL}/generator/status`)
}
