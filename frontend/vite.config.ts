import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  define: {
    global: 'globalThis',
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: true,   // accesibil din rețea (laptop B / VM)
    proxy: {
      // REST API
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // GraphQL
      '/graphql': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // WebSocket — SockJS merge direct la backend, nu prin proxy
      // (de aceea useWebSocket.ts folosește portul 8080 direct)
    }
  }
})
