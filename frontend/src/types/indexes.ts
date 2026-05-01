// ============================================================
//  CommonPlot — Domain Types
// ============================================================

// ── Meet-up ──────────────────────────────────────────────────
export interface Meetup {
  id: number
  titleEvent: string
  location: string
  date: string
  bookID: number
  duration: number
  ownerID: number
  ownerUsername: string
  rating: number
  description: string
  bookTitle?: string
  bookAuthor?: string
}

// ── Book ─────────────────────────────────────────────────────
export interface Book {
  id: number
  title: string
  author: string
  description?: string
  coverUrl?: string
}

// ── User ─────────────────────────────────────────────────────
export interface User {
  id: number
  username: string
  fullName: string
  email: string
}

// ── Payloads ──────────────────────────────────────────────────
export type CreateMeetupPayload = Omit<Meetup, 'id'>
export type UpdateMeetupPayload = Partial<Meetup> & { id: number }
export type ValidationErrors    = Partial<Record<keyof Meetup, string>>

export type CreateBookPayload = Omit<Book, 'id'>
export type UpdateBookPayload = Partial<Book> & { id: number }
