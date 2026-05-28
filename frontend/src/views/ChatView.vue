<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUsersStore } from '@/stores/users'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { BACKEND_URL } from '@/config'

const users = useUsersStore()

interface ChatMessage {
  id?: string
  username: string
  content: string
  role: string
  sentAt: string
}

const messages    = ref<ChatMessage[]>([])
const newMessage  = ref('')
const connected   = ref(false)
const messagesEnd = ref<HTMLElement | null>(null)

let stompClient: Client | null = null

function connect() {
  stompClient = new Client({
    // ── WebSocket cu JWT în header ────────────────────────
    webSocketFactory: () => new SockJS(`${BACKEND_URL}/ws`),
    connectHeaders: {
      Authorization: users.token ? `Bearer ${users.token}` : '',
    },
    reconnectDelay: 5000,

    onConnect: () => {
      connected.value = true
      stompClient!.subscribe('/topic/chat', (message) => {
        try {
          const msg: ChatMessage = JSON.parse(message.body)
          messages.value.push(msg)
          scrollToBottom()
        } catch (e) {
          console.error('Failed to parse chat message:', e)
        }
      })
    },

    onDisconnect: () => { connected.value = false },
    onStompError: () => { connected.value = false },
  })

  stompClient.activate()
}

function disconnect() {
  stompClient?.deactivate()
  connected.value = false
}

// ── Load history cu JWT ───────────────────────────────────────
async function loadHistory() {
  try {
    const headers: Record<string, string> = {}
    if (users.token) {
      headers['Authorization'] = `Bearer ${users.token}`
    }
    const res = await fetch(`${BACKEND_URL}/api/chat/history`, { headers })
    if (res.ok) {
      messages.value = await res.json()
      scrollToBottom()
    }
  } catch {
    console.error('Could not load chat history')
  }
}

function sendMessage() {
  if (!newMessage.value.trim() || !connected.value) return
  if (!users.currentUser) return

  stompClient!.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      username: users.currentUser.username,
      content:  newMessage.value.trim(),
      role:     users.currentUser.role,
    }),
  })

  newMessage.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

async function scrollToBottom() {
  await nextTick()
  messagesEnd.value?.scrollIntoView({ behavior: 'smooth' })
}

