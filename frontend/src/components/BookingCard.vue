<template>
  <div class="booking-card" :class="{ 'booking-card--cancelled': booking.status === 'CANCELLED' }">
    <div class="bc-left">
      <div class="bc-venue">{{ venueName }}</div>
      <div class="bc-meta">
        <span class="bc-date">{{ booking.bookingDate }}</span>
        <span class="bc-time">{{ booking.startTime }} - {{ booking.endTime }}</span>
      </div>
    </div>
    <div class="bc-right">
      <StatusBadge :status="booking.status" />
      <div v-if="booking.status === 'PENDING'" class="bc-actions">
        <button class="bc-btn bc-btn--confirm" @click="$emit('confirm', booking.id)">
          确认
        </button>
        <button class="bc-btn bc-btn--cancel" @click="$emit('cancel', booking.id)">
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import StatusBadge from './StatusBadge.vue'

defineProps({
  booking: { type: Object, required: true },
  venueName: { type: String, default: '' }
})

defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.booking-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  transition: all 0.2s ease;
}

.booking-card:hover {
  border-color: var(--border-light);
}

.booking-card--cancelled {
  opacity: 0.55;
}

.bc-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bc-venue {
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 600;
}

.bc-meta {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.bc-date {
  color: var(--text-muted);
}

.bc-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bc-actions {
  display: flex;
  gap: 8px;
}

.bc-btn {
  padding: 6px 16px;
  border-radius: var(--radius);
  font-size: 0.8rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.bc-btn--confirm {
  background: var(--accent);
  color: var(--bg-root);
}

.bc-btn--confirm:hover {
  background: #6aee9a;
}

.bc-btn--cancel {
  background: transparent;
  color: var(--danger);
  border: 1px solid var(--danger);
}

.bc-btn--cancel:hover {
  background: var(--danger-bg);
}
</style>
