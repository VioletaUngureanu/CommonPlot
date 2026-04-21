<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchMeetupsByBook, fetchMeetupsStatsByBook } from '@/api/bookApi'
import type { Meetup } from '../types/indexes.ts'

const route  = useRoute()
const router = useRouter()

const bookId   = Number(route.params.bookId)
const meetups  = ref<Meetup[]>([])
const stats    = ref<{ totalMeetups: number; avgRating: number; avgDuration: number } | null>(null)
const loading  = ref(true)
const error    = ref<string | null>(null)

onMounted(async () => {
  try {
    const [meetupsData, statsData] = await Promise.all([
      fetchMeetupsByBook(bookId),
      fetchMeetupsStatsByBook(bookId),
    ])
    meetups.value = meetupsData
    stats.value   = statsData
  } catch (e) {
    error.value = 'Could not load meetups for this book.'
  } finally {
    loading.value = false
  }
})

const formatDate = (iso: string) => {
  return new Date(iso).toLocaleDateString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}
</script>

<template>
  <div class="book-meetups">

    <!-- Header -->
    <div class="bm-header">
      <button class="back-btn" @click="router.back()">← Back</button>
      <h1 class="bm-title">Meetups for Book #{{ bookId }}</h1>
    </div>

    <!-- Stats -->
    <div v-if="stats" class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalMeetups }}</span>
        <span class="stat-label">Total Meetups</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.avgRating }}/5</span>
        <span class="stat-label">Avg Rating</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.avgDuration }}min</span>
        <span class="stat-label">Avg Duration</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="bm-loading">Loading meetups...</div>

    <!-- Error -->
    <div v-else-if="error" class="bm-error">{{ error }}</div>

    <!-- Empty -->
    <div v-else-if="meetups.length === 0" class="bm-empty">
      No meetups found for this book.
    </div>

    <!-- Table -->
    <div v-else class="bm-table-wrap">
      <table class="bm-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Event Title</th>
          <th>Location</th>
          <th>Date</th>
          <th>Duration</th>
          <th>Rating</th>
          <th>Owner</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="m in meetups" :key="m.id">
          <td>{{ m.id }}</td>
          <td class="td-title">{{ m.titleEvent }}</td>
          <td>{{ m.location }}</td>
          <td>{{ formatDate(m.date) }}</td>
          <td>{{ m.duration }}min</td>
          <td>
            <span class="rating-badge">{{ m.rating }}/5</span>
          </td>
          <td>{{ m.ownerUsername }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.book-meetups {
  flex: 1;
  padding: var(--space-5) 1.75rem;
  background: var(--color-bg-muted);
  overflow-y: auto;
  font-family: var(--font-serif);
}

.bm-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
.back-btn {
  background: transparent;
  border: 1.5px solid var(--color-border);
  color: var(--color-primary);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.back-btn:hover { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }

.bm-title {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}

/* Stats */
.stats-row {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
.stat-card {
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
  min-width: 120px;
}
.stat-value {
  font-family: var(--font-serif);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-primary);
}
.stat-label {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

/* States */
.bm-loading, .bm-empty {
  text-align: center;
  padding: var(--space-12);
  font-family: var(--font-sans);
  color: var(--cream-600);
  font-style: italic;
}
.bm-error {
  text-align: center;
  padding: var(--space-6);
  color: var(--color-error);
  font-family: var(--font-sans);
}

/* Table */
.bm-table-wrap { overflow-x: auto; }
.bm-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.bm-table thead tr { background: var(--color-accent); }
.bm-table th {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--color-text);
  padding: var(--space-3) var(--space-4);
  text-align: left;
}
.bm-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}
.bm-table tbody tr:hover { background: var(--color-bg-muted); }
.bm-table td {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--color-text);
  padding: 0.7rem var(--space-4);
}
.td-title { font-weight: 600; color: var(--burgundy-700); }
.rating-badge {
  background: var(--color-bg-muted);
  color: var(--color-primary);
  font-weight: 600;
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
}
</style>
