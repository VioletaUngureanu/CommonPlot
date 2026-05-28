<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUsersStore } from '@/stores/users'
import { useRouter } from 'vue-router'
import { BACKEND_URL } from '@/config'

const users  = useUsersStore()
const router = useRouter()

// Redirect dacă nu e admin
if (!users.isAdmin) router.push('/meetups')

// ── State ─────────────────────────────────────────────────────
interface ActionLog {
  id: number
  userId: number
  username: string
  groupId: string
  actionType: string
  actionInfo: string
  timestamp: string
}

interface SuspiciousUser {
  id: number
  userId: number
  username: string
  reason: string
  detectedAt: string
  resolved: boolean
  resolvedAt?: string
  resolvedBy?: string
}

const logs           = ref<ActionLog[]>([])
const suspicious     = ref<SuspiciousUser[]>([])
const activeTab      = ref<'suspicious' | 'logs'>('suspicious')
const loading        = ref(true)
const filterUsername = ref('')

// ── Load ──────────────────────────────────────────────────────
onMounted(async () => {
  await Promise.all([loadLogs(), loadSuspicious()])
  loading.value = false
})

async function loadLogs() {
  const res = await fetch(`${BACKEND_URL}/api/admin/logs`)
  if (res.ok) logs.value = await res.json()
}

async function loadSuspicious() {
  const res = await fetch(`${BACKEND_URL}/api/admin/suspicious/all`)
  if (res.ok) suspicious.value = await res.json()
}

// ── Resolve suspicious user ───────────────────────────────────
async function resolve(id: number) {
  const res = await fetch(`${BACKEND_URL}/api/admin/suspicious/${id}/resolve`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ resolvedBy: users.currentUser?.username }),
  })
  if (res.ok) await loadSuspicious()
}

// ── Filters ───────────────────────────────────────────────────
const filteredLogs = computed(() => {
  if (!filterUsername.value.trim()) return logs.value
  return logs.value.filter(l =>
    l.username.toLowerCase().includes(filterUsername.value.toLowerCase())
  )
})

const activeSuspicious = computed(() =>
  suspicious.value.filter(s => !s.resolved)
)

