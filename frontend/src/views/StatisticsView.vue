<script setup lang="ts">
import { onUnmounted } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale, LinearScale,
  BarElement, Title, Tooltip, Legend,
} from 'chart.js'
import { useStatisticsStore } from '@/stores/statistics'
import { useMeetupsStore } from '@/stores/meetups'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const stats   = useStatisticsStore()
const meetups = useMeetupsStore()

// Oprește thread-ul dacă userul navighează în altă parte
onUnmounted(() => stats.stopThread())

// ── View type ─────────────────────────────────────────────────
import { ref } from 'vue'
const viewType    = ref<'Bar Chart' | 'Tabular View'>('Bar Chart')
const viewOptions = ['Bar Chart', 'Tabular View']

// ── Speed options ─────────────────────────────────────────────
const speedOptions = [
  { label: 'Slow (5s)',   ms: 5000 },
  { label: 'Normal (2s)', ms: 2000 },
  { label: 'Fast (0.5s)', ms: 500  },
]

const DAYS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']

// ── Chart options ─────────────────────────────────────────────
const mainChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  animation: { duration: 400 },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#2a0a10',
      titleFont: { family: 'Inter', size: 12 },
      bodyFont:  { family: 'Inter', size: 11 },
    },
  },
  scales: {
    x: {
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: { font: { family: 'Inter', size: 11 }, color: '#621220' },
    },
    y: {
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: { font: { family: 'Inter', size: 11 }, color: '#621220', stepSize: 1 },
      beginAtZero: true,
    },
  },
}

const horizontalOptions = {
  ...mainChartOptions,
  indexAxis: 'y' as const,
  scales: {
    x: { ...mainChartOptions.scales.x, beginAtZero: true },
    y: { grid: { display: false }, ticks: { font: { family: 'Inter', size: 11 }, color: '#621220' } },
  },
}
</script>

<template>
  <div class="stats-page">

    <!-- ── Header ── -->
    <div class="stats-header">
      <h1 class="stats-title">Statistics</h1>
      <select v-model="viewType" class="chart-select">
        <option v-for="opt in viewOptions" :key="opt">{{ opt }}</option>
      </select>
    </div>

    <!-- ── Thread control panel ── -->
    <div class="thread-panel">
      <div class="thread-panel__left">
        <div class="thread-info">
          <span class="thread-label">Auto-generator</span>
          <span class="thread-counter">
            {{ meetups.totalCount }} total meet-ups
            <span v-if="stats.addedByThread > 0" class="thread-added">
              (+{{ stats.addedByThread }} generated)
            </span>
          </span>
        </div>

        <!-- Speed selector -->
        <div class="speed-group">
          <span class="speed-label">Speed:</span>
          <button
            v-for="opt in speedOptions"
            :key="opt.ms"
            class="speed-btn"
            :class="{ active: stats.threadSpeed === opt.ms }"
            @click="stats.setSpeed(opt.ms)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <!-- Toggle -->
      <button
        class="thread-toggle"
        :class="{ running: stats.threadRunning }"
        @click="stats.toggleThread()"
      >
        <span class="thread-toggle__dot" />
        {{ stats.threadRunning ? 'Stop Generator' : 'Start Generator' }}
      </button>
    </div>

    <!-- ── Most Discussed Books ── -->
    <div class="card card--main">
      <div class="card__header">
        <h2 class="card__title">Most Discussed Books in Meet-ups</h2>
      </div>

      <div class="card__body">

        <!-- BAR CHART -->
        <template v-if="viewType === 'Bar Chart'">
          <div class="chart-wrap">
            <Bar :data="stats.barChartData" :options="mainChartOptions" />
          </div>
          <div class="legend">
            <div v-for="(book, i) in stats.topBooks" :key="book.bookID" class="legend__item">
              <span class="legend__dot" :style="{ background: ['#D4A017','#8B1A2F','#bf6280','#7a1628','#d490a4'][i] }" />
              <span class="legend__label">{{ book.title }}</span>
              <span class="legend__pct">{{ book.percent }}%</span>
            </div>
            <div v-if="stats.topBooks.length === 0" class="legend__empty">No data yet.</div>
          </div>
        </template>

        <!-- TABULAR VIEW -->
        <template v-else>
          <div class="table-outer">
            <table class="stats-table">
              <thead>
              <tr>
                <th class="th-book">Book</th>
                <th v-for="day in DAYS" :key="day">{{ day }}</th>
                <th>TOTAL</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(book, i) in stats.topBooks" :key="book.bookID">
                <td class="td-book">{{ book.title }}</td>
                <td v-for="(count, j) in book.counts" :key="j">{{ count }}</td>
                <td class="td-total">{{ book.total }}</td>
              </tr>
              <tr v-if="stats.topBooks.length === 0">
                <td colspan="9" class="empty-state">No data yet.</td>
              </tr>
              </tbody>
            </table>
          </div>
          <div class="legend">
            <div v-for="(book, i) in stats.topBooks" :key="book.bookID" class="legend__item">
              <span class="legend__dot" :style="{ background: ['#D4A017','#8B1A2F','#bf6280','#7a1628','#d490a4'][i] }" />
              <span class="legend__label">{{ book.title }}</span>
              <span class="legend__pct">{{ book.percent }}%</span>
            </div>
          </div>
        </template>

      </div>
    </div>

    <!-- ── Bottom row ── -->
    <div class="bottom-row">
      <div class="card">
        <h2 class="card__title">Active Cities</h2>
        <div class="chart-wrap chart-wrap--sm">
          <Bar v-if="stats.activeCities.length > 0" :data="stats.citiesChartData" :options="horizontalOptions" />
          <p v-else class="chart-empty">No data yet.</p>
        </div>
      </div>
      <div class="card">
        <h2 class="card__title">Popular Locations</h2>
        <div class="chart-wrap chart-wrap--sm">
          <Bar v-if="stats.popularLocations.length > 0" :data="stats.locationsChartData" :options="horizontalOptions" />
          <p v-else class="chart-empty">No data yet.</p>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.stats-page {
  flex: 1;
  padding: var(--space-5) 1.75rem;
  background: var(--color-bg-muted);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  font-family: var(--font-serif);
}

