<template>
  <div class="page">
    <div class="page-hero">
      <p class="hero-overline">消息通知</p>
      <h1 class="hero-title">消息中心</h1>
      <div class="hero-line"></div>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <TransitionGroup v-else tag="div" name="card" class="notif-list">
      <div
        v-for="n in notifications"
        :key="n.id"
        class="notif-card"
        :class="{ 'notif-card--unread': !n.isRead }"
        @click="handleRead(n)"
      >
        <div class="notif-left">
          <span class="notif-dot" v-if="!n.isRead"></span>
          <div>
            <div class="notif-title">{{ n.title }}</div>
            <div class="notif-content">{{ n.content }}</div>
          </div>
        </div>
        <div class="notif-right">
          <span class="notif-type">{{ typeLabel(n.type) }}</span>
          <span class="notif-time">{{ formatTime(n.createdAt) }}</span>
        </div>
      </div>
    </TransitionGroup>

    <div v-if="!loading && notifications.length === 0" class="empty-state">
      暂无消息
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchNotifications, markNotificationRead } from '../api'

const notifications = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    notifications.value = await fetchNotifications()
  } catch { /* ignore */ } finally { loading.value = false }
}

async function handleRead(n) {
  if (n.isRead) return
  try {
    await markNotificationRead(n.id)
    n.isRead = true
  } catch { /* ignore */ }
}

function typeLabel(t) {
  const map = {
    BOOKING_CREATED: '预约创建', BOOKING_CONFIRMED: '已确认',
    BOOKING_CANCELLED: '已取消', BOOKING_TIMEOUT: '已超时'
  }
  return map[t] || t
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(load)
</script>

<style scoped>
.page { padding: 40px 0; max-width: 720px; }
.page-hero { margin-bottom: 28px; }
.hero-overline {
  font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.15em;
  color: var(--accent); margin-bottom: 12px; font-weight: 500;
}
.hero-title { font-size: 2rem; font-weight: 600; }
.hero-line {
  width: 48px; height: 3px; background: var(--accent);
  margin-top: 16px; border-radius: 2px;
}

.notif-list { display: flex; flex-direction: column; gap: 8px; }

.notif-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 22px;
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius); cursor: pointer;
  transition: all 0.2s;
}
.notif-card:hover { border-color: var(--border-light); }
.notif-card--unread { border-left: 3px solid var(--accent); }
.notif-left { display: flex; align-items: center; gap: 12px; flex: 1; }
.notif-dot {
  width: 8px; height: 8px; border-radius: 50%; background: var(--accent);
  flex-shrink: 0;
}
.notif-title { font-weight: 500; font-size: 0.95rem; color: var(--text-primary); }
.notif-content { font-size: 0.82rem; color: var(--text-muted); margin-top: 2px; }
.notif-right { text-align: right; flex-shrink: 0; margin-left: 16px; }
.notif-type {
  display: block; font-size: 0.72rem; color: var(--accent);
  text-transform: uppercase; letter-spacing: 0.05em;
}
.notif-time { font-size: 0.72rem; color: var(--text-muted); margin-top: 2px; display: block; }

.loading-state, .empty-state {
  text-align: center; padding: 60px 20px; color: var(--text-muted);
}
</style>
