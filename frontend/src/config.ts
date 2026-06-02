// config.ts — URL backend dinamic: Railway în prod, localhost în dev
const isProd = import.meta.env.PROD

export const BACKEND_URL = isProd
  ? import.meta.env.VITE_BACKEND_URL   // setat în Vercel env vars
  : 'http://localhost:8080'

export const WS_URL = isProd
  ? import.meta.env.VITE_BACKEND_URL?.replace('https://', 'wss://').replace('http://', 'ws://')
  : 'ws://localhost:8080'
