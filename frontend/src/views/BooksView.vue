<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'
import {
  fetchBooks, createBook, updateBook, deleteBook,
  type Book
} from '../api/bookApi'
import type { CreateBookPayload } from '../types/indexes.ts'

const router = useRouter()
const users  = useUsersStore()
const isAdmin = computed(() => users.isAdmin)

// ── State ─────────────────────────────────────────────────────
const books   = ref<Book[]>([])
const loading = ref(true)
const error   = ref<string | null>(null)

// ── Modal state ───────────────────────────────────────────────
const modalVisible = ref(false)
const editingBook  = ref<Book | null>(null)
const formErrors   = ref<Record<string, string>>({})

const form = ref<CreateBookPayload>({
  title: '', author: '', description: '', coverUrl: ''
})

// ── Delete confirm ────────────────────────────────────────────
const confirmDeleteId = ref<number | null>(null)

// ── Load ──────────────────────────────────────────────────────
onMounted(loadBooks)

async function loadBooks() {
  loading.value = true
  error.value   = null
  try {
    books.value = await fetchBooks()
  } catch {
    error.value = 'Could not load books.'
  } finally {
    loading.value = false
  }
}

// ── Modal open ────────────────────────────────────────────────
function openAdd() {
  editingBook.value  = null
  form.value         = { title: '', author: '', description: '', coverUrl: '' }
  formErrors.value   = {}
  modalVisible.value = true
}

function openEdit(book: Book) {
  editingBook.value  = book
  form.value         = {
    title:       book.title,
    author:      book.author,
    description: book.description ?? '',
    coverUrl:    book.coverUrl ?? '',
  }
  formErrors.value   = {}
  modalVisible.value = true
}

// ── Validate ──────────────────────────────────────────────────
function validate(): boolean {
  formErrors.value = {}
  if (!form.value.title.trim())
    formErrors.value.title = 'Title is required.'
  else if (form.value.title.length < 2)
    formErrors.value.title = 'Title must be at least 2 characters.'
  if (!form.value.author.trim())
    formErrors.value.author = 'Author is required.'
  if (form.value.coverUrl && !/^https?:\/\/.+/.test(form.value.coverUrl))
    formErrors.value.coverUrl = 'Must be a valid URL (http:// or https://).'
  return Object.keys(formErrors.value).length === 0
}

// ── Save ──────────────────────────────────────────────────────
async function handleSave() {
  if (!validate()) return
  try {
    if (editingBook.value) {
      const updated = await updateBook(editingBook.value.id, form.value)
      const idx = books.value.findIndex(b => b.id === editingBook.value!.id)
      if (idx !== -1) books.value[idx] = updated
    } else {
      const created = await createBook(form.value)
      books.value.push(created)
    }
    modalVisible.value = false
  } catch (e: any) {
    formErrors.value = e?.errors ?? { general: 'Save failed. Please try again.' }
  }
}

// ── Delete ────────────────────────────────────────────────────
async function confirmDelete() {
  if (confirmDeleteId.value === null) return
  try {
    await deleteBook(confirmDeleteId.value)
    books.value = books.value.filter(b => b.id !== confirmDeleteId.value)
  } catch {
    error.value = 'Could not delete book.'
  } finally {
    confirmDeleteId.value = null
  }
}

// ── Navigate to meetups ───────────────────────────────────────
function viewMeetups(bookId: number) {
  router.push({ name: 'book-meetups', params: { bookId } })
}

// ── Cover fallback ────────────────────────────────────────────
function onCoverError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
  img.nextElementSibling?.classList.remove('hidden')
}
</script>

