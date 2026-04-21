/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#8B1A2F',
          dark: '#6B1424',
        },
        accent: '#F5E6CA',
      },
    },
  },
  plugins: [],
}
