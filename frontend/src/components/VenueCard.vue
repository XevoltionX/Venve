<template>
  <router-link :to="`/book/${venue.id}`" class="venue-card">
    <div class="card-accent"></div>
    <div class="card-body">
      <div class="card-top">
        <div class="card-index">{{ index }}</div>
        <div>
          <h3 class="card-name">{{ venue.name }}</h3>
          <p class="card-location">{{ venue.location || '位置未标注' }}</p>
        </div>
        <div class="card-status" :class="{ 'card-status--maint': venue.status === 'MAINTENANCE' }">
          {{ venue.status === 'MAINTENANCE' ? '维护中' : '可预约' }}
        </div>
      </div>
      <div class="card-meta">
        <span v-if="venue.description" class="meta-desc">{{ venue.description }}</span>
        <div class="meta-tags">
          <span class="tag">&#9719; {{ venue.businessHours || '09:00-21:00' }}</span>
          <span class="tag">&#9702; {{ venue.tableCount || 1 }}张球台</span>
          <span class="tag tag-price">¥{{ venue.pricePerHour || 30 }}/小时</span>
        </div>
      </div>
    </div>
    <div class="card-arrow">&#8594;</div>
    <div class="card-glow"></div>
  </router-link>
</template>

<script setup>
defineProps({
  venue: { type: Object, required: true },
  index: { type: Number, default: 1 }
})
</script>

<style scoped>
.venue-card {
  position: relative;
  display: flex; align-items: center; gap: 20px;
  padding: 22px 28px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  text-decoration: none; color: var(--text-primary);
  overflow: hidden; transition: all 0.3s ease;
}
.venue-card:hover { border-color: var(--accent-dim); transform: translateY(-2px); }
.venue-card:hover .card-glow { opacity: 0.6; }

.card-accent {
  position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: linear-gradient(180deg, var(--accent), var(--accent-dim));
  border-radius: 0 2px 2px 0;
}

.card-body { flex: 1; }
.card-top { display: flex; align-items: center; gap: 16px; }
.card-index {
  font-family: var(--font-display); font-size: 1.5rem;
  color: var(--text-muted); font-style: italic; min-width: 32px;
}
.card-name { font-size: 1.1rem; margin-bottom: 2px; }
.card-location { font-size: 0.85rem; color: var(--text-secondary); }

.card-status {
  margin-left: auto; padding: 4px 12px; border-radius: 20px;
  font-size: 0.75rem; font-weight: 500;
  background: rgba(74,222,128,0.1); color: var(--accent);
}
.card-status--maint { background: rgba(201,165,75,0.1); color: var(--gold); }

.card-meta { margin-top: 12px; }
.meta-desc { font-size: 0.82rem; color: var(--text-muted); display: block; margin-bottom: 8px; }
.meta-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 10px; background: var(--bg-surface);
  border: 1px solid var(--border); border-radius: 16px;
  font-size: 0.75rem; color: var(--text-secondary);
}
.tag-price { color: var(--accent); border-color: var(--accent-dim); }

.card-arrow {
  font-size: 1.25rem; color: var(--text-muted);
  transition: transform 0.3s ease;
}
.venue-card:hover .card-arrow { transform: translateX(4px); color: var(--accent); }

.card-glow {
  position: absolute; bottom: -20px; right: 40px;
  width: 120px; height: 60px;
  background: radial-gradient(circle, rgba(74,222,128,0.08) 0%, transparent 70%);
  opacity: 0; transition: opacity 0.3s ease; pointer-events: none;
}
</style>
