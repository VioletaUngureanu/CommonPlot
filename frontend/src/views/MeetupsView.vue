<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMeetupsStore } from '@/stores/meetups'
import type { Meetup, CreateMeetupPayload } from '../types/indexes.ts'
import MeetupModal from '@/components/MeetupModal.vue'

const store = useMeetupsStore()

// ── Paginare server-side ──────────────────────────────────────
const PAGE_SIZE = 10

onMounted(async () => {
  store.initNetworkMonitoring()
  await store.loadPage(0, PAGE_SIZE)
})

const totalPages = computed(() => store.totalPages)
const currentPage = computed(() => store.currentPage + 1)

const goToPage = async (p: number) => {
  if (p >= 1 && p <= totalPages.value) {
    await store.loadPage(p - 1, PAGE_SIZE)
  }
}

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, currentPage.value - 1)
  const end   = Math.min(totalPages.value, start + 3)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// ── Search ────────────────────────────────────────────────────
const searchQuery = ref('')

const filteredMeetups = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return store.allMeetups
  return store.allMeetups.filter(m =>
    m.titleEvent.toLowerCase().includes(q) ||
    m.location.toLowerCase().includes(q) ||
    m.ownerUsername.toLowerCase().includes(q)
  )
})

// ── Detail panel ──────────────────────────────────────────────
const selectedMeetup = ref<Meetup | null>(null)

const selectMeetup = (m: Meetup) => {
  selectedMeetup.value = selectedMeetup.value?.id === m.id ? null : m
}

// ── Modal ─────────────────────────────────────────────────────
const modalVisible = ref(false)
const modalMeetup  = ref<Meetup | null>(null)

const openAdd  = () => { modalMeetup.value = null; modalVisible.value = true }
const openEdit = (m: Meetup) => { modalMeetup.value = m; modalVisible.value = true }
const closeModal = () => { modalVisible.value = false }

const handleSave = async (payload: CreateMeetupPayload | (Partial<Meetup> & { id: number })) => {
  if ('id' in payload) {
    await store.updateMeetup(payload)
  } else {
    await store.addMeetup(payload as CreateMeetupPayload)
  }
  closeModal()
  await store.loadPage(store.currentPage, PAGE_SIZE)
}

// ── Delete ────────────────────────────────────────────────────
const confirmDeleteId = ref<number | null>(null)

const requestDelete = (id: number) => { confirmDeleteId.value = id }
const cancelDelete  = () => { confirmDeleteId.value = null }

const confirmDelete = async () => {
  if (confirmDeleteId.value === null) return
  if (selectedMeetup.value?.id === confirmDeleteId.value) selectedMeetup.value = null
  await store.deleteMeetup(confirmDeleteId.value)
  confirmDeleteId.value = null
  await store.loadPage(store.currentPage, PAGE_SIZE)
}

