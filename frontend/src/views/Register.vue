<template>
  <div class="page">
    <div class="auth-card">
      <div class="auth-hero">
        <span class="auth-logo">&#9670;</span>
        <h1 class="auth-title">注册</h1>
        <p class="auth-subtitle">创建你的 VENVE 账号</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <label class="field-label">用户名</label>
        <input
          type="text"
          class="field-input"
          v-model="form.userName"
          placeholder="2-50个字符"
          autocomplete="username"
        />

        <label class="field-label">密码</label>
        <input
          type="password"
          class="field-input"
          v-model="form.password"
          placeholder="至少6位密码"
          autocomplete="new-password"
        />

        <label class="field-label">手机号（选填）</label>
        <input
          type="text"
          class="field-input"
          v-model="form.phone"
          placeholder="输入手机号"
        />

        <p v-if="error" class="form-error">{{ error }}</p>

        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <p class="switch-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api'

const router = useRouter()

const form = reactive({ userName: '', password: '', phone: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  if (!form.userName || !form.password) return
  loading.value = true
  error.value = ''
  try {
    const data = await register(form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('userName', data.userName)
    localStorage.setItem('role', data.role || 'USER')
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 44px 40px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}

.auth-hero { text-align: center; margin-bottom: 32px; }
.auth-logo { font-size: 32px; color: var(--accent); }
.auth-title { font-size: 1.6rem; margin-top: 8px; }
.auth-subtitle { color: var(--text-muted); font-size: 0.85rem; margin-top: 6px; }

.auth-form { display: flex; flex-direction: column; gap: 14px; }

.field-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
  font-weight: 500;
}

.field-input {
  padding: 12px 16px;
  background: var(--bg-input);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-primary);
  font-size: 0.9rem;
}
.field-input:focus { border-color: var(--accent-dim); }

.form-error {
  color: var(--danger);
  font-size: 0.8rem;
  text-align: center;
  padding: 8px;
  background: var(--danger-bg);
  border-radius: var(--radius);
}

.submit-btn {
  margin-top: 8px;
  padding: 13px;
  background: var(--accent);
  color: var(--bg-root);
  font-size: 0.95rem;
  font-weight: 600;
  border-radius: var(--radius);
  letter-spacing: 0.1em;
  transition: background 0.2s;
}
.submit-btn:hover:not(:disabled) { background: #6aee9a; }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.switch-link {
  text-align: center;
  font-size: 0.85rem;
  color: var(--text-muted);
}
.switch-link a { color: var(--accent); }
</style>