/* Header */
.stats-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.stats-title {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-text);
  margin: 0;
}
.chart-select {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  padding: var(--space-2) var(--space-3);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-soft);
  color: var(--color-text);
  cursor: pointer;
  outline: none;
}

/* ── Thread panel ── */
.thread-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-sm);
  border-left: 4px solid var(--color-border);
  transition: border-color var(--transition-fast);
}
.thread-panel:has(.thread-toggle.running) {
  border-left-color: #2ecc71;
}

.thread-panel__left {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.thread-info { display: flex; flex-direction: column; gap: 2px; }

.thread-label {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--burgundy-700);
}
.thread-counter {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--color-text);
}
.thread-added {
  color: #2ecc71;
  font-weight: 600;
  margin-left: var(--space-1);
}

/* Speed buttons */
.speed-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.speed-label {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  color: var(--burgundy-600);
}
.speed-btn {
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-muted);
  color: var(--color-text);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.speed-btn:hover  { border-color: var(--color-primary); }
.speed-btn.active { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }

/* Toggle button */
.thread-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-full);
  border: 2px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.thread-toggle:hover { background: var(--color-primary); color: var(--color-text-inverse); }

.thread-toggle.running {
  background: #2ecc71;
  border-color: #2ecc71;
  color: white;
}
.thread-toggle.running:hover {
  background: #27ae60;
  border-color: #27ae60;
}

.thread-toggle__dot {
  width: 8px; height: 8px;
  border-radius: var(--radius-full);
  background: currentColor;
  animation: none;
}
.thread-toggle.running .thread-toggle__dot {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.4; transform: scale(0.7); }
}

/* Cards */
.card {
  background: var(--color-bg-soft);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
}
.card--main { flex-shrink: 0; }
.card__header { margin-bottom: var(--space-4); }
.card__title {
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 var(--space-4);
}
.card__body {
  display: grid;
  grid-template-columns: 1fr 180px;
  gap: var(--space-5);
  align-items: start;
}

/* Chart */
.chart-wrap     { height: 260px; position: relative; }
.chart-wrap--sm { height: 200px; }
.chart-empty {
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--cream-600);
  font-style: italic;
  text-align: center;
  padding-top: var(--space-8);
}

/* Table */
.table-outer { overflow-x: auto; }
.stats-table {
  width: 100%;
  border-collapse: collapse;
  font-family: var(--font-sans);
  font-size: var(--text-xs);
}
.stats-table th {
  background: var(--color-accent);
  color: var(--color-text);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: var(--space-2) var(--space-3);
  text-align: center;
  border-bottom: 2px solid var(--color-border);
}
.th-book { text-align: left; min-width: 160px; }
.stats-table tbody tr { border-bottom: 1px solid var(--color-border); }
.stats-table tbody tr:hover { background: var(--color-bg-muted); }
.stats-table td { padding: var(--space-2) var(--space-3); text-align: center; color: var(--color-text); }
.td-book  { text-align: left; font-weight: 600; color: var(--burgundy-700); }
.td-total { font-weight: 700; color: var(--color-primary); }
.empty-state { text-align: center; padding: var(--space-8); color: var(--cream-600); font-style: italic; }

/* Legend */
.legend { display: flex; flex-direction: column; gap: var(--space-2); padding-top: var(--space-2); }
.legend__item { display: flex; align-items: center; gap: var(--space-2); }
.legend__dot  { width: 10px; height: 10px; border-radius: var(--radius-full); flex-shrink: 0; }
.legend__label { font-family: var(--font-sans); font-size: var(--text-xs); color: var(--color-text); flex: 1; line-height: 1.3; }
.legend__pct   { font-family: var(--font-sans); font-size: var(--text-xs); font-weight: 600; color: var(--burgundy-700); }
.legend__empty { font-family: var(--font-sans); font-size: var(--text-xs); color: var(--cream-600); font-style: italic; }

/* Bottom row */
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
}

@media (max-width: 768px) {
  .card__body { grid-template-columns: 1fr; }
  .bottom-row { grid-template-columns: 1fr; }
  .thread-panel { flex-direction: column; gap: var(--space-3); align-items: flex-start; }
}
</style>
