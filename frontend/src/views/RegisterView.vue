<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'
import { validateRegister, hasAuthErrors } from '@/utils/authValidation'
import type { RegisterErrors } from '@/utils/authValidation'

const router = useRouter()
const store  = useUsersStore()

// ── Form state ────────────────────────────────────────────────
const form = ref({
  fullName: '',
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errors   = ref<RegisterErrors>({})
const touched  = ref<Set<string>>(new Set())
const apiError = ref<string | null>(null)
const loading  = ref(false)

// ── Validare live ─────────────────────────────────────────────
const touch = (field: string) => {
  touched.value.add(field)
  errors.value = validateRegister(form.value)
}

const getError = (field: keyof RegisterErrors) =>
  touched.value.has(field) ? errors.value[field] : undefined

// ── Submit ────────────────────────────────────────────────────
const handleRegister = async () => {
  // Marchează toate câmpurile
  Object.keys(form.value).forEach(k => touched.value.add(k))
  errors.value = validateRegister(form.value)
  if (hasAuthErrors(errors.value)) return

  loading.value  = true
  apiError.value = null

  const result = store.register({
    fullName: form.value.fullName,
    username: form.value.username,
    email:    form.value.email,
    password: form.value.password,
  })

  loading.value = false

  if (result.success) {
    router.push('/meetups')
  } else {
    apiError.value = result.error ?? 'Registration failed.'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">

      <!-- Logo -->
      <div class="auth-logo" @click="router.push('/')">
        <img src="../assets/logo.png.png" alt="CommonPlot logo" class="auth-logo__img" />
      </div>

      <!-- Title -->
      <h1 class="auth-title">Create Account</h1>

      <!-- API error -->
      <div v-if="apiError" class="auth-api-error">{{ apiError }}</div>

      <!-- Form -->
      <div class="auth-form">

        <!-- Full Name -->
        <div class="field">
          <div class="input-wrap" :class="{ 'input-wrap--error': getError('fullName') }">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 17c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.fullName"
              type="text"
              placeholder="Full Name"
              class="auth-input"
              @blur="touch('fullName')"
            />
          </div>
          <span v-if="getError('fullName')" class="field-error">{{ getError('fullName') }}</span>
        </div>

        <!-- Username -->
        <div class="field">
          <div class="input-wrap" :class="{ 'input-wrap--error': getError('username') }">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 17c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.username"
              type="text"
              placeholder="Username"
              class="auth-input auth-input--upper"
              @blur="touch('username')"
            />
          </div>
          <span v-if="getError('username')" class="field-error">{{ getError('username') }}</span>
        </div>

        <!-- Email -->
        <div class="field">
          <div class="input-wrap" :class="{ 'input-wrap--error': getError('email') }">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <rect x="2" y="5" width="16" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 7l8 5 8-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.email"
              type="email"
              placeholder="E-mail"
              class="auth-input"
              @blur="touch('email')"
            />
          </div>
          <span v-if="getError('email')" class="field-error">{{ getError('email') }}</span>
        </div>

        <!-- Password -->
        <div class="field">
          <div class="input-wrap" :class="{ 'input-wrap--error': getError('password') }">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <rect x="5" y="9" width="10" height="8" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 9V6a3 3 0 116 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.password"
              type="password"
              placeholder="Password"
              class="auth-input"
              @blur="touch('password')"
            />
          </div>
          <span v-if="getError('password')" class="field-error">{{ getError('password') }}</span>
        </div>

        <!-- Confirm Password -->
        <div class="field">
          <div class="input-wrap" :class="{ 'input-wrap--error': getError('confirmPassword') }">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <rect x="5" y="9" width="10" height="8" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 9V6a3 3 0 116 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="Confirm Password"
              class="auth-input"
              @blur="touch('confirmPassword')"
            />
          </div>
          <span v-if="getError('confirmPassword')" class="field-error">{{ getError('confirmPassword') }}</span>
        </div>

        <!-- Submit -->
        <button class="auth-btn" :disabled="loading" @click="handleRegister">
          {{ loading ? 'Creating account...' : 'REGISTER' }}
        </button>

        <!-- Divider -->
        <div class="auth-divider"><span>or</span></div>

        <!-- Google (placeholder) -->
        <button class="auth-btn-google">
          <svg width="18" height="18" viewBox="0 0 18 18">
            <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z"/>
            <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z"/>
            <path fill="#FBBC05" d="M3.964 10.707A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.707V4.961H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.039l3.007-2.332z"/>
            <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.961L3.964 6.293C4.672 4.166 6.656 3.58 9 3.58z"/>
          </svg>
          Sign up with Google
        </button>

        <!-- Login link -->
        <p class="auth-switch">
          Already have an account?
          <span class="auth-link" @click="router.push('/login')">Login</span>
        </p>

      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: var(--cream-200);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-sans);
}

.auth-card {
  width: min(380px, 92vw);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-8) var(--space-6);
}

/* Logo */
.auth-logo { cursor: pointer; }
.auth-logo__img { height: 70px; width: auto; }

/* Title */
.auth-title {
  font-family: var(--font-serif);
  font-size: var(--text-xl);
  color: var(--burgundy-900);
  font-weight: 700;
  margin: 0;
}

/* API error */
.auth-api-error {
  width: 100%;
  background: var(--color-error-bg);
  border: 1px solid var(--color-error);
  color: var(--color-error);
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  text-align: center;
}

/* Form */
.auth-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* Field */
.field { display: flex; flex-direction: column; gap: var(--space-1); }

.input-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: 0.6rem var(--space-4);
  background: var(--color-bg);
  transition: border-color var(--transition-fast);
}
.input-wrap:focus-within      { border-color: var(--color-primary); }
.input-wrap--error            { border-color: var(--color-error); }

.input-icon {
  width: 16px; height: 16px;
  color: var(--burgundy-400);
  flex-shrink: 0;
}

.auth-input {
  border: none;
  background: transparent;
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--color-text);
  outline: none;
  width: 100%;
}
.auth-input::placeholder { color: var(--burgundy-600); }
.auth-input--upper::placeholder { text-transform: uppercase; letter-spacing: 0.08em; }

.field-error {
  font-size: var(--text-xs);
  color: var(--color-error);
  padding-left: var(--space-4);
}

/* Buttons */
.auth-btn {
  width: 100%;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-full);
  padding: 0.75rem;
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  cursor: pointer;
  transition: background var(--transition-fast);
  margin-top: var(--space-2);
}
.auth-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.auth-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.auth-btn-google {
  width: 100%;
  background: var(--gold-300);
  color: var(--burgundy-900);
  border: none;
  border-radius: var(--radius-full);
  padding: 0.7rem;
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  transition: background var(--transition-fast);
}
.auth-btn-google:hover { background: var(--gold-500); }

/* Divider */
.auth-divider {
  text-align: center;
  position: relative;
  color: var(--cream-600);
  font-size: var(--text-xs);
}
.auth-divider::before,
.auth-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 42%;
  height: 1px;
  background: var(--color-border);
}
.auth-divider::before { left: 0; }
.auth-divider::after  { right: 0; }

/* Switch link */
.auth-switch {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--burgundy-700);
  margin: 0;
}
.auth-link {
  color: var(--color-primary);
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}
.auth-link:hover { color: var(--color-primary-hover); }
</style>
