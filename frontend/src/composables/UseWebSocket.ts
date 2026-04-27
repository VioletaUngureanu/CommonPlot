// ============================================================
//  composables/useWebSocket.ts
//  Silver: WebSocket real-time updates.
//  URL-ul e dinamic — funcționează atât pe localhost cât și
//  pe rețea (client-server pe laptop-uri diferite).
// ============================================================

import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { Meetup } from '../types/indexes.ts'
import { BACKEND_URL } from '@/config'

// URL-ul backend-ului — dinamic față de origine Vite proxy
// Pe localhost:5173 → proxy trimite la localhost:8080
// Pe 192.168.x.x:5173 → proxy trimite la 192.168.x.x:8080
function getWsUrl(): string {
  const origin = window.location.origin
  // Înlocuiește portul Vite (5173) cu portul backend (8080)
  return origin.replace(':5173', ':8080') + '/ws'
}

export function useWebSocket(onMeetupReceived: (meetup: Meetup) => void) {
  const connected = ref(false)
  const error     = ref<string | null>(null)

  const client = new Client({
    webSocketFactory: () => new SockJS(`${BACKEND_URL}/ws`),
    reconnectDelay: 5000,

    onConnect: () => {
      connected.value = true
      error.value     = null

      client.subscribe('/topic/meetups', (message) => {
        try {
          const raw = JSON.parse(message.body)
          const meetup: Meetup = {
            ...raw,
            bookID: raw.bookID ?? raw.bookId,
          }
          onMeetupReceived(meetup)
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e)
        }
      })
    },

    onDisconnect: () => {
      connected.value = false
    },

    onStompError: (frame) => {
      error.value = frame.headers['message'] ?? 'WebSocket error'
      connected.value = false
    },
  })

  function connect() {
    client.activate()
  }

  function disconnect() {
    client.deactivate()
    connected.value = false
  }

  onUnmounted(() => disconnect())

  return { connected, error, connect, disconnect }
}