// ── Formatare dată ────────────────────────────────────────────
const formatDate = (iso: string) => {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleDateString('ro-RO', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const formatDateTime = (iso: string) => {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<template>
  <div class="main">
    <div v-if="!store.isOnline" class="offline-banner">
      ⚠️ Offline — Changes will sync when connection is restored.
      <span v-if="store.offlineQueue.length > 0">({{ store.offlineQueue.length }} pending)</span>
    </div>

    <div v-if="store.loading" class="loading-bar" />

    <div class="topbar">
      <h1 class="topbar__title">Admin: Meet-ups Management</h1>
      <div class="topbar__actions">
        <div class="search-wrap">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <circle cx="6" cy="6" r="4.5" stroke="#8B1A2F" stroke-width="1.5"/>
            <line x1="9.5" y1="9.5" x2="13" y2="13" stroke="#8B1A2F" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <input v-model="searchQuery" type="text" placeholder="Search" class="search-input"/>
        </div>
        <button class="btn-new" @click="openAdd">+ New Meet-up</button>
      </div>
    </div>

    <div class="content" :class="{ 'has-detail': selectedMeetup }">
      <div class="table-wrap">
        <table class="meetup-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>OwnerID</th>
            <th>Location</th>
            <th>Date</th>
            <th>BookID</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="m in filteredMeetups"
            :key="m.id"
            :class="{ selected: selectedMeetup?.id === m.id }"
            @click="selectMeetup(m)"
          >
            <td>{{ m.id }}</td>
            <td>{{ m.ownerID }}</td>
            <td>{{ m.location }}</td>
            <td>{{ formatDate(m.date) }}</td>
            <td>{{ m.bookID }}</td>
            <td class="actions-cell" @click.stop>
              <button class="action-btn action-btn--edit" @click="openEdit(m)">✎</button>
              <button class="action-btn action-btn--delete" @click="requestDelete(m.id)">🗑</button>
            </td>
          </tr>
          <tr v-if="filteredMeetups.length === 0">
            <td colspan="6" class="empty-state">No meet-ups found.</td>
          </tr>
          </tbody>
        </table>

        <div class="pagination" v-if="!searchQuery">
          <button class="pg-btn" @click="goToPage(1)" :disabled="currentPage === 1">«</button>
          <button class="pg-btn" v-for="p in visiblePages" :key="p" :class="{ active: p === currentPage }" @click="goToPage(p)">{{ p }}</button>
          <button class="pg-btn" @click="goToPage(totalPages)" :disabled="currentPage === totalPages">»</button>
        </div>
      </div>

      <Transition name="slide">
        <div v-if="selectedMeetup" class="detail-panel">
          <div class="detail-panel__header">
            <h3>Details #{{ selectedMeetup.id }}</h3>
            <button @click="selectedMeetup = null">✕</button>
          </div>
          <div class="detail-panel__body">
            <p><strong>Event:</strong> {{ selectedMeetup.titleEvent }}</p>
            <p><strong>Date:</strong> {{ formatDateTime(selectedMeetup.date) }}</p>
            <p><strong>Desc:</strong> {{ selectedMeetup.description }}</p>
          </div>
        </div>
      </Transition>
    </div>

    <Teleport to="body">
      <Transition name="modal">
        <div v-if="confirmDeleteId !== null" class="confirm-backdrop" @click.self="cancelDelete">
          <div class="confirm-box">
            <h3>Delete #{{ confirmDeleteId }}?</h3>
            <div class="confirm-actions">
              <button @click="cancelDelete">Cancel</button>
              <button @click="confirmDelete">Delete</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <MeetupModal
      :visible="modalVisible"
      :meetup="modalMeetup"
      @close="closeModal"
      @save="handleSave"
    />
  </div>
</template>

<style scoped>
/* ── Offline banner ── */
.offline-banner {
  background: #f39c12;
  color: white;
  text-align: center;
  padding: var(--space-2) var(--space-4);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 600;
}

/* ── Loading bar ── */
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

/* ── Main ── */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-muted);
  font-family: var(--font-serif);
}

/* ── Topbar ── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft);
  border-bottom: 1px solid var(--color-border);
}
.topbar__title {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
}
.topbar__actions { display: flex; align-items: center; gap: var(--space-3); }

.search-wrap {
  display: flex; align-items: center; gap: var(--space-2);
  background: var(--color-bg-soft);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
}
.search-input {
  border: none; background: transparent;
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); outline: none; width: 140px;
}
.search-input::placeholder { color: var(--cream-600); }

/* btn-new folosește clasa globală btn-primary din main.css */
.btn-new {
  background: var(--color-primary); color: var(--color-text-inverse);
  border: none; border-radius: var(--radius-sm);
  font-family: var(--font-sans); font-size: var(--text-xs);
  font-weight: 600; letter-spacing: 0.05em;
  padding: var(--space-2) var(--space-4); cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-new:hover { background: var(--color-primary-hover); }

/* ── Content area ── */
.content {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr;
  transition: grid-template-columns var(--transition-normal);
  overflow: hidden;
}
.content.has-detail { grid-template-columns: 1fr 300px; }

/* ── Table ── */
.table-wrap {
  padding: var(--space-5) 1.75rem;
  overflow-y: auto;
  display: flex; flex-direction: column;
}

.meetup-table {
  width: 100%; border-collapse: collapse;
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg); overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.meetup-table thead tr { background: var(--color-accent); }
.meetup-table th {
  font-family: var(--font-sans);
  font-size: var(--text-xs); font-weight: 700;
  letter-spacing: 0.07em; text-transform: uppercase;
  color: var(--color-text); padding: var(--space-3) var(--space-4); text-align: left;
}

.meetup-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  cursor: pointer; transition: background var(--transition-fast);
}
.meetup-table tbody tr:hover    { background: var(--color-bg-muted); }
.meetup-table tbody tr.selected { background: var(--burgundy-200); }

.meetup-table td {
  font-family: var(--font-sans);
  font-size: var(--text-sm); color: var(--color-text);
  padding: 0.7rem var(--space-4);
}

.actions-cell { display: flex; gap: var(--space-2); align-items: center; }

.action-btn {
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-soft); color: var(--color-primary);
  cursor: pointer; transition: background var(--transition-fast), color var(--transition-fast);
}
.action-btn:hover               { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.action-btn--delete:hover       { background: var(--color-error);   color: var(--color-text-inverse); border-color: var(--color-error); }

.empty-state {
  text-align: center; padding: var(--space-12);
  color: var(--cream-600); font-family: var(--font-sans);
  font-size: var(--text-base); font-style: italic;
}

/* ── Pagination ── */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: var(--space-1); padding: var(--space-4) 0 0;
}
.pg-btn {
  min-width: 28px; height: 28px; padding: 0 var(--space-2);
  border-radius: var(--radius-sm); border: 1.5px solid var(--color-border);
  background: var(--color-bg-soft); color: var(--color-text);
  font-family: var(--font-sans); font-size: var(--text-xs);
  cursor: pointer; transition: all var(--transition-fast);
}
.pg-btn:hover:not(:disabled) { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.pg-btn.active               { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.pg-btn:disabled             { opacity: 0.35; cursor: default; }

/* ── Detail panel ── */
.detail-panel {
  background: var(--color-bg-soft);
  border-left: 1px solid var(--color-border);
  display: flex; flex-direction: column;
  overflow-y: auto;
}
.detail-panel__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
}
.detail-panel__header h3 {
  font-size: var(--text-base); font-weight: 700; color: var(--color-text);
}
.detail-panel__close {
  background: transparent; border: none;
  color: var(--color-primary); font-size: var(--text-sm);
  cursor: pointer; opacity: 0.6;
  transition: opacity var(--transition-fast);
}
.detail-panel__close:hover { opacity: 1; }

.detail-panel__body {
  padding: var(--space-4) var(--space-5);
  display: flex; flex-direction: column; gap: 0.6rem;
}

.detail-line {
  font-family: var(--font-sans); font-size: var(--text-sm); color: var(--color-text);
}
.detail-line span { font-weight: 600; color: var(--burgundy-700); }

.detail-book {
  display: flex; gap: var(--space-3); align-items: flex-start;
  background: var(--color-bg-muted); border-radius: var(--radius-md);
  padding: var(--space-3); margin: var(--space-1) 0;
}
.detail-book__label {
  font-family: var(--font-sans); font-size: var(--text-xs);
  font-weight: 600; color: var(--burgundy-700);
}

.detail-desc { font-family: var(--font-sans); font-size: var(--text-sm); }
.detail-desc span { font-weight: 600; color: var(--burgundy-700); display: block; margin-bottom: var(--space-1); }
.detail-desc p    { color: var(--color-text); line-height: 1.55; }

/* ── Confirm delete ── */
.confirm-backdrop {
  position: fixed; inset: 0;
  background: rgba(42,10,16,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000;
}
.confirm-box {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-8); width: 320px; text-align: center;
  box-shadow: var(--shadow-lg);
}
.confirm-box h3 {
  font-size: var(--text-base); color: var(--color-text); margin-bottom: var(--space-2);
}
.confirm-box p {
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-primary); margin-bottom: var(--space-6);
}
.confirm-actions { display: flex; gap: var(--space-3); justify-content: center; }
.btn-cancel-sm {
  background: transparent; border: 1.5px solid var(--color-border);
  color: var(--burgundy-700); font-family: var(--font-sans);
  font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm);
  cursor: pointer; transition: border-color var(--transition-fast);
}
.btn-cancel-sm:hover { border-color: var(--color-primary); }
.btn-delete-sm {
  background: var(--color-error); border: none; color: var(--color-text-inverse);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition-fast);
}
.btn-delete-sm:hover { background: #a93226; }

/* ── Transitions ── */
.slide-enter-active, .slide-leave-active { transition: opacity var(--transition-normal), transform var(--transition-normal); }
.slide-enter-from, .slide-leave-to       { opacity: 0; transform: translateX(20px); }
.modal-enter-active, .modal-leave-active { transition: opacity var(--transition-normal); }
.modal-enter-from, .modal-leave-to       { opacity: 0; }
</style>