<template>
  <div class="books-page">

    <!-- Header -->
    <div class="topbar">
      <h1 class="topbar__title">Books</h1>
      <button v-if="isAdmin" class="btn-new" @click="openAdd">+ New Book</button>
    </div>

    <!-- Error -->
    <div v-if="error" class="error-banner">{{ error }}</div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-bar" />
    </div>

    <!-- Grid -->
    <div v-else class="book-grid">
      <div v-for="book in books" :key="book.id" class="book-card">

        <!-- Cover -->
        <div class="book-card__cover">
          <img
            v-if="book.coverUrl"
            :src="book.coverUrl"
            :alt="book.title"
            class="book-card__img"
            @error="onCoverError"
          />
          <!-- Fallback placeholder -->
          <div class="book-card__cover-placeholder" :class="{ hidden: !!book.coverUrl }">
            <svg width="40" height="52" viewBox="0 0 40 52" fill="none">
              <rect width="40" height="52" rx="3" fill="#8B1A2F" opacity=".15"/>
              <rect x="6" y="10" width="28" height="2" rx="1" fill="#8B1A2F" opacity=".3"/>
              <rect x="6" y="15" width="20" height="2" rx="1" fill="#8B1A2F" opacity=".3"/>
              <rect x="6" y="32" width="28" height="12" rx="2" fill="#D4A017" opacity=".2"/>
            </svg>
          </div>
        </div>

        <!-- Info -->
        <div class="book-card__body">
          <h3 class="book-card__title">{{ book.title }}</h3>
          <p class="book-card__author">{{ book.author }}</p>
          <p v-if="book.description" class="book-card__desc">{{ book.description }}</p>
        </div>

        <!-- Actions -->
        <div class="book-card__footer">
          <!-- View meetups — relație 1-to-many -->
          <button class="btn-meetups" @click="viewMeetups(book.id)">
            View Meet-ups →
          </button>
          <div v-if="isAdmin" class="book-card__actions">
            <button class="action-btn action-btn--edit" @click="openEdit(book)" title="Edit">
              <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
                <path d="M10.5 2.5l2 2L5 12H3v-2L10.5 2.5z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
              </svg>
            </button>
            <button class="action-btn action-btn--delete" @click="confirmDeleteId = book.id" title="Delete">
              <svg width="14" height="14" viewBox="0 0 15 15" fill="none">
                <rect x="2" y="4" width="11" height="9" rx="1.5" stroke="currentColor" stroke-width="1.4"/>
                <path d="M1 4h13M5 4V2.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5V4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

      </div>

      <!-- Empty state -->
      <div v-if="books.length === 0" class="empty-state">
        No books yet. Add one!
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="modalVisible" class="modal-backdrop" @click.self="modalVisible = false">
          <div class="modal">
            <div class="modal__header">
              <h2>{{ editingBook ? 'Edit Book' : 'New Book' }}</h2>
              <button class="modal__close" @click="modalVisible = false">✕</button>
            </div>
            <div class="modal__body">

              <div class="field">
                <label>Title *</label>
                <input v-model="form.title" type="text" placeholder="e.g. Crime and Punishment"
                       :class="{ 'input-error': formErrors.title }" />
                <span v-if="formErrors.title" class="field-error">{{ formErrors.title }}</span>
              </div>

              <div class="field">
                <label>Author *</label>
                <input v-model="form.author" type="text" placeholder="e.g. Fyodor Dostoevsky"
                       :class="{ 'input-error': formErrors.author }" />
                <span v-if="formErrors.author" class="field-error">{{ formErrors.author }}</span>
              </div>

              <div class="field">
                <label>Description</label>
                <textarea v-model="form.description" rows="3"
                          placeholder="Brief description of the book..." />
              </div>

              <div class="field">
                <label>Cover URL</label>
                <input v-model="form.coverUrl" type="url"
                       placeholder="https://example.com/cover.jpg"
                       :class="{ 'input-error': formErrors.coverUrl }" />
                <span v-if="formErrors.coverUrl" class="field-error">{{ formErrors.coverUrl }}</span>
                <!-- Preview -->
                <img
                  v-if="form.coverUrl"
                  :src="form.coverUrl"
                  alt="Cover preview"
                  class="cover-preview"
                  @error="(e) => (e.target as HTMLImageElement).style.display = 'none'"
                />
              </div>

              <div v-if="formErrors.general" class="field-error">{{ formErrors.general }}</div>
            </div>
            <div class="modal__footer">
              <button class="btn-cancel" @click="modalVisible = false">Cancel</button>
              <button class="btn-save" @click="handleSave">
                {{ editingBook ? 'Save Changes' : 'Create Book' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Delete confirmation -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="confirmDeleteId !== null" class="modal-backdrop" @click.self="confirmDeleteId = null">
          <div class="confirm-box">
            <h3>Delete this book?</h3>
            <p>Meet-ups associated with this book will not be deleted.</p>
            <div class="confirm-actions">
              <button class="btn-cancel" @click="confirmDeleteId = null">Cancel</button>
              <button class="btn-delete" @click="confirmDelete">Delete</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

  </div>
</template>

<style scoped>
.books-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-muted);
  font-family: var(--font-serif);
}

/* Topbar */
.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft); border-bottom: 1px solid var(--color-border);
}
.topbar__title { font-size: var(--text-lg); font-weight: 700; color: var(--color-text); margin: 0; }

.btn-new {
  background: var(--color-primary); color: var(--color-text-inverse);
  border: none; border-radius: var(--radius-sm);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-4); cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-new:hover { background: var(--color-primary-hover); }

/* Error / Loading */
.error-banner {
  background: #fdecea; color: var(--color-error);
  padding: var(--space-2) var(--space-5);
  font-family: var(--font-sans); font-size: var(--text-sm);
}
.loading-wrap { padding: var(--space-2) 0; }
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

/* Book grid */
.book-grid {
  flex: 1; overflow-y: auto;
  padding: var(--space-5) 1.75rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-5);
  align-content: start;
}

