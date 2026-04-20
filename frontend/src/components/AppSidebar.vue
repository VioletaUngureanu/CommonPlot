<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route  = useRoute()

const navItems = [
  { label: 'Books',           path: '/books'     },
  { label: 'Meet-ups',        path: '/meetups'   },
  { label: 'Chat',            path: '/chat'      },
  { label: 'My Neural Network', path: '/network' },
]

const bottomItems = [
  { label: 'Statistics', path: '/statistics' },
  { label: 'Settings',   path: '/settings'   },
]
</script>

<template>
  <aside class="sidebar">
    <!-- Logo -->
    <div class="sidebar__logo" @click="router.push('/')">
      <img src="../assets/logoDarkBackground.png" alt="logo" style="height: 40px; width: 126px"/>
    </div>

    <!-- Linie sub logo -->
    <div class="sidebar__divider" />
    <div class="sidebar__divider" />

    <!-- Top nav -->
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
    </nav>

    <!-- Linie separator între grupuri -->
    <div class="sidebar__divider sidebar__divider--mid" />

    <!-- Bottom nav -->
    <nav class="sidebar__nav">
      <button
        v-for="item in bottomItems"
        :key="item.path"
        class="sidebar__item"
        :class="{ active: route.path === item.path }"
        @click="router.push(item.path)"
      >
        {{ item.label }}
      </button>
    </nav>

    <!-- Spacer — împinge userul la baza sidebar-ului -->
    <div class="sidebar__spacer" />

    <!-- Linie deasupra adminului -->
    <div class="sidebar__divider" />
    <div class="sidebar__divider" />

    <!-- User -->
    <div class="sidebar__user">
      <div class="sidebar__avatar">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <circle cx="10" cy="7" r="4" fill="#FAF6EC" opacity=".7"/>
          <path d="M2 17c0-4 3.6-7 8-7s8 3 8 7" stroke="#FAF6EC" stroke-width="1.5" stroke-linecap="round" opacity=".7"/>
        </svg>
      </div>
      <span class="sidebar__username">Admin</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 160px;
  min-height: 100vh;
  background: var(--color-sidebar);
  display: flex;
  flex-direction: column;
  padding: var(--space-5) 0;
  flex-shrink: 0;
}

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-4) var(--space-4);
  cursor: pointer;
}

/* Linie decorativă gold */
.sidebar__divider {
  height: 1.5px;
  background: var(--color-secondary);
  margin: 0 0 4px 0;
  padding-top: 5px;

}

/* Linia dintre grupuri — margin simetric sus și jos */
.sidebar__divider--mid {
  margin-top: var(--space-2);
  margin-left: 1rem;
  margin-right: 1rem;
  margin-bottom: 0;
  padding-top: 0;
}

/* Linia de deasupra adminului — fără margin jos */
.sidebar__divider--bottom {
  margin-bottom: 0;
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-2);
}

/* Împinge tot ce urmează spre baza sidebar-ului */
.sidebar__spacer { flex: 1; }

.sidebar__item {
  background: transparent;
  border: none;
  color: var(--cream-300);
  font-family: var(--font-serif),serif;
  font-size: var(--text-sm);
  text-align: left;
  padding: 0.55rem var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}
.sidebar__item:hover  { background: rgba(255,255,255,0.1); }
.sidebar__item.active { background: rgba(255,255,255,0.18); color: var(--color-text-inverse); font-weight: 600; }

.sidebar__user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-5) 0;
  margin-top: var(--space-2);
}
.sidebar__avatar {
  width: 28px; height: 28px;
  border-radius: var(--radius-full);
  border: 1.5px solid rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
}
.sidebar__username {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  color: var(--cream-300);
}
</style>
