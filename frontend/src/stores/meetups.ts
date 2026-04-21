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
    // Verifică serverul la fiecare 5 secunde
    setInterval(async () => {
      try {
        await fetch('http://localhost:8080/api/meetups/stats/count', {
          signal: AbortSignal.timeout(3000) // timeout 3s
        })

        // Serverul e disponibil
        if (!isOnline.value) {
          // Tocmai a revenit online
          isOnline.value = true
          error.value = null

          if (offlineQueue.value.length > 0) {
            await syncOfflineQueue()
          }

          await loadPage(currentPage.value)
        }
      } catch {
        // Serverul nu e disponibil
        if (isOnline.value) {
          isOnline.value = false
          error.value = 'Server unreachable. Working offline.'
        }
      }
    }, 5000)
  }

  // ── Sync offline queue când revine conexiunea (Silver) ────
  async function syncOfflineQueue() {
    if (!isOnline.value || offlineQueue.value.length === 0) return

    const queue = [...offlineQueue.value]
    offlineQueue.value = []          // curăță înainte să înceapă

    for (const op of queue) {
      try {
        if (op.type === 'create')      await api.createMeetup(op.payload)
        else if (op.type === 'update') await api.updateMeetup(op.id, op.payload)
        else if (op.type === 'delete') await api.deleteMeetup(op.id)
      } catch {
        offlineQueue.value.push(op)   // re-adaugă dacă eșuează
      }
    }
    // Reîncarcă datele după sync complet
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
