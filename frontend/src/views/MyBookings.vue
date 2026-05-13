<template>
  <div class="page">
    <div class="page-hero">
      <p class="hero-overline">预约管理</p>
      <h1 class="hero-title">我的预约</h1>
      <div class="hero-line"></div>
    </div>

    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
      加载中...
    </div>

    <div v-else-if="error" class="error-state">{{ error }}</div>

    <div v-else-if="loaded && bookings.length === 0" class="empty-state">
      暂无预约记录
    </div>

    <TransitionGroup v-else-if="loaded" tag="div" name="card" class="booking-list">
      <BookingCard
        v-for="(b, i) in bookings"
        :key="b.id"
        :booking="b"
        :venue-name="getVenueName(b.venueId)"
        :style="{ transitionDelay: `${i * 60}ms` }"
        @confirm="handleConfirm"
        @cancel="handleCancel"
      />
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchMyBookings, confirmBooking, cancelBooking, fetchVenues } from '../api'
import BookingCard from '../components/BookingCard.vue'

const bookings = ref([])
const loading = ref(true)
const error = ref('')
const loaded = ref(false)
const venueMap = ref({})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [bookingData, venues] = await Promise.all([
      fetchMyBookings(),
      fetchVenues()
    ])
    bookings.value = bookingData
    venueMap.value = {}
    venues.forEach(v => { venueMap.value[v.id] = v.name })
    loaded.value = true
  } catch {
    error.value = '加载失败，请确认后端已启动'
  } finally {
    loading.value = false
  }
}

function getVenueName(venueId) {
  return venueMap.value[venueId] || `ID:${venueId}号台`
}

async function handleConfirm(id) {
  try {
    await confirmBooking(id)
    const b = bookings.value.find(x => x.id === id)
    if (b) b.status = 'CONFIRMED'
  } catch (e) {
    alert(e.response?.data?.message || e.message || '确认失败')
  }
}

async function handleCancel(id) {
  try {
    await cancelBooking(id)
    const b = bookings.value.find(x => x.id === id)
    if (b) b.status = 'CANCELLED'
  } catch (e) {
    alert(e.response?.data?.message || e.message || '取消失败')
  }
}

onMounted(load)
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
.hero-title { font-size: 2rem; font-weight: 600; }
.hero-line {
  width: 48px;
  height: 3px;
  background: var(--accent);
  margin-top: 16px;
  border-radius: 2px;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
  font-size: 0.95rem;
}
.error-state { color: var(--danger); }

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
</style>
