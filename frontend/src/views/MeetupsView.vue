<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMeetupsStore } from '@/stores/meetups'
import { useUsersStore } from '@/stores/users'
import type { Meetup, CreateMeetupPayload } from '../types/indexes.ts'
import MeetupModal from '@/components/MeetupModal.vue'
import { useInfiniteScroll } from '../composables/UseInfiniteScroll.ts'

const store = useMeetupsStore()
const users = useUsersStore()
const isAdmin = computed(() => users.isAdmin)

// ── View mode: tabular vs card ────────────────────────────────
const viewMode = ref<'table' | 'card'>(users.isAdmin ? 'table' : 'card')

// ── Paginare — tabular ────────────────────────────────────────
const PAGE_SIZE = 10

onMounted(async () => {
  store.initNetworkMonitoring()
  await store.loadPage(0, PAGE_SIZE)
})

const totalPages  = computed(() => store.totalPages)
const currentPage = computed(() => store.currentPage + 1)

const goToPage = async (p: number) => {
  if (p >= 1 && p <= totalPages.value)
    await store.loadPage(p - 1, PAGE_SIZE)
}

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, currentPage.value - 1)
  const end   = Math.min(totalPages.value, start + 3)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// ── Infinite scroll — card view ───────────────────────────────
const sentinelRef = ref<HTMLElement | null>(null)

const { loading: scrollLoading } = useInfiniteScroll(sentinelRef, {
  onLoadMore: async () => {
    if (store.hasMore) await store.loadNextPage()
  },
  onPrefetch: async () => {
    if (store.hasMore) await store.prefetchNextPage()
  },
})

// Când se trece la card view, dacă nu sunt destule date încarcă mai mult
const switchToCard = async () => {
  viewMode.value = 'card'
  if (store.meetups.length < PAGE_SIZE * 2) {
    await store.loadPage(0, PAGE_SIZE)
  }
}

const switchToTable = async () => {
  viewMode.value = 'table'
  // Reset la prima pagină când revii la tabel
  await store.loadPage(0, PAGE_SIZE)
}

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
const openEdit = (m: Meetup, e?: Event) => {
  e?.stopPropagation()
  modalMeetup.value = m
  modalVisible.value = true
}
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

const requestDelete = (id: number, e?: Event) => {
  e?.stopPropagation()
  confirmDeleteId.value = id
}
const cancelDelete = () => { confirmDeleteId.value = null }

const confirmDelete = async () => {
  if (confirmDeleteId.value === null) return
  if (selectedMeetup.value?.id === confirmDeleteId.value) selectedMeetup.value = null
  await store.deleteMeetup(confirmDeleteId.value)
  confirmDeleteId.value = null
  await store.loadPage(store.currentPage, PAGE_SIZE)
}

