<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchBookById,
  fetchMeetupsByBook,
  fetchMeetupsStatsByBook,
  type Book
} from '../api/bookApi'
import type { Meetup } from '../types/indexes.ts'

const route  = useRoute()
const router = useRouter()

const bookId = Number(route.params.bookId)

// ── State ─────────────────────────────────────────────────────
const book    = ref<Book | null>(null)
const meetups = ref<Meetup[]>([])
const stats   = ref<{
  totalMeetups: number
  avgRating: number
  avgDuration: number
} | null>(null)
const loading = ref(true)
const error   = ref<string | null>(null)

// ── Load ──────────────────────────────────────────────────────
onMounted(async () => {
  try {
    const [bookData, meetupsData, statsData] = await Promise.all([
      fetchBookById(bookId),
      fetchMeetupsByBook(bookId),
      fetchMeetupsStatsByBook(bookId),
    ])
    book.value    = bookData
    meetups.value = meetupsData
    stats.value   = statsData
  } catch {
    error.value = 'Could not load data for this book.'
  } finally {
    loading.value = false
  }
})

// ── Format ────────────────────────────────────────────────────
const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
</script>

<template>
  <div class="bm-page">

    <!-- Header -->
    <div class="topbar">
      <button class="back-btn" @click="router.push({ name: 'books' })">
        ← Books
      </button>
      <h1 class="topbar__title">
        {{ book ? book.title : `Book #${bookId}` }}
      </h1>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-bar" />

    <!-- Error -->
    <div v-else-if="error" class="error-banner">{{ error }}</div>

    <div v-else class="bm-content">

      <!-- Book info + stats -->
      <div class="book-info-row">

        <!-- Cover + details -->
        <div class="book-info-card">
          <div class="book-cover">
            <img
              v-if="book?.coverUrl"
              :src="book.coverUrl"
              :alt="book.title"
              class="book-cover__img"
            />
            <div v-else class="book-cover__placeholder">
              <svg width="48" height="64" viewBox="0 0 48 64" fill="none">
                <rect width="48" height="64" rx="4" fill="#8B1A2F" opacity=".15"/>
                <rect x="6" y="12" width="36" height="2" rx="1" fill="#8B1A2F" opacity=".3"/>
                <rect x="6" y="18" width="24" height="2" rx="1" fill="#8B1A2F" opacity=".2"/>
              </svg>
            </div>
          </div>
          <div class="book-details">
            <h2 class="book-details__title">{{ book?.title }}</h2>
            <p class="book-details__author">{{ book?.author }}</p>
            <p v-if="book?.description" class="book-details__desc">{{ book.description }}</p>
          </div>
        </div>

        <!-- Stats cards -->
        <div class="stats-row" v-if="stats">
          <div class="stat-card">
            <span class="stat-card__value">{{ stats.totalMeetups }}</span>
            <span class="stat-card__label">Total Meet-ups</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__value">{{ stats.avgRating }}/5</span>
            <span class="stat-card__label">Avg Rating</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__value">{{ stats.avgDuration }}min</span>
            <span class="stat-card__label">Avg Duration</span>
          </div>
        </div>
      </div>

      <!-- Meetups table -->
      <div class="table-section">
        <h2 class="section-title">Meet-ups for this book</h2>

        <div v-if="meetups.length === 0" class="empty-state">
          No meet-ups scheduled for this book yet.
        </div>

        <table v-else class="meetup-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Location</th>
            <th>Date</th>
            <th>Duration</th>
            <th>Owner</th>
            <th>Rating</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="m in meetups" :key="m.id">
            <td class="td-id">{{ m.id }}</td>
            <td class="td-title">{{ m.titleEvent }}</td>
            <td>{{ m.location }}</td>
            <td>{{ formatDate(m.date) }}</td>
            <td>{{ m.duration }} min</td>
            <td>{{ m.ownerUsername }}</td>
            <td>
              <span class="rating-badge">{{ m.rating }}/5</span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>
</template>

<style scoped>
.bm-page {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; background: var(--color-bg-muted); font-family: var(--font-serif);
}

