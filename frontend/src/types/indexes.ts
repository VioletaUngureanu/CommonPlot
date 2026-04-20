// ============================================================
//  CommonPlot — Domain Types
//  Toate entitățile aplicației sunt definite aici.
//  Importă din acest fișier în store-uri și componente.
// ============================================================

// ── Meet-up ─────────────────────────────────────────────────
export interface Meetup {
    id: number
    ownerID: number
    location: string
    date: string        // ISO 8601: "2026-05-12T10:30:00"
    bookID: number
    duration: number    // în minute, ex: 120
    ownerUsername: string
    rating: number      // 0.0 – 5.0
    description: string
    titleEvent: string
    bookTitle?: string
    bookAuthor?: string
}

// ── Book (referință) ─────────────────────────────────────────
export interface Book {
    id: number
    title: string
    author: string
    coverUrl?: string   // URL imagine copertă (opțional)
}

// ── User (referință) ─────────────────────────────────────────
export interface User {
    id: number
    username: string
    fullName: string
    email: string
}

// ── Payload pentru creare (fără id — îl generează store-ul) ──
export type CreateMeetupPayload = Omit<Meetup, 'id'>

// ── Payload pentru editare (id obligatoriu + orice câmpuri) ──
export type UpdateMeetupPayload = Partial<Meetup> & { id: number }

// ── Erori de validare (cheie = câmp, valoare = mesaj eroare) ─
export type ValidationErrors = Partial<Record<keyof Meetup, string>>
