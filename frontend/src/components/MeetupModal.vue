<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Meetup, CreateMeetupPayload, ValidationErrors } from '../types/indexes.ts'
import { validateMeetup, hasErrors } from '@/utils/meetupValidation'



const props = defineProps<{
  visible: boolean
  meetup: Meetup | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', payload: CreateMeetupPayload | (Partial<Meetup> & { id: number })): void
}>()

// ── Form state ────────────────────────────────────────────────
const form = ref<Partial<CreateMeetupPayload>>({})
const errors = ref<ValidationErrors>({})
const touched = ref<Set<string>>(new Set())


watch(() => props.visible, (isVisible) => {
  if (!isVisible) return
  errors.value = {}
  touched.value = new Set()
  if (props.meetup) {

    form.value = { ...props.meetup }
  } else {
    // ADD: form gol
    form.value = {
      ownerID: undefined,
      ownerUsername: '',
      location: '',
      date: '',
      bookID: undefined,
      duration: 60,
      rating: 0,
      titleEvent: '',
      description: '',
    }
  }
})

// Validare pe câmpul atins
const touch = (field: string) => {
  touched.value.add(field)
  errors.value = validateMeetup(form.value)
}

const getError = (field: keyof ValidationErrors) =>
  touched.value.has(field) ? errors.value[field] : undefined

// ── Submit ────────────────────────────────────────────────────
const handleSave = () => {
  // Marchează toate câmpurile ca atinse
  Object.keys(form.value).forEach(k => touched.value.add(k))
  errors.value = validateMeetup(form.value)

  if (hasErrors(errors.value)) return

  if (props.meetup) {
    emit('save', { ...form.value, id: props.meetup.id } as Partial<Meetup> & { id: number })
  } else {
    emit('save', form.value as CreateMeetupPayload)
  }
}

const isEdit = () => !!props.meetup
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-backdrop" @click.self="emit('close')">
        <div class="modal">
          <!-- Header -->
          <div class="modal__header">
            <h2>{{ isEdit() ? 'Edit Meet-up' : 'New Meet-up' }}</h2>
            <button class="modal__close" @click="emit('close')">✕</button>
          </div>

          <!-- Body -->
          <div class="modal__body">
            <!-- Title -->
            <div class="field">
              <label>Event Title *</label>
              <input
                v-model="form.titleEvent"
                type="text"
                placeholder="e.g. Morning Coffee & Dostoievski"
                :class="{ error: getError('titleEvent') }"
                @blur="touch('titleEvent')"
              />
              <span v-if="getError('titleEvent')" class="field__error">{{ getError('titleEvent') }}</span>
            </div>

            <!-- Location -->
            <div class="field">
              <label>Location *</label>
              <input
                v-model="form.location"
                type="text"
                placeholder="e.g. Bunt, Cluj-Napoca"
                :class="{ error: getError('location') }"
                @blur="touch('location')"
              />
              <span v-if="getError('location')" class="field__error">{{ getError('location') }}</span>
            </div>

            <!-- Date + Duration row -->
            <div class="field-row">
              <div class="field">
                <label>Date & Time *</label>
                <input
                  v-model="form.date"
                  type="datetime-local"
                  :class="{ error: getError('date') }"
                  @blur="touch('date')"
                />
                <span v-if="getError('date')" class="field__error">{{ getError('date') }}</span>
              </div>
              <div class="field">
                <label>Duration (min) *</label>
                <input
                  v-model.number="form.duration"
                  type="number"
                  min="15"
                  max="480"
                  placeholder="90"
                  :class="{ error: getError('duration') }"
                  @blur="touch('duration')"
                />
                <span v-if="getError('duration')" class="field__error">{{ getError('duration') }}</span>
              </div>
            </div>

            <!-- Owner row -->
            <div class="field-row">
              <div class="field">
                <label>Owner ID *</label>
                <input
                  v-model.number="form.ownerID"
                  type="number"
                  min="1"
                  placeholder="53"
                  :class="{ error: getError('ownerID') }"
                  @blur="touch('ownerID')"
                />
                <span v-if="getError('ownerID')" class="field__error">{{ getError('ownerID') }}</span>
              </div>
              <div class="field">
                <label>Owner Username *</label>
                <input
                  v-model="form.ownerUsername"
                  type="text"
                  placeholder="Alex M."
                  :class="{ error: getError('ownerUsername') }"
                  @blur="touch('ownerUsername')"
                />
                <span v-if="getError('ownerUsername')" class="field__error">{{ getError('ownerUsername') }}</span>
              </div>
            </div>

            <!-- Book + Rating row -->
            <div class="field-row">
              <div class="field">
                <label>Book ID *</label>
                <input
                  v-model.number="form.bookID"
                  type="number"
                  min="1"
                  placeholder="300"
                  :class="{ error: getError('bookID') }"
                  @blur="touch('bookID')"
                />
                <span v-if="getError('bookID')" class="field__error">{{ getError('bookID') }}</span>
              </div>
              <div class="field">
                <label>Rating (0–5)</label>
                <input
                  v-model.number="form.rating"
                  type="number"
                  min="0"
                  max="5"
                  step="0.1"
                  placeholder="4.5"
                  :class="{ error: getError('rating') }"
                  @blur="touch('rating')"
                />
                <span v-if="getError('rating')" class="field__error">{{ getError('rating') }}</span>
              </div>
            </div>

            <!-- Description -->
            <div class="field">
              <label>Description</label>
              <textarea
                v-model="form.description"
                rows="3"
                placeholder="What will you discuss?"
                :class="{ error: getError('description') }"
                @blur="touch('description')"
              />
              <span v-if="getError('description')" class="field__error">{{ getError('description') }}</span>
            </div>
          </div>

          <!-- Footer -->
          <div class="modal__footer">
            <button class="btn-ghost" @click="emit('close')">Cancel</button>
            <button class="btn-primary"   @click="handleSave">
              {{ isEdit() ? 'Save Changes' : 'Create Meet-up' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>

.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(42, 10, 16, 0.55);
  backdrop-filter: blur(3px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}


.modal {
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  width: min(580px, 95vw);
  max-height: 90vh;
  display: flex; flex-direction: column;
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}


.modal__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: var(--color-primary);
  color: var(--color-text-inverse);
}
.modal__header h2 {
  font-family: var(--font-serif);
  font-size: var(--text-lg);
  font-weight: 700;
}
.modal__close {
  background: transparent; border: none;
  color: var(--color-text-inverse);
  font-size: var(--text-base);
  cursor: pointer; opacity: 0.7;
  transition: opacity var(--transition-fast);
}
.modal__close:hover { opacity: 1; }


.modal__body {
  padding: var(--space-6);
  overflow-y: auto;
  display: flex; flex-direction: column; gap: var(--space-4);
}


.field     { display: flex; flex-direction: column; gap: var(--space-1); }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }

label {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-primary);
}

input, textarea {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  padding: 0.55rem var(--space-3);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-soft);
  color: var(--color-text);
  outline: none;
  transition: border-color var(--transition-fast);
}
input:focus, textarea:focus { border-color: var(--color-primary); }
input.error, textarea.error { border-color: var(--color-error); background: var(--color-error-bg); }
textarea { resize: vertical; }

.field__error {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  color: var(--color-error);
}


.modal__footer {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-soft);
}


.modal-enter-active, .modal-leave-active { transition: opacity var(--transition-normal), transform var(--transition-normal); }
.modal-enter-from, .modal-leave-to { opacity: 0; transform: scale(0.96); }
</style>
