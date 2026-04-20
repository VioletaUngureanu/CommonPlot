// // ============================================================
// //  stores/meetups.ts
// //  Pinia store — "RAM-ul" aplicației pentru meet-up-uri.
// //  Persistență prin sessionStorage (RAM al tab-ului).
// //  Se resetează când închizi browser-ul — respectă cerința
// //  "stored solely in RAM of the client machine".
// // ============================================================
//
// import { defineStore } from 'pinia'
// import { ref, computed } from 'vue'
// import type { Meetup, CreateMeetupPayload, UpdateMeetupPayload } from '../types/indexes.ts'
// import { validateMeetup, hasErrors } from '@/utils/meetupValidation'
//
//
// const INITIAL_MEETUPS: Meetup[] = [
//   // ── Luni (2026-04-06) ──
//   { id: 101, ownerID: 53,  ownerUsername: 'Alex M.',   location: 'Bunt, Cluj-Napoca',    date: '2026-04-06T10:30:00', bookID: 300, duration: 120, rating: 4.9, titleEvent: 'Morning Coffee & Dostoievski',  description: 'Discussing the moral dilemmas from the first chapters.' },
//   { id: 102, ownerID: 34,  ownerUsername: 'Maria P.',  location: 'Cafe, Iași',           date: '2026-04-06T15:00:00', bookID: 57,  duration: 90,  rating: 4.5, titleEvent: 'Atomic Habits Monday',         description: 'Building better reading habits together.' },
//   { id: 103, ownerID: 70,  ownerUsername: 'Ionut B.',  location: 'Everast, București',   date: '2026-04-06T18:00:00', bookID: 39,  duration: 60,  rating: 4.2, titleEvent: 'Evening Philosophy',           description: 'A short but intense discussion about existentialism.' },
//   { id: 104, ownerID: 12,  ownerUsername: 'Sorin T.',  location: 'Meron, Cluj-Napoca',   date: '2026-04-06T11:00:00', bookID: 57,  duration: 150, rating: 4.7, titleEvent: 'Atomic Habits Deep Dive',      description: 'Exploring habit loops and cue-routine-reward.' },
//   // ── Marți (2026-04-07) ──
//   { id: 105, ownerID: 136, ownerUsername: 'Andrei V.', location: 'Meron, Cluj-Napoca',   date: '2026-04-07T10:00:00', bookID: 101, duration: 120, rating: 4.8, titleEvent: 'Kafka & Cappuccino',           description: "Exploring the absurd through Kafka's lens." },
//   { id: 106, ownerID: 20,  ownerUsername: 'Diana F.',  location: 'Cafe, Iași',           date: '2026-04-07T14:00:00', bookID: 300, duration: 90,  rating: 4.0, titleEvent: 'Brothers K — Chapter 3',       description: 'An afternoon dedicated to the Karamazov family.' },
//   { id: 107, ownerID: 88,  ownerUsername: 'Petra M.',  location: 'Bunt, Cluj-Napoca',    date: '2026-04-07T16:00:00', bookID: 57,  duration: 75,  rating: 4.3, titleEvent: 'Habits Tuesday',               description: 'Weekly Atomic Habits accountability session.' },
//   { id: 108, ownerID: 55,  ownerUsername: 'Radu C.',   location: 'Hub, București',       date: '2026-04-07T18:00:00', bookID: 39,  duration: 60,  rating: 4.1, titleEvent: 'Crime & Punishment II',        description: 'Continuing our journey with Raskolnikov.' },
//   { id: 109, ownerID: 91,  ownerUsername: 'Ioana L.',  location: 'Everast, București',   date: '2026-04-07T19:30:00', bookID: 120, duration: 90,  rating: 4.6, titleEvent: 'War and Peace Tuesday',        description: 'Discussing the Napoleonic campaigns.' },
//   // ── Miercuri (2026-04-08) ──
//   { id: 110, ownerID: 44,  ownerUsername: 'Bogdan S.', location: 'Cafe, Iași',           date: '2026-04-08T10:00:00', bookID: 39,  duration: 120, rating: 4.4, titleEvent: 'Dostoevsky Wednesdays',        description: 'Weekly Crime and Punishment reading circle.' },
//   { id: 111, ownerID: 77,  ownerUsername: 'Alina R.',  location: 'Meron, Cluj-Napoca',   date: '2026-04-08T12:00:00', bookID: 57,  duration: 60,  rating: 4.7, titleEvent: 'Midweek Habits Check',        description: 'Midweek accountability for our habits journey.' },
//   { id: 112, ownerID: 33,  ownerUsername: 'Cristi P.', location: 'Hub, București',       date: '2026-04-08T15:00:00', bookID: 300, duration: 90,  rating: 4.5, titleEvent: 'Karamazov Wednesdays',        description: 'The Grand Inquisitor chapter discussion.' },
//   { id: 113, ownerID: 66,  ownerUsername: 'Mihai D.',  location: 'Bunt, Cluj-Napoca',    date: '2026-04-08T17:00:00', bookID: 101, duration: 75,  rating: 4.2, titleEvent: 'Kafka Afternoon',              description: 'The Metamorphosis and its themes.' },
//   { id: 114, ownerID: 22,  ownerUsername: 'Simona V.', location: 'Everast, București',   date: '2026-04-08T19:00:00', bookID: 39,  duration: 60,  rating: 4.3, titleEvent: 'Crime & Punishment III',       description: 'The police investigation chapters.' },
//   // ── Joi (2026-04-09) ──
//   { id: 115, ownerID: 105, ownerUsername: 'Elena D.',  location: 'Meron, Cluj-Napoca',   date: '2026-04-09T11:00:00', bookID: 57,  duration: 150, rating: 4.7, titleEvent: 'Spring Reads',                 description: 'Celebrating spring with uplifting reads.' },
//   { id: 116, ownerID: 48,  ownerUsername: 'Tudor M.',  location: 'Cafe, Iași',           date: '2026-04-09T14:00:00', bookID: 300, duration: 90,  rating: 4.8, titleEvent: 'Thursday Brothers K',          description: 'Alyosha and the monastery chapters.' },
//   { id: 117, ownerID: 83,  ownerUsername: 'Lena P.',   location: 'Hub, București',       date: '2026-04-09T16:00:00', bookID: 120, duration: 60,  rating: 4.4, titleEvent: 'War & Peace Thursday',         description: 'Pierre Bezukhov and his transformation.' },
//   { id: 118, ownerID: 61,  ownerUsername: 'Felix T.',  location: 'Bunt, Cluj-Napoca',    date: '2026-04-09T18:30:00', bookID: 39,  duration: 90,  rating: 4.6, titleEvent: 'Raskolnikov Thursdays',        description: 'The psychological breakdown chapters.' },
//   // ── Vineri (2026-04-10) ──
//   { id: 119, ownerID: 29,  ownerUsername: 'Ana G.',    location: 'Cafe, Iași',           date: '2026-04-10T10:00:00', bookID: 57,  duration: 60,  rating: 4.5, titleEvent: 'Friday Habits',                description: 'End of week habits review.' },
//   { id: 120, ownerID: 74,  ownerUsername: 'Vlad C.',   location: 'Everast, București',   date: '2026-04-10T13:00:00', bookID: 101, duration: 90,  rating: 4.3, titleEvent: 'Kafka Fridays',                description: 'The Trial — court scenes analysis.' },
//   { id: 121, ownerID: 38,  ownerUsername: 'Oana M.',   location: 'Meron, Cluj-Napoca',   date: '2026-04-10T16:00:00', bookID: 300, duration: 120, rating: 4.9, titleEvent: 'Karamazov Friday',             description: 'The dramatic confession scene.' },
//   // ── Sâmbătă (2026-04-11) ──
//   { id: 122, ownerID: 57,  ownerUsername: 'Dan R.',    location: 'Bunt, Cluj-Napoca',    date: '2026-04-11T10:00:00', bookID: 57,  duration: 120, rating: 4.8, titleEvent: 'Saturday Habits Club',         description: 'Weekend deep dive into atomic habits.' },
//   { id: 123, ownerID: 93,  ownerUsername: 'Irina S.',  location: 'Hub, București',       date: '2026-04-11T12:00:00', bookID: 39,  duration: 90,  rating: 4.5, titleEvent: 'Crime Saturday',               description: 'Final chapters of Crime and Punishment.' },
//   { id: 124, ownerID: 15,  ownerUsername: 'Liviu A.',  location: 'Cafe, Iași',           date: '2026-04-11T14:00:00', bookID: 300, duration: 60,  rating: 4.6, titleEvent: 'Karamazov Weekend',            description: 'The epilogue and final reflections.' },
//   { id: 125, ownerID: 42,  ownerUsername: 'Mara B.',   location: 'Everast, București',   date: '2026-04-11T16:30:00', bookID: 120, duration: 90,  rating: 4.4, titleEvent: 'War & Peace Saturday',         description: 'Natasha and Andrei — love and war.' },
//   { id: 126, ownerID: 68,  ownerUsername: 'Paul N.',   location: 'Meron, Cluj-Napoca',   date: '2026-04-11T18:00:00', bookID: 57,  duration: 75,  rating: 4.7, titleEvent: 'Habits Evening',               description: 'Reflecting on our weekly habit progress.' },
//   { id: 127, ownerID: 31,  ownerUsername: 'Daria C.',  location: 'Bunt, Cluj-Napoca',    date: '2026-04-11T20:00:00', bookID: 101, duration: 60,  rating: 4.2, titleEvent: 'Late Night Kafka',             description: 'Kafka by night — the castle metaphors.' },
//   // ── Duminică (2026-04-12) ──
//   { id: 128, ownerID: 146, ownerUsername: 'Radu M.',   location: 'Bunt, Cluj-Napoca',    date: '2026-04-12T10:00:00', bookID: 57,  duration: 75,  rating: 4.6, titleEvent: 'Sunday Habits',                description: 'Preparing our habits for the week ahead.' },
//   { id: 129, ownerID: 20,  ownerUsername: 'Diana F.',  location: 'Cafe, Iași',           date: '2026-04-12T12:00:00', bookID: 121, duration: 90,  rating: 4.0, titleEvent: 'Poetry & Prose',               description: 'An afternoon dedicated to Romanian literature.' },
//   { id: 130, ownerID: 53,  ownerUsername: 'Alex M.',   location: 'Everast, București',   date: '2026-04-12T14:00:00', bookID: 300, duration: 120, rating: 4.9, titleEvent: 'Karamazov Sunday',             description: 'Final thoughts on the Brothers Karamazov.' },
//   { id: 131, ownerID: 70,  ownerUsername: 'Ionut B.',  location: 'Hub, București',       date: '2026-04-12T16:00:00', bookID: 39,  duration: 60,  rating: 4.3, titleEvent: 'Philosophy Sunday',            description: 'Existentialism and its modern relevance.' },
//   { id: 132, ownerID: 136, ownerUsername: 'Andrei V.', location: 'Meron, Cluj-Napoca',   date: '2026-04-12T18:00:00', bookID: 120, duration: 90,  rating: 4.5, titleEvent: 'Tolstoi Sundays',              description: 'The spiritual transformation of Pierre.' },
//   // ── Săptămâna viitoare — câteva extra ──
//   { id: 133, ownerID: 34,  ownerUsername: 'Maria P.',  location: 'Cafe, Iași',           date: '2026-04-13T11:00:00', bookID: 57,  duration: 60,  rating: 4.4, titleEvent: 'Monday Habits',                description: 'Starting the week right with good habits.' },
//   { id: 134, ownerID: 88,  ownerUsername: 'Petra M.',  location: 'Bunt, Cluj-Napoca',    date: '2026-04-14T15:00:00', bookID: 300, duration: 90,  rating: 4.7, titleEvent: 'Karamazov Tuesday',            description: 'Ivan and his rebellion against God.' },
//   { id: 135, ownerID: 44,  ownerUsername: 'Bogdan S.', location: 'Everast, București',   date: '2026-04-15T17:00:00', bookID: 39,  duration: 75,  rating: 4.5, titleEvent: 'Raskolnikov Returns',          description: 'Re-reading the key confession scene.' },
//   {
//     id: 136,
//     ownerID: 146,
//     ownerUsername: 'Radu M.',
//     location: 'Bunt, Cluj-Napoca',
//     date: '2026-04-15T17:00:00',
//     bookID: 100,
//     duration: 75,
//     rating: 4.6,
//     titleEvent: 'Dune Book Club',
//     description: 'Discussing the first half of Dune — no spoilers beyond chapter 20!',
//   },
// ]
//
// // ── SessionStorage helpers ────────────────────────────────────
// // sessionStorage = RAM al tab-ului curent.
// // Se șterge automat când închizi browser-ul / tab-ul.
// // Respectă cerința "stored solely in RAM of the client machine".
//
// const SESSION_KEY_MEETUPS = 'commonplot_meetups'
// const SESSION_KEY_NEXT_ID = 'commonplot_next_id'
//
// function loadFromSession(): Meetup[] {
//   try {
//     const raw = sessionStorage.getItem(SESSION_KEY_MEETUPS)
//     return raw ? (JSON.parse(raw) as Meetup[]) : [...INITIAL_MEETUPS]
//   } catch {
//     return [...INITIAL_MEETUPS]
//   }
// }
//
// function loadNextId(): number {
//   try {
//     const raw = sessionStorage.getItem(SESSION_KEY_NEXT_ID)
//     return raw ? parseInt(raw, 10) : 200
//   } catch {
//     return 200
//   }
// }
//
// function saveToSession(data: Meetup[], id: number): void {
//   try {
//     sessionStorage.setItem(SESSION_KEY_MEETUPS, JSON.stringify(data))
//     sessionStorage.setItem(SESSION_KEY_NEXT_ID, String(id))
//   } catch {
//     // sessionStorage indisponibil — continuăm fără persistență
//   }
// }
//
// // ── Store ─────────────────────────────────────────────────────
// export const useMeetupsStore = defineStore('meetups', () => {
//
//   // ── State — inițializat din sessionStorage dacă există ──
//   const meetups = ref<Meetup[]>(loadFromSession())
//   const nextId  = ref<number>(loadNextId())
//
//   // ── Getters ──
//   const allMeetups = computed(() => meetups.value)
//
//   const totalCount = computed(() => meetups.value.length)
//
//   function getPage(page: number, pageSize: number): Meetup[] {
//     const start = (page - 1) * pageSize
//     return meetups.value.slice(start, start + pageSize)
//   }
//
//   function getById(id: number): Meetup | undefined {
//     return meetups.value.find(m => m.id === id)
//   }
//
//   // ── Actions ──
//
//   /**
//    * Adaugă un meet-up nou.
//    * @returns { success, errors, meetup }
//    */
//   function addMeetup(payload: CreateMeetupPayload) {
//     const errors = validateMeetup(payload)
//     if (hasErrors(errors)) return { success: false, errors, meetup: null }
//
//     const newMeetup: Meetup = { ...payload, id: nextId.value++ }
//     meetups.value.push(newMeetup)
//     saveToSession(meetups.value, nextId.value)     // ← persistă
//     return { success: true, errors: {}, meetup: newMeetup }
//   }
//
//   /**
//    * Actualizează un meet-up existent.
//    * @returns { success, errors }
//    */
//   function updateMeetup(payload: UpdateMeetupPayload) {
//     const errors = validateMeetup(payload)
//     if (hasErrors(errors)) return { success: false, errors }
//
//     const index = meetups.value.findIndex(m => m.id === payload.id)
//     if (index === -1) return { success: false, errors: { id: 'Meet-up not found.' } }
//
//     meetups.value[index] = { ...meetups.value[index], ...payload } as Meetup
//     saveToSession(meetups.value, nextId.value)     // ← persistă
//     return { success: true, errors: {} }
//   }
//
//   /**
//    * Șterge un meet-up după id.
//    * @returns { success }
//    */
//   function deleteMeetup(id: number) {
//     const index = meetups.value.findIndex(m => m.id === id)
//     if (index === -1) return { success: false }
//
//     meetups.value.splice(index, 1)
//     saveToSession(meetups.value, nextId.value)     // ← persistă
//     return { success: true }
//   }
//
//   /**
//    * Resetează la datele inițiale (util pentru teste / demo).
//    */
//   function resetToInitial() {
//     meetups.value = [...INITIAL_MEETUPS]
//     nextId.value = 200
//     saveToSession(meetups.value, nextId.value)
//   }
//
//   return {
//     meetups,
//     allMeetups,
//     totalCount,
//     getPage,
//     getById,
//     addMeetup,
//     updateMeetup,
//     deleteMeetup,
//     resetToInitial,
//   }
// })
// ============================================================
//  stores/meetups.ts
//  Pinia store conectat la backend Spring Boot.
//  Silver: offline support — dacă serverul e down,
//  operațiunile merg pe date locale și se sincronizează
//  când conexiunea revine.
// ============================================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Meetup, CreateMeetupPayload, UpdateMeetupPayload } from '../types/indexes.ts'
import { validateMeetup, hasErrors } from '@/utils/meetupValidation'
import * as api from '@/api/meetupApi'
import type { PagedResponse } from '@/api/meetupApi'