// ── Format ────────────────────────────────────────────────────
const formatDate = (iso: string) => {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}
const formatDateTime = (iso: string) => {
  if (!iso) return ''
  return new Date(iso).toLocaleString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

// Stele pentru rating
const stars = (rating: number) => {
  const full  = Math.floor(rating)
  const half  = rating % 1 >= 0.5 ? 1 : 0
  const empty = 5 - full - half
  return '★'.repeat(full) + (half ? '½' : '') + '☆'.repeat(empty)
}
</script>

<template>
  <div class="main">

    <!-- Offline banner -->
    <div v-if="!store.isOnline" class="offline-banner">
      ⚠️ Offline — Changes will sync when connection is restored.
      <span v-if="store.offlineQueue.length > 0">({{ store.offlineQueue.length }} pending)</span>
    </div>

    <!-- Loading bar -->
    <div v-if="store.loading" class="loading-bar" />

    <!-- Topbar -->
    <div class="topbar">
      <h1 class="topbar__title">Meet-ups Management</h1>
      <div class="topbar__actions">

        <!-- Search -->
        <div class="search-wrap">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <circle cx="6" cy="6" r="4.5" stroke="#8B1A2F" stroke-width="1.5"/>
            <line x1="9.5" y1="9.5" x2="13" y2="13" stroke="#8B1A2F" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <input v-model="searchQuery" type="text" placeholder="Search" class="search-input"/>
        </div>

        <!-- View toggle — admin only -->
        <div v-if="isAdmin" class="view-toggle">
          <button
            class="toggle-btn"
            :class="{ active: viewMode === 'table' }"
            @click="switchToTable"
            title="Table view"
          >
            <!-- Table icon -->
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="1" y="1" width="14" height="3" rx="1" fill="currentColor" opacity=".4"/>
              <rect x="1" y="6" width="14" height="3" rx="1" fill="currentColor"/>
              <rect x="1" y="11" width="14" height="3" rx="1" fill="currentColor" opacity=".4"/>
            </svg>
          </button>
          <button
            class="toggle-btn"
            :class="{ active: viewMode === 'card' }"
            @click="switchToCard"
            title="Card view (infinite scroll)"
          >
            <!-- Grid icon -->
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="1" y="1" width="6" height="6" rx="1" fill="currentColor"/>
              <rect x="9" y="1" width="6" height="6" rx="1" fill="currentColor" opacity=".4"/>
              <rect x="1" y="9" width="6" height="6" rx="1" fill="currentColor" opacity=".4"/>
              <rect x="9" y="9" width="6" height="6" rx="1" fill="currentColor"/>
            </svg>
          </button>
        </div>

        <button v-if="isAdmin" class="btn-new" @click="openAdd">+ New Meet-up</button>
      </div>
    </div>

    <!-- TABLE VIEW — admin only -->
    <template v-if="viewMode === 'table' && isAdmin">
      <div class="content" :class="{ 'has-detail': selectedMeetup }">
        <div class="table-wrap">
          <table class="meetup-table">
            <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Location</th>
              <th>Date</th>
              <th>Book ID</th>
              <th>Rating</th>
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
              <td class="td-title">{{ m.titleEvent }}</td>
              <td>{{ m.location }}</td>
              <td>{{ formatDate(m.date) }}</td>
              <td>{{ m.bookID }}</td>
              <td>{{ m.rating }}/5</td>
              <td class="actions-cell" @click.stop>
                <button class="action-btn action-btn--edit" @click="openEdit(m)">
                  <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
                    <path d="M10.5 2.5l2 2L5 12H3v-2L10.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button class="action-btn action-btn--delete" @click="requestDelete(m.id)">
                  <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
                    <rect x="2" y="4" width="11" height="9" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                    <path d="M1 4h13M5 4V2.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5V4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  </svg>
                </button>
              </td>
            </tr>
            <tr v-if="filteredMeetups.length === 0">
              <td colspan="7" class="empty-state">No meet-ups found.</td>
            </tr>
            </tbody>
          </table>

          <!-- Pagination (only in table mode) -->
          <div v-if="!searchQuery" class="pagination">
            <button class="pg-btn" :disabled="currentPage === 1" @click="goToPage(1)">«</button>
            <button class="pg-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">‹</button>
            <button
              v-for="p in visiblePages" :key="p"
              class="pg-btn" :class="{ active: p === currentPage }"
              @click="goToPage(p)"
            >{{ p }}</button>
            <button class="pg-btn" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">›</button>
            <button class="pg-btn" :disabled="currentPage === totalPages" @click="goToPage(totalPages)">»</button>
          </div>
        </div>

        <!-- Detail panel -->
        <Transition name="slide">
          <div v-if="selectedMeetup" class="detail-panel">
            <div class="detail-panel__header">
              <h3>Meet-up Details</h3>
              <button class="detail-panel__close" @click="selectedMeetup = null">✕</button>
            </div>
            <div class="detail-panel__body">
              <p class="detail-line"><span>ID:</span> {{ selectedMeetup.id }}</p>
              <p class="detail-line"><span>Title:</span> {{ selectedMeetup.titleEvent }}</p>
              <p class="detail-line"><span>Location:</span> {{ selectedMeetup.location }}</p>
              <p class="detail-line"><span>Date:</span> {{ formatDateTime(selectedMeetup.date) }}</p>
              <p class="detail-line"><span>Duration:</span> {{ selectedMeetup.duration }} min</p>
              <p class="detail-line"><span>Book ID:</span> {{ selectedMeetup.bookID }}</p>
              <p class="detail-line"><span>Owner:</span> {{ selectedMeetup.ownerUsername }}</p>
              <p class="detail-line"><span>Rating:</span> {{ selectedMeetup.rating }}/5</p>
              <div class="detail-desc">
                <span>Description:</span>
                <p>{{ selectedMeetup.description }}</p>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </template>

    <!-- ══════════════════════════════════════════════════════
         CARD VIEW — infinite scroll
    ══════════════════════════════════════════════════════ -->
    <template v-else>
      <div class="card-wrap">

        <div class="card-grid">
          <div
            v-for="m in filteredMeetups"
            :key="m.id"
            class="meetup-card"
            @click="selectMeetup(m)"
            :class="{ 'meetup-card--selected': selectedMeetup?.id === m.id }"
          >
            <!-- Card header -->
            <div class="meetup-card__header">
              <span class="meetup-card__id">#{{ m.id }}</span>
              <span class="meetup-card__rating">{{ stars(m.rating) }}</span>
            </div>

            <!-- Title -->
            <h3 class="meetup-card__title">{{ m.titleEvent }}</h3>

            <!-- Info rows -->
            <div class="meetup-card__info">
              <div class="meetup-card__row">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <path d="M6 1a3.5 3.5 0 110 7A3.5 3.5 0 016 1zM6 9c-3.314 0-5 1.343-5 2v.5h10V11c0-.657-1.686-2-5-2z" fill="currentColor"/>
                </svg>
                {{ m.ownerUsername }}
              </div>
              <div class="meetup-card__row">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <path d="M6 1C4.067 1 2.5 2.567 2.5 4.5c0 2.625 3.5 6.5 3.5 6.5S9.5 7.125 9.5 4.5C9.5 2.567 7.933 1 6 1zm0 4.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3z" fill="currentColor"/>
                </svg>
                {{ m.location }}
              </div>
              <div class="meetup-card__row">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <rect x="1" y="2" width="10" height="9" rx="1" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M1 5h10M4 1v2M8 1v2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
                {{ formatDate(m.date) }}
              </div>
              <div class="meetup-card__row">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <circle cx="6" cy="6" r="4.5" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M6 3.5V6l2 1.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
                {{ m.duration }} min
              </div>
            </div>

            <!-- Description -->
            <p class="meetup-card__desc">{{ m.description }}</p>

            <!-- Footer actions -->
            <div class="meetup-card__footer" @click.stop>
              <span class="meetup-card__book">Book #{{ m.bookID }}</span>
              <div v-if="isAdmin" class="meetup-card__actions">
                <button class="action-btn action-btn--edit" @click="openEdit(m, $event)">
                  <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
                    <path d="M10.5 2.5l2 2L5 12H3v-2L10.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button class="action-btn action-btn--delete" @click="requestDelete(m.id, $event)">
                  <svg width="13" height="13" viewBox="0 0 15 15" fill="none">
                    <rect x="2" y="4" width="11" height="9" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                    <path d="M1 4h13M5 4V2.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5V4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-if="filteredMeetups.length === 0" class="card-empty">
            No meet-ups found.
          </div>
        </div>

        <!-- Sentinel — IntersectionObserver îl observă pentru infinite scroll -->
        <div ref="sentinelRef" class="sentinel" />

        <!-- Infinite scroll indicator -->
        <div class="scroll-indicator">
          <div v-if="store.loadingMore || scrollLoading" class="scroll-loading">
            <span class="scroll-loading__dot" />
            <span class="scroll-loading__dot" />
            <span class="scroll-loading__dot" />
          </div>
          <div v-else-if="!store.hasMore && filteredMeetups.length > 0" class="scroll-end">
            All {{ store.totalElements }} meet-ups loaded
          </div>
        </div>

      </div>
    </template>

    <!-- Delete confirmation -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="confirmDeleteId !== null" class="confirm-backdrop" @click.self="cancelDelete">
          <div class="confirm-box">
            <h3>Delete Meet-up #{{ confirmDeleteId }}?</h3>
            <p>This action cannot be undone.</p>
            <div class="confirm-actions">
              <button class="btn-cancel-sm" @click="cancelDelete">Cancel</button>
              <button class="btn-delete-sm" @click="confirmDelete">Delete</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Add / Edit Modal -->
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
  background: #f39c12; color: white; text-align: center;
  padding: var(--space-2) var(--space-4);
  font-family: var(--font-sans); font-size: var(--text-sm); font-weight: 600;
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
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; background: var(--color-bg-muted); font-family: var(--font-serif);
}

