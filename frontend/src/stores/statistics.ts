// ============================================================
//  stores/statistics.ts
//  Date pentru statistici derivate din meetups store.
//  Include un "thread" simulat (setInterval) care adaugă
//  meetup-uri automat — poate fi pornit/oprit din UI.
// ============================================================

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useMeetupsStore } from '@/stores/meetups'
import type { CreateMeetupPayload } from '../types/indexes.ts'
import { BACKEND_URL } from '@/config'


export const DAYS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
const COLORS = ['#D4A017', '#8B1A2F', '#bf6280', '#7a1628', '#d490a4']

// Date mock pentru generatorul automat
const MOCK_BOOKS = [
  { id: 39,  title: 'Crime and Punishment',    author: 'Fyodor Dostoevsky' },
  { id: 57,  title: 'Atomic Habits',           author: 'James Clear'       },
  { id: 101, title: 'The Trial',               author: 'Franz Kafka'       },
  { id: 120, title: 'War and Peace',           author: 'Leo Tolstoy'       },
  { id: 300, title: 'The Brothers Karamazov',  author: 'Fyodor Dostoevsky' },
]

const MOCK_LOCATIONS = [
  'Bunt, Cluj-Napoca',
  'Cafe, Iași',
  'Everast, București',
  'Hub, București',
  'Meron, Cluj-Napoca',
]

const MOCK_USERNAMES = ['Alex M.', 'Maria P.', 'Ionut B.', 'Elena D.', 'Andrei V.', 'Diana F.']

function randomItem<T>(arr: T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]!
}

// Generează o dată viitoare aleatoare (între 1 și 30 zile de azi)
function randomFutureDate(): string {
  const d = new Date()
  d.setDate(d.getDate() + Math.floor(Math.random() * 30) + 1)
  d.setHours(Math.floor(Math.random() * 12) + 8)
  d.setMinutes(0, 0, 0)
  return d.toISOString().slice(0, 16)
}

// Ziua săptămânii din ISO string (0 = Luni … 6 = Duminică)
function getDayIndex(isoDate: string): number {
  const d = new Date(isoDate)
  return (d.getDay() + 6) % 7
}

export const useStatisticsStore = defineStore('statistics', () => {
  const meetupsStore = useMeetupsStore()

  // ── Thread state ──────────────────────────────────────────
  const threadRunning  = ref(false)
  const threadSpeed    = ref(2000) // ms
  const addedByThread  = ref(0)

  // ── Backend generator (Silver) ────────────────────────────
  async function startThread() {
    if (threadRunning.value) return
    try {
      const intervalSeconds = Math.round(threadSpeed.value / 1000)

      await fetch(`${BACKEND_URL}/api/meetups/generator/start?interval=${intervalSeconds}`, { method: 'POST' })

      threadRunning.value = true
    } catch {
      console.error('Could not start generator — is the backend running?')
    }
  }

  async function stopThread() {
    try {
      await fetch(`${BACKEND_URL}/api/meetups/generator/stop`, { method: 'POST' })
    } catch { /* ignore */ }
    threadRunning.value = false
  }

  async function toggleThread() {
    threadRunning.value ? await stopThread() : await startThread()
  }

  async function setSpeed(ms: number) {
    threadSpeed.value = ms
    if (threadRunning.value) {
      await stopThread()
      await startThread()
    }
  }

  // ── Most Discussed Books ──────────────────────────────────
  const booksByDay = computed(() => {
    const map: Record<number, { title: string; author: string; counts: number[] }> = {}

    for (const m of meetupsStore.allMeetups) {
      if (!map[m.bookID]) {
        map[m.bookID] = {
          title:  m.bookTitle  ?? `Book #${m.bookID}`,
          author: m.bookAuthor ?? 'Unknown Author',
          counts: [0, 0, 0, 0, 0, 0, 0],
        }
      }
      const entry  = map[m.bookID]!
      const dayIdx = getDayIndex(m.date)
      entry.counts[dayIdx] = (entry.counts[dayIdx] ?? 0) + 1
    }

    return Object.entries(map).map(([id, data]) => ({
      bookID: Number(id),
      title:  data.title,
      author: data.author,
      counts: data.counts,
      total:  data.counts.reduce((a, b) => a + b, 0),
    }))
  })

  const topBooks = computed(() => {
    const sorted = [...booksByDay.value].sort((a, b) => b.total - a.total)
    const total  = sorted.reduce((s, b) => s + b.total, 0)
    return sorted.slice(0, 5).map(b => ({
      ...b,
      percent: total > 0 ? Math.round((b.total / total) * 100) : 0,
    }))
  })

  const barChartData = computed(() => ({
    labels: DAYS,
    datasets: topBooks.value.map((book, i) => ({
      label:           book.title,
      data:            book.counts,
      backgroundColor: COLORS[i % COLORS.length],
      borderRadius:    3,
      borderSkipped:   false,
    })),
  }))

  // ── Active Cities ─────────────────────────────────────────
  const activeCities = computed(() => {
    const map: Record<string, number> = {}
    for (const m of meetupsStore.allMeetups) {
      const parts = m.location.split(',')
      const city  = (parts[parts.length - 1] ?? parts[0]).trim()
      map[city] = (map[city] ?? 0) + 1
    }
    return Object.entries(map)
      .map(([city, count]) => ({ city, count }))
      .sort((a, b) => b.count - a.count)
  })

  const citiesChartData = computed(() => ({
    labels: activeCities.value.map(c => c.city),
    datasets: [{
      label:           'Meet-ups',
      data:            activeCities.value.map(c => c.count),
      backgroundColor: '#D4A017',
      borderRadius:    3,
    }],
  }))

  // ── Popular Locations ─────────────────────────────────────
  const popularLocations = computed(() => {
    const map: Record<string, number> = {}
    for (const m of meetupsStore.allMeetups) {
      map[m.location] = (map[m.location] ?? 0) + 1
    }
    return Object.entries(map)
      .map(([location, count]) => ({ location, count }))
      .sort((a, b) => b.count - a.count)
  })

  const locationsChartData = computed(() => ({
    labels: popularLocations.value.map(l => l.location),
    datasets: [{
      label:           'Meet-ups',
      data:            popularLocations.value.map(l => l.count),
      backgroundColor: '#8B1A2F',
      borderRadius:    3,
    }],
  }))

  return {
    DAYS,
    booksByDay,
    topBooks,
    barChartData,
    activeCities,
    citiesChartData,
    popularLocations,
    locationsChartData,
    // thread
    threadRunning,
    threadSpeed,
    addedByThread,
    toggleThread,
    setSpeed,
    stopThread,
  }
})
