import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import basicSsl from '@vitejs/plugin-basic-ssl'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    basicSsl({
      name: 'commonplot',
      domains: ['localhost'],
    }),
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
    host: true,
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/graphql': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/ws': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
    }
  }
})