/* ── Topbar ── */
.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft); border-bottom: 1px solid var(--color-border);
}
.topbar__title { font-size: var(--text-lg); font-weight: 700; color: var(--color-text); }
.topbar__actions { display: flex; align-items: center; gap: var(--space-3); }

.search-wrap {
  display: flex; align-items: center; gap: var(--space-2);
  border: 1.5px solid var(--color-border); border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3); background: var(--color-bg-soft);
}
.search-input {
  border: none; background: transparent;
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); outline: none; width: 140px;
}
.search-input::placeholder { color: var(--cream-600); }

/* ── View toggle ── */
.view-toggle {
  display: flex; gap: 2px;
  background: var(--color-bg-muted);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm); padding: 2px;
}
.toggle-btn {
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 28px; border-radius: calc(var(--radius-sm) - 2px);
  border: none; background: transparent; color: var(--cream-600);
  cursor: pointer; transition: all var(--transition-fast);
}
.toggle-btn:hover { color: var(--color-primary); }
.toggle-btn.active { background: var(--color-bg-soft); color: var(--color-primary); box-shadow: var(--shadow-sm); }

.btn-new {
  background: var(--color-primary); color: var(--color-text-inverse);
  border: none; border-radius: var(--radius-sm);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  letter-spacing: 0.05em; padding: var(--space-2) var(--space-4);
  cursor: pointer; transition: background var(--transition-fast);
}
.btn-new:hover { background: var(--color-primary-hover); }

