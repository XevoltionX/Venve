<template>
  <div class="page">
    <div class="page-hero">
      <p class="hero-overline">系统监控</p>
      <h1 class="hero-title">运行指标</h1>
      <div class="hero-line"></div>
    </div>

    <div class="metrics-overview">
      <div class="metric-card">
        <div class="metric-value" :class="totalQps > 0 ? 'text-accent' : ''">
          {{ totalQps }}
        </div>
        <div class="metric-label">总 QPS</div>
      </div>
      <div class="metric-card">
        <div class="metric-value text-danger">
          {{ totalErrors }}
        </div>
        <div class="metric-label">错误数</div>
      </div>
      <div class="metric-card">
        <div class="metric-value">
          {{ routeCount }}
        </div>
        <div class="metric-label">活跃路由</div>
      </div>
      <button class="refresh-btn" @click="fetchMetrics" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <div class="section-title">路由级指标</div>
    <div class="route-table">
      <div class="table-header">
        <span class="col-path">路径</span>
        <span class="col-qps">QPS</span>
        <span class="col-latency">平均延迟(ms)</span>
        <span class="col-errors">错误数</span>
      </div>
      <div
        v-for="(metric, path) in routes"
        :key="path"
        class="table-row"
      >
        <span class="col-path">{{ path }}</span>
        <span class="col-qps">{{ metric.qps }}</span>
        <span class="col-latency">{{ metric.avgLatencyMs }}</span>
        <span class="col-errors" :class="{ 'has-error': metric.errors > 0 }">
          {{ metric.errors }}
        </span>
      </div>
      <div v-if="Object.keys(routes).length === 0" class="table-empty">
        暂无数据，点击刷新
      </div>
    </div>

    <div class="section-title">外部监控端点</div>
    <div class="link-cards">
      <a href="/actuator/health" target="_blank" class="link-card">
        <span class="link-label">/actuator/health</span>
        <span class="link-desc">健康检查</span>
      </a>
      <a href="/actuator/metrics" target="_blank" class="link-card">
        <span class="link-label">/actuator/metrics</span>
        <span class="link-desc">Micrometer 指标</span>
      </a>
      <a href="/actuator/prometheus" target="_blank" class="link-card">
        <span class="link-label">/actuator/prometheus</span>
        <span class="link-desc">Prometheus 格式</span>
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const totalQps = ref(0)
const totalErrors = ref(0)
const routeCount = ref(0)
const routes = ref({})
const loading = ref(false)

async function fetchMetrics() {
  loading.value = true
  try {
    const resp = await axios.get('/api/monitor')
    const data = resp.data
    totalQps.value = data.totalQps || 0
    totalErrors.value = data.totalErrors || 0
    routes.value = data.routes || {}
    routeCount.value = Object.keys(routes.value).length
  } catch {
    // keep stale data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMetrics()
  window.__monitorTimer = setInterval(fetchMetrics, 5000)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  clearInterval(window.__monitorTimer)
})
</script>

<style scoped>
.page { padding: 40px 0; }

.page-hero { margin-bottom: 28px; }
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
  width: 48px; height: 3px;
  background: var(--accent);
  margin-top: 16px; border-radius: 2px;
}

.metrics-overview {
  display: flex;
  gap: 16px;
  margin-bottom: 36px;
  align-items: center;
  flex-wrap: wrap;
}

.metric-card {
  flex: 1; min-width: 140px;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  text-align: center;
}

.metric-value {
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 600;
  color: var(--text-primary);
}
.metric-value.text-accent { color: var(--accent); }
.metric-value.text-danger { color: var(--danger); }

.metric-label {
  margin-top: 6px;
  font-size: 0.8rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.refresh-btn {
  padding: 14px 28px;
  background: var(--accent);
  color: var(--bg-root);
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s;
}
.refresh-btn:hover:not(:disabled) { background: #6aee9a; }
.refresh-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.section-title {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 600;
  margin-bottom: 16px;
  margin-top: 8px;
}

.route-table {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1.2fr 0.8fr;
  padding: 14px 20px;
  background: var(--bg-surface);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-muted);
  font-weight: 500;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1.2fr 0.8fr;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  font-size: 0.9rem;
  transition: background 0.15s;
}
.table-row:hover { background: var(--bg-surface); }

.col-path { color: var(--text-primary); }
.col-qps { color: var(--accent); font-weight: 500; }
.col-latency { color: var(--text-secondary); font-family: monospace; }
.col-errors { color: var(--text-muted); }
.col-errors.has-error { color: var(--danger); font-weight: 600; }

.table-empty {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.link-cards {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.link-card {
  flex: 1; min-width: 200px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  text-decoration: none;
  transition: border-color 0.2s;
}

.link-card:hover { border-color: var(--accent-dim); }

.link-label {
  display: block;
  color: var(--accent);
  font-family: monospace;
  font-size: 0.85rem;
  margin-bottom: 4px;
}

.link-desc {
  color: var(--text-muted);
  font-size: 0.8rem;
}
</style>
