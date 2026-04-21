// ============================================================
//  api/meetupApi.ts
//  Layer de comunicare cu backend-ul Spring Boot.
//
//  ── MODE SWITCH ──────────────────────────────────────────────
//  Schimbă doar această linie pentru a comuta între REST și GraphQL:
//  false = REST  (/api/meetups)
//  true  = GraphQL (/graphql)
const USE_GRAPHQL = true
// ─────────────────────────────────────────────────────────────

import type { Meetup, CreateMeetupPayload, UpdateMeetupPayload } from '../types/indexes.ts'
import { GraphQLClient, gql } from 'graphql-request'

const BASE_URL    = 'http://localhost:8080/api/meetups'
const GQL_CLIENT  = new GraphQLClient('http://localhost:8080/graphql')

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

// ══════════════════════════════════════════════════════════════
//  REST helpers
// ══════════════════════════════════════════════════════════════

async function apiFetch<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ status: response.status, error: response.statusText }))
    throw error as ApiError
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

// ══════════════════════════════════════════════════════════════
//  GraphQL helpers
// ══════════════════════════════════════════════════════════════

const MEETUP_FIELDS = `
  id titleEvent location date
  bookID bookTitle bookAuthor
  ownerID ownerUsername duration rating description
`

async function gqlFetchMeetups(page = 0, size = 10): Promise<PagedResponse<Meetup>> {
  const query = gql`
    query GetMeetups($page: Int, $size: Int) {
      meetups(page: $page, size: $size) {
        content { ${MEETUP_FIELDS} }
        page pageSize totalElements totalPages first last
      }
    }
  `
  const data: any = await GQL_CLIENT.request(query, { page, size })
  return data.meetups
}

async function gqlFetchMeetupById(id: number): Promise<Meetup> {
  const query = gql`
    query GetMeetup($id: ID!) {
      meetup(id: $id) { ${MEETUP_FIELDS} }
    }
  `
  const data: any = await GQL_CLIENT.request(query, { id })
  return data.meetup
}

async function gqlCreateMeetup(payload: CreateMeetupPayload): Promise<Meetup> {
  const mutation = gql`
    mutation CreateMeetup($input: MeetupInput!) {
      createMeetup(input: $input) { ${MEETUP_FIELDS} }
    }
  `
  const data: any = await GQL_CLIENT.request(mutation, { input: payload })
  return data.createMeetup
}

async function gqlUpdateMeetup(id: number, payload: Partial<Meetup>): Promise<Meetup> {
  const mutation = gql`
    mutation UpdateMeetup($id: ID!, $input: MeetupInput!) {
      updateMeetup(id: $id, input: $input) { ${MEETUP_FIELDS} }
    }
  `
  const data: any = await GQL_CLIENT.request(mutation, { id, input: payload })
  return data.updateMeetup
}

async function gqlDeleteMeetup(id: number): Promise<void> {
  const mutation = gql`
    mutation DeleteMeetup($id: ID!) {
      deleteMeetup(id: $id)
    }
  `
  await GQL_CLIENT.request(mutation, { id })
}

// ══════════════════════════════════════════════════════════════
//  CRUD — REST sau GraphQL în funcție de USE_GRAPHQL
// ══════════════════════════════════════════════════════════════

/** GET meetups paginat */
export async function fetchMeetups(page = 0, size = 10): Promise<PagedResponse<Meetup>> {
  if (USE_GRAPHQL) return gqlFetchMeetups(page, size)
  return apiFetch(`${BASE_URL}?page=${page}&size=${size}`)
}

/** GET meetup by ID */
export async function fetchMeetupById(id: number): Promise<Meetup> {
  if (USE_GRAPHQL) return gqlFetchMeetupById(id)
  return apiFetch(`${BASE_URL}/${id}`)
}

/** POST — creare */
export async function createMeetup(payload: CreateMeetupPayload): Promise<Meetup> {
  if (USE_GRAPHQL) return gqlCreateMeetup(payload)
  return apiFetch(BASE_URL, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/** PUT — actualizare */
export async function updateMeetup(id: number, payload: Partial<Meetup>): Promise<Meetup> {
  if (USE_GRAPHQL) return gqlUpdateMeetup(id, payload)
  return apiFetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/** DELETE */
export async function deleteMeetup(id: number): Promise<void> {
  if (USE_GRAPHQL) return gqlDeleteMeetup(id)
  return apiFetch(`${BASE_URL}/${id}`, { method: 'DELETE' })
}

// ══════════════════════════════════════════════════════════════
//  Statistics — doar REST
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
