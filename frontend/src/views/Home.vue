<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-badge">&#9670; PREMIUM TABLE TENNIS</div>
      <h1 class="hero-title">
        专业场地<br><span class="hero-accent">即刻预约</span>
      </h1>
      <p class="hero-desc">
        VENVE 致力于为乒乓球爱好者提供高品质的运动空间。覆盖多个区域、灵活时段、透明定价，让你的每一次挥拍都值得。
      </p>
      <div class="hero-actions">
        <router-link to="/venues" class="btn-primary">立即预约</router-link>
        <router-link to="/bookings" class="btn-secondary">我的预约</router-link>
      </div>
    </section>

    <!-- Stats -->
    <section class="stats">
      <div class="stat-item">
        <div class="stat-num">{{ venueCount }}</div>
        <div class="stat-label">可用球台</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">09-21</div>
        <div class="stat-label">营业时段</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">5</div>
        <div class="stat-label">覆盖区域</div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <h2 class="section-title">为什么选择 VENVE</h2>
      <div class="section-line"></div>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">&#8986;</div>
          <h3>实时预约</h3>
          <p>查看实时可用时段，一键预约，无需排队等待。系统采用分布式锁技术，杜绝超卖。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">&#128737;</div>
          <h3>安全可靠</h3>
          <p>JWT 无状态认证 + BCrypt 密码加密。每个操作经过网关鉴权，数据安全有保障。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">&#9889;</div>
          <h3>智能提醒</h3>
          <p>预约创建后 15 分钟内确认有效，超时自动释放。消息中心实时推送状态变更通知。</p>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta">
      <div class="cta-card">
        <h2>准备好开始了吗？</h2>
        <p>选择一个球台，预订你的专属时段。</p>
        <router-link to="/venues" class="btn-primary">浏览球台 &#8594;</router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchVenues } from '../api'

const venueCount = ref('--')

onMounted(async () => {
  try {
    const venues = await fetchVenues()
    venueCount.value = venues.filter(v => v.status === 'AVAILABLE').length
  } catch { venueCount.value = 5 }
})
</script>

<style scoped>
.home { padding: 40px 0 80px; }

/* Hero */
.hero {
  text-align: center; padding: 60px 0 40px;
  position: relative;
}
.hero-badge {
  display: inline-block; padding: 6px 18px;
  background: rgba(74,222,128,0.08); border: 1px solid rgba(74,222,128,0.2);
  border-radius: 24px; color: var(--accent);
  font-size: 0.7rem; letter-spacing: 0.2em; font-weight: 600;
  margin-bottom: 28px;
}
.hero-title {
  font-family: var(--font-display);
  font-size: clamp(2.8rem, 6vw, 4.2rem);
  font-weight: 700; line-height: 1.15;
  margin-bottom: 20px;
}
.hero-accent { color: var(--accent); font-style: italic; }
.hero-desc {
  max-width: 560px; margin: 0 auto 36px;
  font-size: 1rem; color: var(--text-secondary); line-height: 1.7;
}
.hero-actions { display: flex; gap: 14px; justify-content: center; }
.btn-primary {
  display: inline-block; padding: 14px 36px;
  background: var(--accent); color: var(--bg-root);
  border-radius: var(--radius); font-size: 0.95rem;
  font-weight: 600; text-decoration: none; letter-spacing: 0.02em;
  transition: background 0.2s;
}
.btn-primary:hover { background: #6aee9a; }
.btn-secondary {
  display: inline-block; padding: 14px 36px;
  background: transparent; color: var(--text-primary);
  border: 1px solid var(--border); border-radius: var(--radius);
  font-size: 0.95rem; font-weight: 500; text-decoration: none;
  transition: border-color 0.2s;
}
.btn-secondary:hover { border-color: var(--accent-dim); }

/* Stats */
.stats {
  display: flex; justify-content: center; align-items: center;
  gap: 0; margin: 40px 0 64px;
  padding: 32px; background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}
.stat-item { padding: 0 48px; text-align: center; }
.stat-num {
  font-family: var(--font-display); font-size: 2.2rem;
  font-weight: 600; color: var(--text-primary);
}
.stat-label {
  margin-top: 6px; font-size: 0.8rem; color: var(--text-muted);
  text-transform: uppercase; letter-spacing: 0.08em;
}
.stat-divider { width: 1px; height: 48px; background: var(--border); }

/* Features */
.features { margin-bottom: 72px; }
.section-title {
  text-align: center; font-family: var(--font-display);
  font-size: 1.8rem; font-weight: 600; margin-bottom: 12px;
}
.section-line {
  width: 40px; height: 3px; background: var(--accent);
  margin: 0 auto 44px; border-radius: 2px;
}
.feature-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
}
.feature-card {
  padding: 32px 28px; background: var(--bg-card);
  border: 1px solid var(--border); border-radius: var(--radius-lg);
  transition: border-color 0.3s;
}
.feature-card:hover { border-color: var(--accent-dim); }
.feature-icon { font-size: 2rem; margin-bottom: 16px; }
.feature-card h3 { font-size: 1.1rem; margin-bottom: 10px; font-weight: 600; }
.feature-card p { font-size: 0.85rem; color: var(--text-secondary); line-height: 1.65; }

/* CTA */
.cta-card {
  text-align: center; padding: 56px 40px;
  background: linear-gradient(135deg, rgba(74,222,128,0.06) 0%, rgba(74,222,128,0.01) 100%);
  border: 1px solid var(--border); border-radius: var(--radius-lg);
}
.cta-card h2 { font-family: var(--font-display); font-size: 1.8rem; margin-bottom: 12px; }
.cta-card p { color: var(--text-secondary); margin-bottom: 28px; font-size: 0.95rem; }

@media (max-width: 720px) {
  .feature-grid { grid-template-columns: 1fr; }
  .stats { flex-direction: column; gap: 20px; }
  .stat-divider { width: 48px; height: 1px; }
}
</style>
