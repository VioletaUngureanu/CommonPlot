
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMeetupsStore } from '@/stores/meetups'
import { validateMeetup, hasErrors } from '@/utils/meetupValidation'
import type { CreateMeetupPayload } from '@/types/indexes.ts'


const validPayload: CreateMeetupPayload = {
  ownerID: 1,
  ownerUsername: 'testuser',
  location: 'Cafe Central, Cluj',
  date: '2026-06-01T10:00:00',
  bookID: 42,
  duration: 20,
  rating: 4.5,
  titleEvent: 'Test Book Club',
  description: 'A test meet-up description.',
}

describe('validateMeetup()', () => {

  it('returnează fără erori pentru un payload valid', () => {
    const errors = validateMeetup(validPayload)
    expect(hasErrors(errors)).toBe(false)
  })

  it('eroare dacă titleEvent lipsește', () => {
    const errors = validateMeetup({ ...validPayload, titleEvent: '' })
    expect(errors.titleEvent).toBeDefined()
  })

  it('eroare dacă titleEvent e prea scurt', () => {
    const errors = validateMeetup({ ...validPayload, titleEvent: 'AB' })
    expect(errors.titleEvent).toBeDefined()
  })

  it('eroare dacă titleEvent e prea lung', () => {
    const errors = validateMeetup({ ...validPayload, titleEvent: 'A'.repeat(151) })
    expect(errors.titleEvent).toBeDefined()
  })

  it('eroare dacă location lipsește', () => {
    const errors = validateMeetup({ ...validPayload, location: '' })
    expect(errors.location).toBeDefined()
  })

  it('eroare dacă location e prea scurtă', () => {
    const errors = validateMeetup({ ...validPayload, location: 'AB' })
    expect(errors.location).toBeDefined()
  })

  it('eroare dacă date lipsește', () => {
    const errors = validateMeetup({ ...validPayload, date: '' })
    expect(errors.date).toBeDefined()
  })

  it('eroare dacă date e invalidă', () => {
    const errors = validateMeetup({ ...validPayload, date: 'not-a-date' })
    expect(errors.date).toBeDefined()
  })

  it('eroare dacă bookID e 0', () => {
    const errors = validateMeetup({ ...validPayload, bookID: 0 })
    expect(errors.bookID).toBeDefined()
  })

  it('eroare dacă bookID e negativ', () => {
    const errors = validateMeetup({ ...validPayload, bookID: -5 })
    expect(errors.bookID).toBeDefined()
  })

  it('eroare dacă ownerID lipsește', () => {
    const errors = validateMeetup({ ...validPayload, ownerID: 0 })
    expect(errors.ownerID).toBeDefined()
  })

  it('eroare dacă duration e sub minim', () => {
    const errors = validateMeetup({ ...validPayload, duration: 10 })
    expect(errors.duration).toBeDefined()
  })

  it('eroare dacă duration depășește maximul', () => {
    const errors = validateMeetup({ ...validPayload, duration: 600 })
    expect(errors.duration).toBeDefined()
  })

  it('eroare dacă rating e în afara intervalului', () => {
    const errors = validateMeetup({ ...validPayload, rating: 6 })
    expect(errors.rating).toBeDefined()
  })

  it('eroare dacă description depășește 500 caractere', () => {
    const errors = validateMeetup({ ...validPayload, description: 'A'.repeat(501) })
    expect(errors.description).toBeDefined()
  })

  it('fără eroare dacă description lipsește (câmp opțional)', () => {
    const errors = validateMeetup({ ...validPayload, description: '' })
    expect(errors.description).toBeUndefined()
  })
})


describe('useMeetupsStore()', () => {

  beforeEach(() => {
    setActivePinia(createPinia())
  })


  it('are date inițiale (mock)', () => {
    const store = useMeetupsStore()
    expect(store.allMeetups.length).toBeGreaterThan(0)
  })

  it('totalCount reflectă numărul de meet-up-uri', () => {
    const store = useMeetupsStore()
    expect(store.totalCount).toBe(store.allMeetups.length)
  })


  it('getById returnează undefined pentru id inexistent', () => {
    const store = useMeetupsStore()
    expect(store.getById(99999)).toBeUndefined()
  })

  // ── getPage ──
  it('getPage returnează numărul corect de elemente', () => {
    const store = useMeetupsStore()
    const page = store.getPage(1, 3)
    expect(page.length).toBe(3)
  })




  it('adaugă un meet-up valid', () => {
    const store = useMeetupsStore()
    const before = store.totalCount
    const result = store.addMeetup(validPayload)
    expect(result.success).toBe(true)
    expect(store.totalCount).toBe(before + 1)
  })

  it('meet-up-ul adăugat are un ID unic', () => {
    const store = useMeetupsStore()
    const r1 = store.addMeetup(validPayload)
    const r2 = store.addMeetup(validPayload)
    expect(r1.meetup!.id).not.toBe(r2.meetup!.id)
  })

  it('nu adaugă meet-up cu payload invalid', () => {
    const store = useMeetupsStore()
    const before = store.totalCount
    const result = store.addMeetup({ ...validPayload, titleEvent: '' })
    expect(result.success).toBe(false)
    expect(store.totalCount).toBe(before)
  })

  it('returnează erori când payload-ul e invalid', () => {
    const store = useMeetupsStore()
    const result = store.addMeetup({ ...validPayload, location: '' })
    expect(hasErrors(result.errors)).toBe(true)
  })


  it('nu actualizează dacă id-ul nu există', () => {
    const store = useMeetupsStore()
    const result = store.updateMeetup({ id: 99999, location: 'Ghost' })
    expect(result.success).toBe(false)
  })




  it('returnează false când se șterge un id inexistent', () => {
    const store = useMeetupsStore()
    const result = store.deleteMeetup(99999)
    expect(result.success).toBe(false)
  })
})