/* ══════════════════════════════════════════════════════════
   TABLE VIEW
══════════════════════════════════════════════════════════ */
.content {
  flex: 1; display: grid; grid-template-columns: 1fr;
  transition: grid-template-columns var(--transition-normal); overflow: hidden;
}
.content.has-detail { grid-template-columns: 1fr 300px; }

.table-wrap {
  padding: var(--space-5) 1.75rem; overflow-y: auto;
  display: flex; flex-direction: column;
}

.meetup-table {
  width: 100%; border-collapse: collapse;
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  overflow: hidden; box-shadow: var(--shadow-sm);
}
.meetup-table thead tr { background: var(--color-accent); }
.meetup-table th {
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 700;
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
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); padding: 0.7rem var(--space-4);
}
.td-title { font-weight: 600; color: var(--burgundy-700); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.actions-cell { display: flex; gap: var(--space-2); align-items: center; }
.action-btn {
  display: flex; align-items: center; justify-content: center;
  width: 30px; height: 30px; border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-soft); color: var(--color-primary);
  cursor: pointer; transition: all var(--transition-fast);
}
.action-btn:hover         { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.action-btn--delete:hover { background: var(--color-error);   color: var(--color-text-inverse); border-color: var(--color-error); }

.empty-state {
  text-align: center; padding: var(--space-12);
  color: var(--cream-600); font-family: var(--font-sans); font-style: italic;
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
  background: var(--color-bg-soft); border-left: 1px solid var(--color-border);
  display: flex; flex-direction: column; overflow-y: auto;
}
.detail-panel__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--color-border);
}
.detail-panel__header h3 { font-size: var(--text-base); font-weight: 700; color: var(--color-text); }
.detail-panel__close {
  background: transparent; border: none; color: var(--color-primary);
  cursor: pointer; opacity: 0.6; transition: opacity var(--transition-fast);
}
.detail-panel__close:hover { opacity: 1; }
.detail-panel__body { padding: var(--space-4) var(--space-5); display: flex; flex-direction: column; gap: 0.6rem; }
.detail-line { font-family: var(--font-sans); font-size: var(--text-sm); color: var(--color-text); }
.detail-line span { font-weight: 600; color: var(--burgundy-700); }
.detail-desc { font-family: var(--font-sans); font-size: var(--text-sm); }
.detail-desc span { font-weight: 600; color: var(--burgundy-700); display: block; margin-bottom: var(--space-1); }
.detail-desc p { color: var(--color-text); line-height: 1.55; }