/* Topbar */
.topbar {
  display: flex; align-items: center; gap: var(--space-4);
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft); border-bottom: 1px solid var(--color-border);
}
.back-btn {
  background: transparent; border: 1.5px solid var(--color-border);
  color: var(--color-primary); font-family: var(--font-sans);
  font-size: var(--text-sm); font-weight: 600;
  padding: var(--space-1) var(--space-3); border-radius: var(--radius-sm);
  cursor: pointer; transition: all var(--transition-fast);
  white-space: nowrap;
}
.back-btn:hover { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.topbar__title {
  font-size: var(--text-lg); font-weight: 700; color: var(--color-text);
  margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* Loading / Error */
.loading-bar {
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  animation: loading 1s ease-in-out infinite;
}
@keyframes loading {
  0%   { transform: scaleX(0); transform-origin: left; }
  50%  { transform: scaleX(1); transform-origin: left; }
  51%  { transform: scaleX(1); transform-origin: right; }
  100% { transform: scaleX(0); transform-origin: right; }
}
.error-banner {
  padding: var(--space-4) var(--space-6); background: #fdecea;
  color: var(--color-error); font-family: var(--font-sans); font-size: var(--text-sm);
}

/* Content */
.bm-content {
  flex: 1; overflow-y: auto;
  padding: var(--space-5) 1.75rem;
  display: flex; flex-direction: column; gap: var(--space-5);
}

/* Book info row */
.book-info-row {
  display: flex; gap: var(--space-5); align-items: flex-start; flex-wrap: wrap;
}

.book-info-card {
  display: flex; gap: var(--space-4); align-items: flex-start;
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-4); box-shadow: var(--shadow-sm);
  flex: 1; min-width: 280px;
}

.book-cover {
  width: 80px; flex-shrink: 0;
}
.book-cover__img {
  width: 80px; height: 110px; object-fit: cover;
  border-radius: var(--radius-sm); box-shadow: var(--shadow-sm);
}
.book-cover__placeholder {
  width: 80px; height: 110px; background: var(--color-bg-muted);
  border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center;
}

.book-details__title {
  font-family: var(--font-serif); font-size: var(--text-lg);
  font-weight: 700; color: var(--color-text); margin: 0 0 var(--space-1);
}
.book-details__author {
  font-family: var(--font-sans); font-size: var(--text-sm);
  font-weight: 600; color: var(--burgundy-700); margin: 0 0 var(--space-2);
}
.book-details__desc {
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text-muted); line-height: 1.5; margin: 0;
}

/* Stats */
.stats-row { display: flex; gap: var(--space-3); flex-wrap: wrap; }
.stat-card {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  display: flex; flex-direction: column; align-items: center; gap: var(--space-1);
  box-shadow: var(--shadow-sm); min-width: 110px;
}
.stat-card__value {
  font-family: var(--font-serif); font-size: var(--text-2xl);
  font-weight: 700; color: var(--color-primary);
}
.stat-card__label {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.06em;
}

/* Table section */
.table-section { display: flex; flex-direction: column; gap: var(--space-3); }
.section-title {
  font-size: var(--text-base); font-weight: 700; color: var(--color-text); margin: 0;
}

.empty-state {
  text-align: center; padding: var(--space-10);
  font-family: var(--font-sans); color: var(--cream-600); font-style: italic;
}

.meetup-table {
  width: 100%; border-collapse: collapse;
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm);
}
.meetup-table thead tr { background: var(--color-accent); }
.meetup-table th {
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.07em;
  color: var(--color-text); padding: var(--space-3) var(--space-4); text-align: left;
}
.meetup-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}
.meetup-table tbody tr:hover { background: var(--color-bg-muted); }
.meetup-table td {
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); padding: 0.7rem var(--space-4);
}
.td-id    { color: var(--cream-600); font-size: var(--text-xs); }
.td-title { font-weight: 600; color: var(--burgundy-700); }

.rating-badge {
  background: var(--color-bg-muted); color: var(--color-primary);
  font-weight: 600; font-size: var(--text-xs);
  padding: 2px var(--space-2); border-radius: var(--radius-sm);
}
</style>
