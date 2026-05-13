<template>
  <div class="date-picker">
    <div class="dp-header">
      <button class="dp-nav" @click="prevMonth">&lsaquo;</button>
      <span class="dp-month">{{ year }}年 {{ month }}月</span>
      <button class="dp-nav" @click="nextMonth">&rsaquo;</button>
    </div>
    <div class="dp-weekdays">
      <span v-for="d in weekdays" :key="d">{{ d }}</span>
    </div>
    <div class="dp-grid">
      <button
        v-for="(day, i) in days"
        :key="i"
        class="dp-day"
        :class="{
          'dp-day--empty': !day,
          'dp-day--selected': day === selectedDay && month === selectedMonth && year === selectedYear,
          'dp-day--today': isToday(day),
          'dp-day--past': day && isPast(day),
          'dp-day--disabled': day && !isSelectable(day)
        }"
        :disabled="!day || !isSelectable(day)"
        @click="day && isSelectable(day) && selectDay(day)"
      >
        {{ day || '' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const weekdays = ['一', '二', '三', '四', '五', '六', '日']

const now = new Date()
const todayYear = now.getFullYear()
const todayMonth = now.getMonth() + 1
const todayDay = now.getDate()

const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const selectedYear = ref(now.getFullYear())
const selectedMonth = ref(now.getMonth() + 1)
const selectedDay = ref(null)

if (props.modelValue) {
  const parts = props.modelValue.split('-')
  selectedYear.value = parseInt(parts[0])
  selectedMonth.value = parseInt(parts[1])
  selectedDay.value = parseInt(parts[2])
  year.value = selectedYear.value
  month.value = selectedMonth.value
}

const days = computed(() => {
  const firstDay = new Date(year.value, month.value - 1, 1)
  const lastDay = new Date(year.value, month.value, 0)
  const startDow = firstDay.getDay()
  const padStart = startDow === 0 ? 6 : startDow - 1

  const arr = []
  for (let i = 0; i < padStart; i++) arr.push(null)
  for (let d = 1; d <= lastDay.getDate(); d++) arr.push(d)
  return arr
})

function isToday(day) {
  return year.value === todayYear && month.value === todayMonth && day === todayDay
}

function isPast(day) {
  const d = new Date(year.value, month.value - 1, day)
  const t = new Date(todayYear, todayMonth - 1, todayDay)
  return d < t
}

function isSelectable(day) {
  if (!day) return false
  const d = new Date(year.value, month.value - 1, day)
  const t = new Date(todayYear, todayMonth - 1, todayDay)
  const max = new Date(todayYear, todayMonth - 1, todayDay + 30)
  return d >= t && d <= max
}

function selectDay(day) {
  selectedDay.value = day
  selectedYear.value = year.value
  selectedMonth.value = month.value
  const m = String(month.value).padStart(2, '0')
  const d = String(day).padStart(2, '0')
  const val = `${year.value}-${m}-${d}`
  emit('update:modelValue', val)
  emit('change', val)
}

function prevMonth() {
  if (month.value === 1) { month.value = 12; year.value-- }
  else month.value--
}

function nextMonth() {
  if (month.value === 12) { month.value = 1; year.value++ }
  else month.value++
}
</script>

<style scoped>
.date-picker {
  width: 100%; max-width: 360px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.dp-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}

.dp-month {
  font-family: var(--font-display);
  font-size: 1.05rem;
  color: var(--text-primary);
}

.dp-nav {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: var(--bg-surface);
  border: 1px solid var(--border); border-radius: var(--radius);
  color: var(--text-secondary); font-size: 1.2rem;
  cursor: pointer; transition: all 0.2s;
}
.dp-nav:hover { border-color: var(--accent-dim); color: var(--text-primary); }

.dp-weekdays {
  display: grid; grid-template-columns: repeat(7, 1fr);
  margin-bottom: 6px;
}
.dp-weekdays span {
  text-align: center; font-size: 0.7rem;
  color: var(--text-muted); padding: 6px 0;
  text-transform: uppercase; letter-spacing: 0.05em;
}

.dp-grid {
  display: grid; grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.dp-day {
  aspect-ratio: 1;
  display: flex; align-items: center; justify-content: center;
  background: none; border: none; border-radius: var(--radius);
  color: var(--text-primary); font-size: 0.85rem;
  cursor: pointer; transition: all 0.15s;
}

.dp-day:hover:not(:disabled) { background: var(--bg-elevated); }

.dp-day--selected {
  background: var(--accent) !important;
  color: var(--bg-root) !important;
  font-weight: 700;
}

.dp-day--today {
  border: 1px solid var(--accent);
  color: var(--accent);
  font-weight: 600;
}
.dp-day--today.dp-day--selected {
  border-color: var(--accent);
}

.dp-day--past { color: var(--text-muted); opacity: 0.4; }
.dp-day--disabled { color: var(--text-muted); opacity: 0.3; cursor: not-allowed; }
.dp-day--empty { cursor: default; }
</style>