/* ══════════════════════════════════════════════════════════
   CARD VIEW — infinite scroll
══════════════════════════════════════════════════════════ */
.card-wrap {
  flex: 1; overflow-y: auto;
  padding: var(--space-5) 1.75rem;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.meetup-card {
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-sm);
  border: 1.5px solid transparent;
  cursor: pointer;
  display: flex; flex-direction: column; gap: var(--space-2);
  transition: all var(--transition-fast);
}
.meetup-card:hover          { border-color: var(--color-border); box-shadow: var(--shadow-md); transform: translateY(-2px); }
.meetup-card--selected      { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(139,26,47,0.1); }

.meetup-card__header {
  display: flex; align-items: center; justify-content: space-between;
}
.meetup-card__id {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-600); font-weight: 600;
}
.meetup-card__rating {
  font-size: 0.7rem; color: var(--gold-500);
  letter-spacing: 1px;
}

.meetup-card__title {
  font-family: var(--font-serif); font-size: var(--text-base);
  font-weight: 700; color: var(--color-text);
  margin: 0; line-height: 1.3;
  display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}

.meetup-card__info { display: flex; flex-direction: column; gap: 4px; }
.meetup-card__row {
  display: flex; align-items: center; gap: var(--space-2);
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--color-text-muted);
}
.meetup-card__row svg { color: var(--burgundy-600); flex-shrink: 0; }

.meetup-card__desc {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-600); line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
  flex: 1;
}

.meetup-card__footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border);
  margin-top: auto;
}
.meetup-card__book {
  font-family: var(--font-sans); font-size: var(--text-xs);
  font-weight: 600; color: var(--burgundy-700);
  background: var(--color-bg-muted);
  padding: 2px var(--space-2); border-radius: var(--radius-sm);
}
.meetup-card__actions { display: flex; gap: var(--space-1); }

.card-empty {
  grid-column: 1 / -1; text-align: center; padding: var(--space-12);
  color: var(--cream-600); font-family: var(--font-sans); font-style: italic;
}

/* ── Sentinel (invisible) ── */
.sentinel { height: 1px; width: 100%; }

/* ── Infinite scroll indicator ── */
.scroll-indicator {
  display: flex; justify-content: center;
  padding: var(--space-6) 0; min-height: 48px;
}
.scroll-loading { display: flex; gap: var(--space-1); align-items: center; }
.scroll-loading__dot {
  width: 8px; height: 8px; border-radius: var(--radius-full);
  background: var(--color-primary); animation: bounce 0.6s infinite alternate;
}
.scroll-loading__dot:nth-child(2) { animation-delay: 0.2s; }
.scroll-loading__dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  from { transform: translateY(0); opacity: 0.4; }
  to   { transform: translateY(-6px); opacity: 1; }
}
.scroll-end {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-600); font-style: italic;
}

/* ── Confirm delete ── */
.confirm-backdrop {
  position: fixed; inset: 0; background: rgba(42,10,16,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 2000;
}
.confirm-box {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-8); width: 320px; text-align: center; box-shadow: var(--shadow-lg);
}
.confirm-box h3 { font-size: var(--text-base); color: var(--color-text); margin-bottom: var(--space-2); }
.confirm-box p  { font-family: var(--font-sans); font-size: var(--text-sm); color: var(--color-primary); margin-bottom: var(--space-6); }
.confirm-actions { display: flex; gap: var(--space-3); justify-content: center; }
.btn-cancel-sm {
  background: transparent; border: 1.5px solid var(--color-border);
  color: var(--burgundy-700); font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); cursor: pointer;
}
.btn-cancel-sm:hover { border-color: var(--color-primary); }
.btn-delete-sm {
  background: var(--color-error); border: none; color: var(--color-text-inverse);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); cursor: pointer;
}
.btn-delete-sm:hover { background: #a93226; }

/* ── Transitions ── */
.slide-enter-active, .slide-leave-active { transition: opacity var(--transition-normal), transform var(--transition-normal); }
.slide-enter-from, .slide-leave-to       { opacity: 0; transform: translateX(20px); }
.modal-enter-active, .modal-leave-active { transition: opacity var(--transition-normal); }
.modal-enter-from, .modal-leave-to       { opacity: 0; }
</style>