// ── Tipuri pentru offline queue (Silver) ──────────────────────
type OfflineOperation =
  | { type: 'create'; payload: CreateMeetupPayload }
  | { type: 'update'; id: number; payload: Partial<Meetup> }
  | { type: 'delete'; id: number }

export const useMeetupsStore = defineStore('meetups', () => {

  // ── State ─────────────────────────────────────────────────
  const meetups        = ref<Meetup[]>([])
  const totalElements  = ref(0)
  const totalPages     = ref(0)
  const currentPage    = ref(0)
  const loading        = ref(false)
  const error          = ref<string | null>(null)

  // Silver: offline support
  const isOnline       = ref(navigator.onLine)
  const offlineQueue   = ref<OfflineOperation[]>([])
  const offlineNextId  = ref(-1) // IDs negativi pentru entități offline

  // ── Getters ───────────────────────────────────────────────
  const allMeetups  = computed(() => meetups.value)
  const totalCount  = computed(() => totalElements.value)

  // ── Network monitoring (Silver) ───────────────────────────
  function initNetworkMonitoring() {
    window.addEventListener('online', async () => {
      isOnline.value = true
      await syncOfflineQueue()
    })
    window.addEventListener('offline', () => {
      isOnline.value = false
    })
  }

  // ── Sync offline queue când revine conexiunea (Silver) ────
  async function syncOfflineQueue() {
    if (!isOnline.value || offlineQueue.value.length === 0) return

    const queue = [...offlineQueue.value]
    offlineQueue.value = []

    for (const op of queue) {
      try {
        if (op.type === 'create') {
          await api.createMeetup(op.payload)
        } else if (op.type === 'update') {
          await api.updateMeetup(op.id, op.payload)
        } else if (op.type === 'delete') {
          await api.deleteMeetup(op.id)
        }
      } catch (e) {
        // Dacă sync eșuează, re-adaugă în queue
        offlineQueue.value.push(op)
      }
    }

    // Reîncarcă datele după sync
    await loadPage(currentPage.value)
  }

  // ══════════════════════════════════════════════════════════
  //  READ — paginare server-side
  // ══════════════════════════════════════════════════════════

  async function loadPage(page = 0, size = 10): Promise<void> {
    loading.value = true
    error.value   = null
    try {
      const response: PagedResponse<Meetup> = await api.fetchMeetups(page, size)
      meetups.value       = response.content
      totalElements.value = response.totalElements
      totalPages.value    = response.totalPages
      currentPage.value   = response.page
    } catch (e) {
      error.value = 'Could not connect to server. Working in offline mode.'
      isOnline.value = false
    } finally {
      loading.value = false
    }
  }

  function getById(id: number): Meetup | undefined {
    return meetups.value.find(m => m.id === id)
  }

  // ══════════════════════════════════════════════════════════
  //  CREATE
  // ══════════════════════════════════════════════════════════

  async function addMeetup(payload: CreateMeetupPayload) {
    const errors = validateMeetup(payload)
    if (hasErrors(errors)) return { success: false, errors, meetup: null }

    if (!isOnline.value) {
      // Offline: aggiungi localmente con ID negativo
      const tempId = offlineNextId.value--
      const tempMeetup: Meetup = { ...payload, id: tempId }
      meetups.value.push(tempMeetup)
      totalElements.value++
      offlineQueue.value.push({ type: 'create', payload })
      return { success: true, errors: {}, meetup: tempMeetup }
    }

    try {
      const created = await api.createMeetup(payload)
      meetups.value.push(created)
      totalElements.value++
      return { success: true, errors: {}, meetup: created }
    } catch (e: any) {
      const apiErrors = e?.errors ?? {}
      return { success: false, errors: apiErrors, meetup: null }
    }
  }

  // ══════════════════════════════════════════════════════════
  //  UPDATE
  // ══════════════════════════════════════════════════════════

  async function updateMeetup(payload: UpdateMeetupPayload) {
    const errors = validateMeetup(payload)
    if (hasErrors(errors)) return { success: false, errors }

    const index = meetups.value.findIndex(m => m.id === payload.id)
    if (index === -1) return { success: false, errors: { id: 'Meet-up not found.' } }

    if (!isOnline.value) {
      // Offline: aggiorna localmente
      meetups.value[index] = { ...meetups.value[index], ...payload } as Meetup
      offlineQueue.value.push({ type: 'update', id: payload.id, payload })
      return { success: true, errors: {} }
    }

    try {
      const updated = await api.updateMeetup(payload.id, payload)
      meetups.value[index] = updated
      return { success: true, errors: {} }
    } catch (e: any) {
      return { success: false, errors: e?.errors ?? {} }
    }
  }

  // ══════════════════════════════════════════════════════════
  //  DELETE
  // ══════════════════════════════════════════════════════════

  async function deleteMeetup(id: number) {
    if (!isOnline.value) {
      // Offline: rimuovi localmente
      meetups.value = meetups.value.filter(m => m.id !== id)
      totalElements.value--
      offlineQueue.value.push({ type: 'delete', id })
      return { success: true }
    }

    try {
      await api.deleteMeetup(id)
      meetups.value = meetups.value.filter(m => m.id !== id)
      totalElements.value--
      return { success: true }
    } catch {
      return { success: false }
    }
  }

  // ── Getters pentru paginare (compatibile cu MeetupsView) ──
  function getPage(page: number, pageSize: number): Meetup[] {
    return meetups.value
  }

  return {
    meetups,
    totalElements,
    totalPages,
    currentPage,
    loading,
    error,
    isOnline,
    offlineQueue,
    allMeetups,
    totalCount,
    loadPage,
    getById,
    getPage,
    addMeetup,
    updateMeetup,
    deleteMeetup,
    initNetworkMonitoring,
    syncOfflineQueue,
  }
})