// ── Format ────────────────────────────────────────────────────
function formatDate(iso: string) {
  return new Date(iso).toLocaleString('ro-RO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

function actionColor(type: string): string {
  if (type.includes('DELETE'))       return 'action--delete'
  if (type.includes('LOGIN_FAILED')) return 'action--danger'
  if (type.includes('LOGIN'))        return 'action--success'
  if (type.includes('CREATE'))       return 'action--create'
  return 'action--default'
}
</script>

<template>
  <div class="admin-page">

    <!-- Header -->
    <div class="topbar">
      <h1 class="topbar__title">Admin — Security Dashboard</h1>
      <div class="topbar__badges">
        <span class="badge badge--danger" v-if="activeSuspicious.length > 0">
          ⚠ {{ activeSuspicious.length }} suspicious user{{ activeSuspicious.length > 1 ? 's' : '' }}
        </span>
        <span class="badge badge--safe" v-else>✓ No active threats</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-bar" />

    <div v-else class="admin-content">

      <!-- Tabs -->
      <div class="tabs">
        <button class="tab" :class="{ active: activeTab === 'suspicious' }"
                @click="activeTab = 'suspicious'">
          Suspicious Users
          <span v-if="activeSuspicious.length" class="tab-count">{{ activeSuspicious.length }}</span>
        </button>
        <button class="tab" :class="{ active: activeTab === 'logs' }"
                @click="activeTab = 'logs'">
          Action Logs
          <span class="tab-count">{{ logs.length }}</span>
        </button>
      </div>

      <!-- ── SUSPICIOUS USERS ── -->
      <div v-if="activeTab === 'suspicious'" class="section">

        <div v-if="suspicious.length === 0" class="empty-state">
          No suspicious activity detected.
        </div>

        <div v-else class="suspicious-list">
          <div
            v-for="s in suspicious"
            :key="s.id"
            class="suspicious-card"
            :class="{ 'suspicious-card--resolved': s.resolved }"
          >
            <div class="suspicious-card__header">
              <div class="suspicious-card__user">
                <span class="suspicious-card__username">{{ s.username }}</span>
                <span class="suspicious-card__status" :class="s.resolved ? 'status--resolved' : 'status--active'">
                  {{ s.resolved ? 'Resolved' : 'Active' }}
                </span>
              </div>
              <span class="suspicious-card__date">{{ formatDate(s.detectedAt) }}</span>
            </div>

            <p class="suspicious-card__reason">{{ s.reason }}</p>

            <div class="suspicious-card__footer" v-if="!s.resolved">
              <button class="btn-resolve" @click="resolve(s.id)">
                ✓ Mark as Resolved
              </button>
            </div>
            <div class="suspicious-card__footer" v-else>
              <span class="resolved-info">
                Resolved by <strong>{{ s.resolvedBy }}</strong> at {{ formatDate(s.resolvedAt!) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── ACTION LOGS ── -->
      <div v-if="activeTab === 'logs'" class="section">

        <!-- Filter -->
        <div class="log-filter">
          <input
            v-model="filterUsername"
            type="text"
            placeholder="Filter by username..."
            class="filter-input"
          />
          <span class="log-count">{{ filteredLogs.length }} entries</span>
        </div>

        <div class="log-table-wrap">
          <table class="log-table">
            <thead>
            <tr>
              <th>Timestamp</th>
              <th>User</th>
              <th>Group</th>
              <th>Action</th>
              <th>Details</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="log in filteredLogs" :key="log.id">
              <td class="td-time">{{ formatDate(log.timestamp) }}</td>
              <td class="td-user">{{ log.username }}</td>
              <td>
                  <span class="group-badge" :class="log.groupId === 'ADMIN' ? 'group--admin' : 'group--user'">
                    {{ log.groupId }}
                  </span>
              </td>
              <td>
                  <span class="action-badge" :class="actionColor(log.actionType)">
                    {{ log.actionType }}
                  </span>
              </td>
              <td class="td-info">{{ log.actionInfo }}</td>
            </tr>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="5" class="empty-row">No logs found.</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.admin-page {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; background: var(--color-bg-muted);
  font-family: var(--font-sans);
}

.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft); border-bottom: 1px solid var(--color-border);
}
.topbar__title { font-size: var(--text-lg); font-weight: 700; color: var(--color-text); margin: 0; }
.topbar__badges { display: flex; gap: var(--space-2); }

.badge {
  font-size: var(--text-xs); font-weight: 700;
  padding: var(--space-1) var(--space-3); border-radius: var(--radius-sm);
}
.badge--danger { background: #fdecea; color: var(--color-error); border: 1px solid var(--color-error); }
.badge--safe   { background: #eafaf1; color: #27ae60; border: 1px solid #27ae60; }

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

.admin-content {
  flex: 1; overflow: hidden;
  display: flex; flex-direction: column;
  padding: var(--space-4) 1.75rem;
  gap: var(--space-4);
}

/* Tabs */
.tabs { display: flex; gap: var(--space-2); border-bottom: 2px solid var(--color-border); }
.tab {
  background: transparent; border: none; border-bottom: 2px solid transparent;
  margin-bottom: -2px; padding: var(--space-2) var(--space-4);
  font-family: var(--font-sans); font-size: var(--text-sm); font-weight: 600;
  color: var(--cream-600); cursor: pointer;
  display: flex; align-items: center; gap: var(--space-2);
  transition: all var(--transition-fast);
}
.tab:hover { color: var(--color-primary); }
.tab.active { color: var(--color-primary); border-bottom-color: var(--color-primary); }
.tab-count {
  background: var(--color-primary); color: white;
  font-size: 10px; font-weight: 700;
  padding: 1px var(--space-1); border-radius: var(--radius-sm);
  min-width: 18px; text-align: center;
}

.section { flex: 1; overflow-y: auto; }

/* Suspicious cards */
.suspicious-list { display: flex; flex-direction: column; gap: var(--space-3); }
.suspicious-card {
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  padding: var(--space-4); box-shadow: var(--shadow-sm);
  border-left: 4px solid var(--color-error);
}
.suspicious-card--resolved { border-left-color: var(--color-border); opacity: 0.7; }

.suspicious-card__header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--space-2);
}
.suspicious-card__user { display: flex; align-items: center; gap: var(--space-2); }
.suspicious-card__username { font-weight: 700; color: var(--color-text); font-size: var(--text-base); }
.suspicious-card__status {
  font-size: var(--text-xs); font-weight: 700;
  padding: 2px var(--space-2); border-radius: var(--radius-sm);
}
.status--active   { background: #fdecea; color: var(--color-error); }
.status--resolved { background: #eafaf1; color: #27ae60; }
.suspicious-card__date { font-size: var(--text-xs); color: var(--cream-600); }
.suspicious-card__reason { font-size: var(--text-sm); color: var(--color-text); margin: 0 0 var(--space-3); }
.suspicious-card__footer { display: flex; align-items: center; }

.btn-resolve {
  background: #eafaf1; color: #27ae60; border: 1px solid #27ae60;
  font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 700;
  padding: var(--space-1) var(--space-3); border-radius: var(--radius-sm);
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-resolve:hover { background: #27ae60; color: white; }
.resolved-info { font-size: var(--text-xs); color: var(--cream-600); }

/* Log table */
.log-filter {
  display: flex; align-items: center; gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.filter-input {
  border: 1.5px solid var(--color-border); border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); background: var(--color-bg-soft); outline: none;
  width: 220px;
}
.filter-input:focus { border-color: var(--color-primary); }
.log-count { font-size: var(--text-xs); color: var(--cream-600); }

.log-table-wrap { overflow: auto; }
.log-table {
  width: 100%; border-collapse: collapse;
  background: var(--color-bg-soft); border-radius: var(--radius-lg);
  overflow: hidden; box-shadow: var(--shadow-sm);
}
.log-table thead tr { background: var(--color-accent); }
.log-table th {
  font-size: var(--text-xs); font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.07em; color: var(--color-text);
  padding: var(--space-3) var(--space-4); text-align: left;
}
.log-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}
.log-table tbody tr:hover { background: var(--color-bg-muted); }
.log-table td { font-size: var(--text-xs); color: var(--color-text); padding: 0.6rem var(--space-4); }
.td-time  { color: var(--cream-600); white-space: nowrap; }
.td-user  { font-weight: 600; color: var(--burgundy-700); }
.td-info  { color: var(--cream-600); max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-row { text-align: center; padding: var(--space-8); color: var(--cream-600); font-style: italic; }

.group-badge {
  font-size: 10px; font-weight: 700; letter-spacing: 0.05em;
  padding: 2px var(--space-2); border-radius: var(--radius-sm);
  text-transform: uppercase;
}
.group--admin { background: var(--color-primary); color: white; }
.group--user  { background: var(--color-bg-muted); color: var(--burgundy-700); border: 1px solid var(--color-border); }

.action-badge {
  font-size: 10px; font-weight: 700; padding: 2px var(--space-2);
  border-radius: var(--radius-sm); text-transform: uppercase; white-space: nowrap;
}
.action--delete  { background: #fdecea; color: var(--color-error); }
.action--danger  { background: #fff3e0; color: #e65100; }
.action--success { background: #eafaf1; color: #27ae60; }
.action--create  { background: #e3f2fd; color: #1565c0; }
.action--default { background: var(--color-bg-muted); color: var(--color-text-muted); }

.empty-state {
  text-align: center; padding: var(--space-12);
  color: var(--cream-600); font-style: italic;
}
</style>