/* Book card */
.book-card {
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1.5px solid transparent;
  display: flex; flex-direction: column;
  overflow: hidden;
  transition: all var(--transition-fast);
}
.book-card:hover { border-color: var(--color-border); box-shadow: var(--shadow-md); transform: translateY(-2px); }

/* Cover */
.book-card__cover {
  height: 180px; background: var(--color-bg-muted);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; position: relative;
}
.book-card__img {
  width: 100%; height: 100%; object-fit: cover;
}
.book-card__cover-placeholder {
  display: flex; align-items: center; justify-content: center;
  width: 100%; height: 100%;
}
.book-card__cover-placeholder.hidden { display: none; }

/* Body */
.book-card__body { padding: var(--space-3) var(--space-4); flex: 1; }
.book-card__title {
  font-family: var(--font-serif); font-size: var(--text-base);
  font-weight: 700; color: var(--color-text);
  margin: 0 0 var(--space-1);
  display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.book-card__author {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--burgundy-700); font-weight: 600;
  margin: 0 0 var(--space-2);
}
.book-card__desc {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-600); line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 3;
  -webkit-box-orient: vertical; overflow: hidden;
  margin: 0;
}

/* Footer */
.book-card__footer {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  display: flex; align-items: center; justify-content: space-between;
}
.btn-meetups {
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  color: var(--color-primary); background: transparent; border: none;
  cursor: pointer; padding: 0; transition: color var(--transition-fast);
}
.btn-meetups:hover { color: var(--color-primary-hover); }

.book-card__actions { display: flex; gap: var(--space-1); }
.action-btn {
  display: flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-soft); color: var(--color-primary);
  cursor: pointer; transition: all var(--transition-fast);
}
.action-btn:hover         { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.action-btn--delete:hover { background: var(--color-error); color: var(--color-text-inverse); border-color: var(--color-error); }

/* Empty state */
.empty-state {
  grid-column: 1 / -1; text-align: center; padding: var(--space-16);
  font-family: var(--font-sans); color: var(--cream-600); font-style: italic;
}

/* Modal */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(42,10,16,0.55);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  width: min(480px, 95vw); max-height: 90vh;
  display: flex; flex-direction: column;
  box-shadow: var(--shadow-xl); overflow: hidden;
}
.modal__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  background: var(--color-primary); color: var(--color-text-inverse);
}
.modal__header h2 { font-family: var(--font-serif); font-size: var(--text-lg); font-weight: 700; margin: 0; }
.modal__close { background: transparent; border: none; color: white; cursor: pointer; font-size: 1rem; opacity: 0.8; }
.modal__close:hover { opacity: 1; }
.modal__body { padding: var(--space-5) var(--space-6); display: flex; flex-direction: column; gap: var(--space-4); overflow-y: auto; }
.modal__footer {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-muted);
}

.field { display: flex; flex-direction: column; gap: var(--space-1); }
label {
  font-family: var(--font-sans); font-size: var(--text-xs);
  font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
  color: var(--burgundy-700);
}
input, textarea {
  font-family: var(--font-sans); font-size: var(--text-sm);
  padding: 0.55rem var(--space-3);
  border: 1.5px solid var(--color-border); border-radius: var(--radius-sm);
  background: var(--color-bg); color: var(--color-text); outline: none;
  transition: border-color var(--transition-fast);
}
input:focus, textarea:focus { border-color: var(--color-primary); }
input.input-error { border-color: var(--color-error); }
textarea { resize: vertical; }
.field-error { font-family: var(--font-sans); font-size: var(--text-xs); color: var(--color-error); }

.cover-preview {
  margin-top: var(--space-2); width: 80px; height: 100px;
  object-fit: cover; border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
}

.btn-cancel {
  background: transparent; border: 1.5px solid var(--color-border);
  color: var(--burgundy-700); font-family: var(--font-sans);
  font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); cursor: pointer;
}
.btn-cancel:hover { border-color: var(--color-primary); }
.btn-save {
  background: var(--color-primary); border: none; color: var(--color-text-inverse);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-save:hover { background: var(--color-primary-hover); }

/* Confirm delete */
.confirm-box {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-8); width: 340px; text-align: center; box-shadow: var(--shadow-lg);
}
.confirm-box h3 { font-size: var(--text-base); color: var(--color-text); margin: 0 0 var(--space-2); }
.confirm-box p  { font-family: var(--font-sans); font-size: var(--text-sm); color: var(--color-text-muted); margin: 0 0 var(--space-6); }
.confirm-actions { display: flex; gap: var(--space-3); justify-content: center; }
.btn-delete {
  background: var(--color-error); border: none; color: var(--color-text-inverse);
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600;
  padding: var(--space-2) var(--space-5); border-radius: var(--radius-sm); cursor: pointer;
}
.btn-delete:hover { background: #a93226; }

/* Transitions */
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
