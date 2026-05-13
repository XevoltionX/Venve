<template>
  <div class="slot-picker">
    <div class="slot-header">
      <h3 class="slot-title">选择时段</h3>
      <span class="slot-subtitle">{{ date }}</span>
    </div>
    <div v-if="slots.length === 0" class="slot-empty">
      该日期没有可用时段
    </div>
    <div v-else class="slot-grid">
      <button
        v-for="slot in slots"
        :key="slot.start"
        class="slot-btn"
        :class="{ 'slot-btn--active': isSelected(slot) }"
        @click="$emit('select', slot)"
      >
        <span class="slot-time">{{ slot.start }} - {{ slot.end }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  slots: { type: Array, required: true },
  date: { type: String, default: '' },
  selected: { type: Object, default: null }
})

const emit = defineEmits(['select'])

function isSelected(slot) {
  return props.selected?.start === slot.start && props.selected?.end === slot.end
}
</script>

<style scoped>
.slot-picker {
  margin-top: 24px;
}

.slot-header {
  margin-bottom: 16px;
}

.slot-title {
  font-size: 1.05rem;
  color: var(--text-primary);
}

.slot-subtitle {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 2px;
  display: block;
}

.slot-empty {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-muted);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border);
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 10px;
}

.slot-btn {
  padding: 14px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.slot-btn:hover {
  border-color: var(--accent-dim);
  color: var(--text-primary);
  background: var(--bg-elevated);
}

.slot-btn--active {
  border-color: var(--accent);
  color: var(--accent);
  background: rgba(74, 222, 128, 0.08);
  box-shadow: 0 0 0 1px var(--accent);
}

.slot-time {
  display: block;
}
</style>