function formatTime(iso: string): string {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString('ro-RO', {
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(async () => {
  await loadHistory()
  connect()
})

onUnmounted(() => disconnect())
</script>

<template>
  <div class="chat-page">

    <!-- Header -->
    <div class="chat-header">
      <div class="chat-header__left">
        <h1 class="chat-header__title">Chat</h1>
        <div class="chat-status" :class="{ 'chat-status--connected': connected }">
          <span class="chat-status__dot" />
          {{ connected ? 'Connected' : 'Connecting...' }}
        </div>
      </div>
      <div class="chat-header__user">
        Logged in as <strong>{{ users.currentUser?.username }}</strong>
        <span class="chat-role-badge" :class="users.currentUser?.role === 'ADMIN' ? 'badge--admin' : 'badge--user'">
          {{ users.currentUser?.role }}
        </span>
      </div>
    </div>

    <!-- Messages -->
    <div class="chat-messages">

      <div v-if="messages.length === 0" class="chat-empty">
        No messages yet. Say hello!
      </div>

      <div
        v-for="(msg, i) in messages"
        :key="msg.id ?? i"
        class="chat-message"
        :class="{
          'chat-message--own':  msg.username === users.currentUser?.username,
          'chat-message--admin': msg.role === 'ADMIN'
        }"
      >
        <div class="chat-bubble">
          <div class="chat-bubble__header">
            <span class="chat-bubble__username">{{ msg.username }}</span>
            <span v-if="msg.role === 'ADMIN'" class="chat-bubble__admin-badge">ADMIN</span>
            <span class="chat-bubble__time">{{ formatTime(msg.sentAt) }}</span>
          </div>
          <p class="chat-bubble__content">{{ msg.content }}</p>
        </div>
      </div>

      <!-- Sentinel pentru scroll -->
      <div ref="messagesEnd" />
    </div>

    <!-- Input -->
    <div class="chat-input-wrap">
      <textarea
        v-model="newMessage"
        class="chat-input"
        placeholder="Type a message... (Enter to send)"
        rows="2"
        :disabled="!connected"
        @keydown="handleKeydown"
      />
      <button
        class="chat-send-btn"
        :disabled="!connected || !newMessage.trim()"
        @click="sendMessage"
      >
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M2 10l16-8-6 8 6 8-16-8z" fill="currentColor"/>
        </svg>
      </button>
    </div>

  </div>
</template>

<style scoped>
.chat-page {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; background: var(--color-bg-muted);
  font-family: var(--font-sans);
}

/* Header */
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1.1rem 1.75rem;
  background: var(--color-bg-soft); border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}
.chat-header__left  { display: flex; align-items: center; gap: var(--space-3); }
.chat-header__title { font-size: var(--text-lg); font-weight: 700; color: var(--color-text); margin: 0; }
.chat-header__user  { font-size: var(--text-sm); color: var(--color-text-muted); display: flex; align-items: center; gap: var(--space-2); }

.chat-status {
  display: flex; align-items: center; gap: var(--space-1);
  font-size: var(--text-xs); color: var(--cream-600);
}
.chat-status__dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--cream-400);
  transition: background var(--transition-fast);
}
.chat-status--connected .chat-status__dot { background: #27ae60; }
.chat-status--connected { color: #27ae60; }

.chat-role-badge {
  font-size: 10px; font-weight: 700; letter-spacing: 0.05em;
  padding: 2px var(--space-2); border-radius: var(--radius-sm);
  text-transform: uppercase;
}
.badge--admin { background: var(--color-primary); color: white; }
.badge--user  { background: var(--color-bg-muted); color: var(--burgundy-700); border: 1px solid var(--color-border); }

/* Messages area */
.chat-messages {
  flex: 1; overflow-y: auto;
  padding: var(--space-4) 1.75rem;
  display: flex; flex-direction: column; gap: var(--space-3);
}

.chat-empty {
  text-align: center; padding: var(--space-12);
  color: var(--cream-600); font-style: italic;
}

/* Message bubble */
.chat-message {
  display: flex;
  justify-content: flex-start;
}
.chat-message--own {
  justify-content: flex-end;
}

.chat-bubble {
  max-width: 65%;
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  box-shadow: var(--shadow-sm);
  border: 1.5px solid var(--color-border);
}
.chat-message--own .chat-bubble {
  background: var(--burgundy-100, #f9eaee);
  border-color: var(--burgundy-200, #f0cdd5);
}
.chat-message--admin .chat-bubble {
  border-color: var(--color-primary);
}

.chat-bubble__header {
  display: flex; align-items: center; gap: var(--space-2);
  margin-bottom: var(--space-1);
}
.chat-bubble__username {
  font-size: var(--text-xs); font-weight: 700;
  color: var(--burgundy-700);
}
.chat-bubble__admin-badge {
  font-size: 9px; font-weight: 700;
  background: var(--color-primary); color: white;
  padding: 1px var(--space-1); border-radius: 3px;
  letter-spacing: 0.05em;
}
.chat-bubble__time {
  font-size: 10px; color: var(--cream-600); margin-left: auto;
}
.chat-bubble__content {
  font-size: var(--text-sm); color: var(--color-text);
  line-height: 1.5; margin: 0; white-space: pre-wrap;
}

/* Input */
.chat-input-wrap {
  display: flex; align-items: flex-end; gap: var(--space-3);
  padding: var(--space-3) 1.75rem;
  background: var(--color-bg-soft); border-top: 1px solid var(--color-border);
  flex-shrink: 0;
}
.chat-input {
  flex: 1; resize: none;
  font-family: var(--font-sans); font-size: var(--text-sm);
  color: var(--color-text); background: var(--color-bg);
  border: 1.5px solid var(--color-border); border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3); outline: none;
  transition: border-color var(--transition-fast);
  line-height: 1.5;
}
.chat-input:focus { border-color: var(--color-primary); }
.chat-input:disabled { opacity: 0.5; cursor: not-allowed; }

.chat-send-btn {
  width: 40px; height: 40px; flex-shrink: 0;
  background: var(--color-primary); color: white;
  border: none; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: background var(--transition-fast);
}
.chat-send-btn:hover:not(:disabled) { background: var(--color-primary-hover); }
.chat-send-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
