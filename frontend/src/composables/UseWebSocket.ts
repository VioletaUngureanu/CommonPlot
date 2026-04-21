// ============================================================
//  composables/useWebSocket.ts
//  Silver: WebSocket real-time updates
//  Ascultă /topic/meetups de la backend.
//  Când generatorul adaugă un meetup, clientul e notificat
//  și actualizează tabelul + graficele automat.
// ============================================================

import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { Meetup } from '../types/indexes.ts'

export function useWebSocket(onMeetupReceived: (meetup: Meetup) => void) {
  const connected = ref(false)
  const error     = ref<string | null>(null)

  const client = new Client({
    // SockJS ca fallback pentru browsere fără WebSocket nativ
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 5000,

    onConnect: () => {
      connected.value = true
      error.value     = null

      // Abonare la topicul de meetups
      client.subscribe('/topic/meetups', (message) => {
        try {
          const raw = JSON.parse(message.body)
          // Backend trimite bookId (lowercase), frontend folosește bookID
          const meetup: Meetup = {
            ...raw,
            bookID: raw.bookId ?? raw.bookID,
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

  // Deconectare automată când componenta e distrusă
  onUnmounted(() => disconnect())

  return { connected, error, connect, disconnect }
}
