<template>
  <div class="page">
    <div class="page-hero">
      <p class="hero-overline">选择球台</p>
      <h1 class="hero-title">预约乒乓球台</h1>
      <p class="hero-desc">选择一个可用的球台，开始你的运动时光</p>
      <div class="hero-line"></div>
    </div>

    <div class="filter-bar">
      <button
        v-for="area in areas"
        :key="area.value"
        class="filter-chip"
        :class="{ 'filter-chip--active': activeArea === area.value }"
        @click="switchArea(area.value)"
      >
        {{ area.label }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
      加载中...
    </div>

    <div v-else-if="error" class="error-state">
      {{ error }}
      <button class="retry-btn" @click="loadVenues">重试</button>
    </div>

    <TransitionGroup v-else tag="div" name="card" class="venue-grid">
      <VenueCard
        v-for="(v, i) in venues"
        :key="v.id"
        :venue="v"
        :index="i + 1"
        :style="{ transitionDelay: `${i * 60}ms` }"
      />
    </TransitionGroup>

    <div v-if="!loading && !error && venues.length === 0" class="empty-state">
      该区域暂无可用球台
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchVenues } from '../api'
import VenueCard from '../components/VenueCard.vue'

const areas = [
  { label: '全部', value: '' },
  { label: 'A 区', value: 'A区' },
  { label: 'B 区', value: 'B区' },
  { label: 'C 区', value: 'C区' }
]

const venues = ref([])
const loading = ref(true)
const error = ref('')
const activeArea = ref('')

async function loadVenues() {
  loading.value = true
  error.value = ''
  try {
    venues.value = await fetchVenues(activeArea.value || undefined)
  } catch (e) {
    error.value = '无法加载球台列表，请确认后端已启动'
  } finally {
    loading.value = false
  }
}

function switchArea(area) {
  activeArea.value = area
  loadVenues()
}

onMounted(loadVenues)
</script>

<style scoped>
.page { padding: 40px 0; }

.page-hero { margin-bottom: 32px; }

.hero-overline {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--accent);
  margin-bottom: 12px;
  font-weight: 500;
}

.hero-title {
  font-size: 2.4rem;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.hero-desc {
  margin-top: 10px;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.hero-line {
  width: 48px;
  height: 3px;
  background: var(--accent);
  margin-top: 20px;
  border-radius: 2px;
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
}

.filter-chip {
  padding: 8px 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 24px;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-chip:hover {
  border-color: var(--accent-dim);
  color: var(--text-primary);
}

.filter-chip--active {
  background: rgba(74, 222, 128, 0.1);
  border-color: var(--accent);
  color: var(--accent);
}

.venue-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  font-size: 0.95rem;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 10px;
  vertical-align: -4px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.error-state { color: var(--danger); }

.retry-btn {
  display: block;
  margin: 16px auto 0;
  padding: 8px 24px;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border-radius: var(--radius);
  font-size: 0.9rem;
  cursor: pointer;
}
</style>
