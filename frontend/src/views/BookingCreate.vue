<template>
  <div class="page">
    <button class="back-btn" @click="$router.push('/')">
      <span>&#8592;</span> 返回球台列表
    </button>

    <div class="page-hero">
      <p class="hero-overline">创建预约</p>
      <h1 class="hero-title">{{ venue?.name || '加载中...' }}</h1>
      <p class="hero-desc">{{ venue?.location }}</p>
      <div class="hero-line"></div>
    </div>

    <div v-if="venue" class="venue-info-card">
      <div class="info-row" v-if="venue.description">
        <span class="info-label">简介</span>
        <span class="info-value">{{ venue.description }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">营业时间</span>
        <span class="info-value">{{ venue.businessHours || '09:00-21:00' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">球台数量</span>
        <span class="info-value">{{ venue.tableCount || 1 }} 张</span>
      </div>
      <div class="info-row">
        <span class="info-label">价格</span>
        <span class="info-value info-price">¥{{ venue.pricePerHour || 30 }} / 小时</span>
      </div>
    </div>

    <div class="form-section">
      <label class="field-label">选择日期</label>
      <DatePicker v-model="selectedDate" @change="loadSlots" />
    </div>

    <div v-if="dateSelected" class="form-section">
      <SlotPicker
        :slots="slots"
        :date="selectedDate"
        :selected="selectedSlot"
        @select="selectedSlot = $event"
      />
    </div>

    <div v-if="selectedSlot" class="form-section booking-form">
      <div class="booking-summary">
        <div class="summary-row">
          <span class="summary-label">预约人</span>
          <span>{{ userName }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">球台</span>
          <span>{{ venue?.name }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">日期</span>
          <span>{{ selectedDate }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">时段</span>
          <span class="summary-accent">{{ selectedSlot.start }} - {{ selectedSlot.end }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">预估费用</span>
          <span class="summary-accent">¥{{ venue?.pricePerHour || 30 }}</span>
        </div>
      </div>
      <button
        class="submit-btn"
        :disabled="submitting"
        @click="handleSubmit"
      >
        <span v-if="submitting" class="loading-spinner"></span>
        {{ submitting ? '提交中...' : '确认预约' }}
      </button>
      <p v-if="submitError" class="submit-error">{{ submitError }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchSlots, createBooking, fetchVenues } from '../api'
import SlotPicker from '../components/SlotPicker.vue'
import DatePicker from '../components/DatePicker.vue'

const route = useRoute()
const router = useRouter()
const venueId = computed(() => route.params.venueId)
const userName = localStorage.getItem('userName') || ''

const venue = ref(null)
const slots = ref([])
const selectedDate = ref('')
const selectedSlot = ref(null)
const submitting = ref(false)
const submitError = ref('')

const today = new Date().toISOString().split('T')[0]
const dateSelected = computed(() => !!selectedDate.value)

async function loadSlots() {
  if (!selectedDate.value) return
  selectedSlot.value = null
  try {
    slots.value = await fetchSlots(venueId.value, selectedDate.value)
  } catch {
    slots.value = []
  }
}

onMounted(async () => {
  try {
    const venues = await fetchVenues()
    venue.value = venues.find(v => v.id == venueId.value) || null
  } catch { /* ignore */ }
})

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true
  submitError.value = ''
  try {
    await createBooking({
      venueId: venueId.value,
      bookingDate: selectedDate.value,
      startTime: selectedSlot.value.start,
      endTime: selectedSlot.value.end
    })
    router.push('/bookings')
  } catch (e) {
    submitError.value = e.response?.data?.message || e.message || '预约失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { padding: 40px 0; max-width: 640px; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: none;
  color: var(--text-secondary);
  font-size: 0.85rem;
  padding: 6px 0;
  margin-bottom: 28px;
  transition: color 0.2s;
  cursor: pointer;
}
.back-btn:hover { color: var(--text-primary); }

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
.hero-desc { margin-top: 6px; color: var(--text-secondary); font-size: 0.9rem; }
.hero-line {
  width: 48px;
  height: 3px;
  background: var(--accent);
  margin-top: 16px;
  border-radius: 2px;
}

.venue-info-card {
  padding: 20px 22px; margin-bottom: 24px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius);
}
.info-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 0.85rem; }
.info-label { color: var(--text-muted); }
.info-value { color: var(--text-primary); }
.info-price { color: var(--accent); font-weight: 600; }

.form-section { margin-bottom: 28px; }

.field-label {
  display: block;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
  margin-bottom: 10px;
  font-weight: 500;
}

.date-input {
  width: 100%;
  padding: 14px 18px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-primary);
  font-size: 0.95rem;
  transition: border-color 0.2s;
}
.date-input:focus { border-color: var(--accent-dim); }

.booking-form {
  padding: 28px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.booking-summary {
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-surface);
  border-radius: var(--radius);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}
.summary-label { color: var(--text-muted); }
.summary-accent { color: var(--accent); font-weight: 500; }

.submit-btn {
  width: 100%;
  padding: 14px;
  background: var(--accent);
  color: var(--bg-root);
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.submit-btn:hover:not(:disabled) { background: #6aee9a; }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.submit-error {
  color: var(--danger);
  font-size: 0.85rem;
  text-align: center;
  margin-top: 14px;
}

.loading-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(7, 19, 13, 0.3);
  border-top-color: var(--bg-root);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
