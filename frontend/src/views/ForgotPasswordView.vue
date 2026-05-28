<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'

const router = useRouter()
const store  = useUsersStore()

const email    = ref('')
const loading  = ref(false)
const success  = ref(false)
const error    = ref<string | null>(null)

async function handleSubmit() {
  if (!email.value.trim()) {
    error.value = 'Email is required.'
    return
  }
  loading.value = true
  error.value   = null
  const result  = await store.forgotPassword(email.value)
  loading.value = false
  if (result.success) {
    success.value = true
  } else {
    error.value = result.error ?? 'Something went wrong.'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo" @click="router.push('/')">
        <img src="../assets/logo.png" alt="CommonPlot" class="auth-logo__img" />
      </div>

      <h1 class="auth-title">Forgot Password</h1>

      <div v-if="success" class="auth-success">
        If this email is registered, a reset link has been sent.
        Check the server console for the token (demo mode).
      </div>

      <div v-else class="auth-form">
        <p class="auth-subtitle">Enter your email and we'll send you a reset link.</p>

        <div v-if="error" class="auth-api-error">{{ error }}</div>

        <div class="field">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <rect x="2" y="5" width="16" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 7l8 5 8-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="email"
              type="email"
              placeholder="E-mail"
              class="auth-input"
              @keyup.enter="handleSubmit"
            />
          </div>
        </div>

        <button class="auth-btn" :disabled="loading" @click="handleSubmit">
          {{ loading ? 'Sending...' : 'SEND RESET LINK' }}
        </button>
      </div>

      <p class="auth-switch">
        <span class="auth-link" @click="router.push('/login')">← Back to Login</span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh; background: var(--color-bg);
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-sans);
}
.auth-card {
  width: min(380px, 92vw); display: flex; flex-direction: column;
  align-items: center; gap: var(--space-4);
  padding: var(--space-8) var(--space-6);
}
.auth-logo { cursor: pointer; }
.auth-logo__img { height: 70px; width: auto; }
.auth-title {
  font-family: var(--font-serif); font-size: var(--text-xl);
  color: var(--burgundy-900); font-weight: 700; margin: 0;
}
.auth-subtitle {
  font-size: var(--text-sm); color: var(--cream-600); margin: 0; text-align: center;
}
.auth-success {
  background: #eafaf1; border: 1px solid #27ae60; color: #1e8449;
  border-radius: var(--radius-sm); padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm); text-align: center; width: 100%;
}
.auth-api-error {
  width: 100%; background: #fdecea; border: 1px solid var(--color-error);
  color: var(--color-error); border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3); font-size: var(--text-sm); text-align: center;
}
.auth-form { width: 100%; display: flex; flex-direction: column; gap: var(--space-3); }
.field { display: flex; flex-direction: column; }
.input-wrap {
  display: flex; align-items: center; gap: var(--space-2);
  border: 1.5px solid var(--color-border); border-radius: var(--radius-full);
  padding: 0.6rem var(--space-4); background: var(--color-bg);
  transition: border-color var(--transition-fast);
}
.input-wrap:focus-within { border-color: var(--color-primary); }
.input-icon { width: 16px; height: 16px; color: var(--burgundy-400); flex-shrink: 0; }
.auth-input {
  border: none; background: transparent; font-family: var(--font-sans);
  font-size: var(--text-sm); color: var(--color-text); outline: none; width: 100%;
}
.auth-input::placeholder { color: var(--cream-600); }
.auth-btn {
  width: 100%; background: var(--color-primary); color: var(--color-text-inverse);
  border: none; border-radius: var(--radius-full); padding: 0.75rem;
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 700;
  letter-spacing: 0.15em; text-transform: uppercase; cursor: pointer;
  transition: background var(--transition-fast); margin-top: var(--space-2);
}
.auth-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.auth-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.auth-switch { text-align: center; font-size: var(--text-sm); color: var(--burgundy-700); margin: 0; }
.auth-link { color: var(--color-primary); font-weight: 600; cursor: pointer; text-decoration: underline; }
</style>
