<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users'

const router = useRouter()
const store  = useUsersStore()

const form     = ref({ username: '', password: '' })
const apiError = ref<string | null>(null)
const loading  = ref(false)

const handleLogin = async () => {
  if (!form.value.username.trim() || !form.value.password.trim()) {
    apiError.value = 'Username and password are required.'
    return
  }

  loading.value  = true
  apiError.value = null

  const result = await store.login(form.value.username, form.value.password)
  loading.value = false

  if (result.success) {
    router.push('/meetups')
  } else {
    apiError.value = result.error ?? 'Login failed.'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">

      <!-- Logo -->
      <div class="auth-logo" @click="router.push('/')">
        <img src="../assets/logo.png" alt="CommonPlot" class="auth-logo__img" />
      </div>



      <!-- API error -->
      <div v-if="apiError" class="auth-api-error">{{ apiError }}</div>

      <!-- Form -->
      <div class="auth-form">

        <!-- Username -->
        <div class="field">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 17c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.username"
              type="text"
              placeholder="Username"
              class="auth-input"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Password -->
        <div class="field">
          <div class="input-wrap">
            <svg class="input-icon" viewBox="0 0 20 20" fill="none">
              <rect x="5" y="9" width="10" height="8" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 9V6a3 3 0 116 0v3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.password"
              type="password"
              placeholder="Password"
              class="auth-input"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <!-- Submit -->
        <button class="auth-btn" :disabled="loading" @click="handleLogin">
          {{ loading ? 'Logging in...' : 'LOGIN' }}
        </button>

        <!-- Forgot password -->
        <p class="auth-switch" style="margin-top: -0.5rem;">
          <span class="auth-link" @click="router.push('/forgot-password')">Forgot password?</span>
        </p>

        <!-- Register link -->
        <p class="auth-switch">
          Don't have an account?
          <span class="auth-link" @click="router.push('/register')">Sign Up</span>
        </p>

      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-sans);
}
.auth-card {
  width: min(380px, 92vw);
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-4);
  padding: var(--space-8) var(--space-6);
}
.auth-logo { cursor: pointer; }
.auth-logo__img { height: 70px; width: auto; }

.auth-hint {
  display: flex; gap: var(--space-4);
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-600); background: var(--color-bg-muted);
  padding: var(--space-2) var(--space-4); border-radius: var(--radius-sm);
}
.auth-hint strong { color: var(--burgundy-700); }

.auth-api-error {
  width: 100%;
  background: #fdecea; border: 1px solid var(--color-error);
  color: var(--color-error); border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm); text-align: center;
}
.auth-form { width: 100%; display: flex; flex-direction: column; gap: var(--space-3); }
.field { display: flex; flex-direction: column; gap: var(--space-1); }
.input-wrap {
  display: flex; align-items: center; gap: var(--space-2);
  border: 1.5px solid var(--color-border); border-radius: var(--radius-full);
  padding: 0.6rem var(--space-4); background: var(--color-bg);
  transition: border-color var(--transition-fast);
}
.input-wrap:focus-within { border-color: var(--color-primary); }
.input-icon { width: 16px; height: 16px; color: var(--burgundy-400); flex-shrink: 0; }
.auth-input {
  border: none; background: transparent;
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); outline: none; width: 100%;
}
.auth-input::placeholder { color: var(--cream-600); }
.auth-btn {
  width: 100%; background: var(--color-primary); color: var(--color-text-inverse);
  border: none; border-radius: var(--radius-full); padding: 0.75rem;
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 700;
  letter-spacing: 0.15em; text-transform: uppercase;
  cursor: pointer; transition: background var(--transition-fast); margin-top: var(--space-2);
}
.auth-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.auth-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.auth-switch { text-align: center; font-size: var(--text-sm); color: var(--burgundy-700); margin: 0; }
.auth-link { color: var(--color-primary); font-weight: 600; cursor: pointer; text-decoration: underline; }
.auth-link:hover { color: var(--color-primary-hover); }
</style>
