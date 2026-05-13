<template>
  <span class="badge" :class="'badge--' + status">
    <span class="badge-dot"></span>
    {{ label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const label = computed(() => {
  switch (props.status) {
    case 'PENDING': return '待确认'
    case 'CONFIRMED': return '已确认'
    case 'CANCELLED': return '已取消'
    default: return props.status
  }
})
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.badge--PENDING {
  background: rgba(201, 165, 75, 0.12);
  color: var(--gold);
}
.badge--PENDING .badge-dot { background: var(--gold); }

.badge--CONFIRMED {
  background: rgba(74, 222, 128, 0.12);
  color: var(--accent);
}
.badge--CONFIRMED .badge-dot { background: var(--accent); }

.badge--CANCELLED {
  background: rgba(224, 82, 82, 0.1);
  color: var(--danger);
}
.badge--CANCELLED .badge-dot { background: var(--danger); }
</style>
