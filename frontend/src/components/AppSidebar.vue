<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUsersStore } from '@/stores/users'

const router = useRouter()
const route  = useRoute()
const users  = useUsersStore()

const isAdmin  = computed(() => users.isAdmin)
const username = computed(() => users.currentUser?.username ?? 'Guest')
const role     = computed(() => users.currentUser?.role ?? '')

// Navigare disponibilă pentru toți
const navItems = [
  { label: 'Books',    path: '/books'   },
  { label: 'Meet-ups', path: '/meetups' },
  { label: 'Chat',     path: '/chat'    },
]

// Navigare doar pentru admin
const adminItems = [
  { label: 'My Neural Network', path: '/network' },
]

// Bottom nav — doar admin
const bottomItems = [
  { label: 'Statistics', path: '/statistics' },
  { label: 'Settings',   path: '/settings'   },
  { label: '🔐 Security', path: '/admin'     },  // ← Gold: logging + suspicious users
]

function handleLogout() {
  users.logout()
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <!-- Logo -->
    <div class="sidebar__logo" @click="router.push('/')">
      <img src="../assets/logoDarkBackground.png" alt="logo" style="height: 40px; width: 126px"/>
    </div>

    <div class="sidebar__divider" />
    <div class="sidebar__divider" />

    <!-- Nav principal -->
    <nav class="sidebar__nav">
      <button
        v-for="item in navItems"
        :key="item.path"
        class="sidebar__item"
        :class="{ active: route.path === item.path }"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </button>

      <!-- Admin only items -->
      <template v-if="isAdmin">
        <button
          v-for="item in adminItems"
          :key="item.path"
          class="sidebar__item sidebar__item--admin"
          :class="{ active: route.path === item.path }"
          @click="router.push(item.path)"
        >
          {{ item.label }}
        </button>
      </template>
    </nav>

    <div class="sidebar__divider sidebar__divider--mid" />

    <!-- Bottom nav — doar admin -->
    <nav v-if="isAdmin" class="sidebar__nav">
      <button
        v-for="item in bottomItems"
        :key="item.path"
        class="sidebar__item"
        :class="{
          active: route.path === item.path,
          'sidebar__item--security': item.path === '/admin'
        }"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="sidebar__spacer" />

    <div class="sidebar__divider" />
    <div class="sidebar__divider" />

    <!-- User info + logout -->
    <div class="sidebar__user">
      <div class="sidebar__avatar">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <circle cx="10" cy="7" r="4" fill="#FAF6EC" opacity=".7"/>
          <path d="M2 17c0-4 3.6-7 8-7s8 3 8 7" stroke="#FAF6EC" stroke-width="1.5" stroke-linecap="round" opacity=".7"/>
        </svg>
      </div>
      <div class="sidebar__user-info">
        <span class="sidebar__username">{{ username }}</span>
        <span class="sidebar__role">{{ role }}</span>
      </div>
    </div>
    <button class="sidebar__logout" @click="handleLogout">Logout</button>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 160px; min-height: 100vh;
  background: var(--color-sidebar);
  display: flex; flex-direction: column;
  padding: var(--space-5) 0; flex-shrink: 0;
}
.sidebar__logo {
  display: flex; align-items: center; gap: var(--space-2);
  padding: 0 var(--space-4) var(--space-4); cursor: pointer;
}
.sidebar__divider {
  height: 1.5px; background: var(--color-secondary);
  margin: 0 0 4px 0; padding-top: 5px;
}
.sidebar__divider--mid {
  margin: var(--space-2) 1rem 0; padding-top: 0;
}
.sidebar__nav {
  display: flex; flex-direction: column; gap: var(--space-1);
  padding: var(--space-2);
}
.sidebar__spacer { flex: 1; }
.sidebar__item {
  background: transparent; border: none;
  color: var(--cream-300); font-family: var(--font-serif), serif;
  font-size: var(--text-sm); text-align: left;
  padding: 0.55rem var(--space-3); border-radius: var(--radius-md);
  cursor: pointer; transition: background var(--transition-fast), color var(--transition-fast);
}
.sidebar__item:hover  { background: rgba(255,255,255,0.1); }
.sidebar__item.active { background: rgba(255,255,255,0.18); color: var(--color-text-inverse); font-weight: 600; }
.sidebar__item--admin    { opacity: 0.85; }
.sidebar__item--security { color: #f39c12; }
.sidebar__item--security:hover { background: rgba(243,156,18,0.15); }
.sidebar__item--security.active { background: rgba(243,156,18,0.2); color: #f39c12; }

.sidebar__user {
  display: flex; align-items: center; gap: var(--space-2);
  padding: var(--space-4) var(--space-5) 0; margin-top: var(--space-2);
}
.sidebar__avatar {
  width: 28px; height: 28px; border-radius: var(--radius-full);
  border: 1.5px solid rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.sidebar__user-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.sidebar__username {
  font-family: var(--font-sans); font-size: var(--text-xs);
  color: var(--cream-300); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.sidebar__role {
  font-family: var(--font-sans); font-size: 10px;
  color: var(--color-secondary); text-transform: uppercase; letter-spacing: 0.05em;
}
.sidebar__logout {
  margin: var(--space-2) var(--space-3) 0;
  background: transparent; border: 1px solid rgba(255,255,255,0.2);
  color: var(--cream-500); font-family: var(--font-sans); font-size: 10px;
  font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase;
  border-radius: var(--radius-sm); padding: var(--space-1) var(--space-3);
  cursor: pointer; transition: all var(--transition-fast);
}
.sidebar__logout:hover { background: rgba(255,255,255,0.1); color: white; }
</style>
