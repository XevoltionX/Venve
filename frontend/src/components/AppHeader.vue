<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <span class="logo-mark">&#9670;</span>
        <span class="logo-text">VENVE</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/" class="nav-link" active-class="nav-link--active" exact-active-class="nav-link--active">
          <span class="nav-icon">&#9670;</span>首页
        </router-link>
        <router-link to="/venues" class="nav-link" active-class="nav-link--active">
          <span class="nav-icon">&#9702;</span>球台
        </router-link>
        <router-link to="/bookings" class="nav-link" active-class="nav-link--active">
          <span class="nav-icon">&#9783;</span>我的预约
        </router-link>
        <template v-if="isAdmin">
          <router-link to="/monitor" class="nav-link" active-class="nav-link--active">
            <span class="nav-icon">&#9731;</span>监控
          </router-link>
          <router-link to="/admin" class="nav-link" active-class="nav-link--active">
            <span class="nav-icon">&#9881;</span>管理
          </router-link>
        </template>
      </nav>
      <div class="user-area" v-if="userName">
        <router-link to="/notifications" class="bell-btn" title="消息通知">
          <span class="bell-icon">&#128276;</span>
          <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </router-link>
        <span class="user-name">&#9787; {{ userName }}</span>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </div>
      <div class="user-area" v-else>
        <router-link to="/login" class="nav-link">登录</router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { fetchUnreadCount } from '../api'

const router = useRouter()
const route = useRoute()
const userName = ref(localStorage.getItem('userName') || '')
const isAdmin = ref(localStorage.getItem('role') === 'ADMIN')
const unreadCount = ref(0)

let pollTimer = null

watch(() => route.fullPath, () => {
  userName.value = localStorage.getItem('userName') || ''
  isAdmin.value = localStorage.getItem('role') === 'ADMIN'
})

async function updateUnread() {
  if (!userName.value) return
  try {
    const data = await fetchUnreadCount()
    unreadCount.value = data.count || 0
  } catch { /* ignore */ }
}

onMounted(() => {
  updateUnread()
  pollTimer = setInterval(updateUnread, 15000)
})

onUnmounted(() => {
  clearInterval(pollTimer)
})

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userName')
  localStorage.removeItem('role')
  userName.value = ''
  isAdmin.value = false
  unreadCount.value = 0
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  background: rgba(7, 19, 13, 0.85);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
}
.header-inner {
  max-width: 1080px; margin: 0 auto; padding: 0 20px; height: 56px;
  display: flex; align-items: center; justify-content: space-between;
}
.logo {
  display: flex; align-items: center; gap: 10px;
  text-decoration: none; color: var(--text-primary);
}
.logo-mark { font-size: 22px; color: var(--accent); }
.logo-text {
  font-family: var(--font-display); font-size: 1.25rem;
  font-weight: 600; letter-spacing: 0.08em;
}
.nav-links { display: flex; gap: 4px; }
.nav-link {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border-radius: var(--radius);
  color: var(--text-secondary); text-decoration: none;
  font-size: 0.85rem; font-weight: 500; transition: all 0.2s ease;
}
.nav-link:hover { color: var(--text-primary); background: var(--bg-elevated); }
.nav-link--active { color: var(--accent); background: rgba(74, 222, 128, 0.06); }
.nav-icon { font-size: 1rem; }
.user-area { display: flex; align-items: center; gap: 12px; }
.user-name { color: var(--text-secondary); font-size: 0.85rem; }
.logout-btn {
  padding: 5px 14px; background: transparent;
  border: 1px solid var(--border); border-radius: var(--radius);
  color: var(--text-muted); font-size: 0.8rem; cursor: pointer; transition: all 0.2s;
}
.logout-btn:hover { border-color: var(--danger); color: var(--danger); }
.bell-btn {
  position: relative; display: flex; align-items: center;
  text-decoration: none; color: var(--text-secondary); font-size: 1.2rem;
}
.bell-btn:hover { color: var(--accent); }
.bell-icon { line-height: 1; }
.bell-badge {
  position: absolute; top: -6px; right: -8px;
  min-width: 16px; height: 16px; padding: 0 4px;
  background: var(--danger); color: #fff; border-radius: 10px;
  font-size: 0.65rem; font-weight: 600; text-align: center; line-height: 16px;
}
</style>
