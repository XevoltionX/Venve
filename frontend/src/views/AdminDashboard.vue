<template>
  <div class="page">
    <div class="page-hero">
      <p class="hero-overline">管理员</p>
      <h1 class="hero-title">后台管理</h1>
      <div class="hero-line"></div>
    </div>

    <div class="tab-bar">
      <button v-for="tab in tabs" :key="tab.key" class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === tab.key }"
        @click="switchTab(tab.key)">
        {{ tab.label }}
      </button>
      <button v-if="activeTab === 'venues'" class="add-btn" @click="openCreate">&#10010; 新增</button>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <input v-if="activeTab==='venues'" v-model="filters.name" class="fld-inline" placeholder="名称搜索..." @keyup.enter="loadData" />
      <input v-if="activeTab==='venues'" v-model="filters.location" class="fld-inline" placeholder="区域..." @keyup.enter="loadData" />
      <select v-if="activeTab==='venues'" v-model="filters.status" class="fld-inline" @change="loadData">
        <option value="">全部状态</option>
        <option value="AVAILABLE">可用</option>
        <option value="MAINTENANCE">维护中</option>
      </select>

      <input v-if="activeTab==='bookings'" v-model="filters.userName" class="fld-inline" placeholder="用户名..." @keyup.enter="loadData" />
      <select v-if="activeTab==='bookings'" v-model="filters.status" class="fld-inline" @change="loadData">
        <option value="">全部状态</option>
        <option value="PENDING">PENDING</option>
        <option value="CONFIRMED">CONFIRMED</option>
        <option value="CANCELLED">CANCELLED</option>
      </select>

      <input v-if="activeTab==='users'" v-model="filters.userName" class="fld-inline" placeholder="用户名..." @keyup.enter="loadData" />
      <select v-if="activeTab==='users'" v-model="filters.role" class="fld-inline" @change="loadData">
        <option value="">全部角色</option>
        <option value="USER">USER</option>
        <option value="ADMIN">ADMIN</option>
      </select>

      <button class="btn-search" @click="loadData">搜索</button>
    </div>

    <div v-if="loading" class="state-msg"><span class="spinner"></span> 加载中...</div>
    <div v-else-if="loadErr" class="state-msg state-err">{{ loadErr }} <button class="retry-btn" @click="loadData">重试</button></div>

    <!-- 场馆 -->
    <div v-else-if="activeTab === 'venues'" class="tab-content">
      <div v-if="venues.length === 0" class="state-msg">暂无数据</div>
      <div v-else class="table-wrap">
        <div class="tbl-header"><span class="col-name">名称</span><span>区域</span><span>状态</span><span>价格</span><span class="col-act">操作</span></div>
        <div v-for="v in venues" :key="v.id" class="tbl-row">
          <span class="col-name">{{ v.name }}</span><span>{{ v.location }}</span>
          <span :class="v.status==='AVAILABLE'?'text-green':'text-gold'">{{ v.status }}</span>
          <span>¥{{ v.pricePerHour }}</span>
          <span class="col-act"><button class="act-btn" @click="openVenueEdit(v)">编辑</button><button class="del-btn" @click="delVenue(v)">删除</button></span>
        </div>
      </div>
    </div>

    <!-- 预约 -->
    <div v-else-if="activeTab === 'bookings'" class="tab-content">
      <div v-if="bookings.length === 0" class="state-msg">暂无数据</div>
      <div v-else class="table-wrap">
        <div class="tbl-header tbl-6"><span>ID</span><span>用户</span><span>场馆</span><span>日期</span><span>状态</span><span class="col-act">操作</span></div>
        <div v-for="b in bookings" :key="b.id" class="tbl-row tbl-6">
          <span>#{{ b.id }}</span><span>{{ b.userName }}</span><span>#{{ b.venueId }}</span><span>{{ b.bookingDate }}</span>
          <span :class="statusClass(b.status)">{{ b.status }}</span>
          <span class="col-act"><select class="fld-sm" :value="b.status" @change="updateBookingStatus(b, $event.target.value)"><option value="PENDING">PENDING</option><option value="CONFIRMED">CONFIRMED</option><option value="CANCELLED">CANCELLED</option></select><button class="del-btn" @click="delBooking(b)">删除</button></span>
        </div>
      </div>
    </div>

    <!-- 用户 -->
    <div v-else-if="activeTab === 'users'" class="tab-content">
      <div v-if="users.length === 0" class="state-msg">暂无数据</div>
      <div v-else class="table-wrap">
        <div class="tbl-header tbl-6"><span>ID</span><span>用户名</span><span>手机</span><span>角色</span><span>注册时间</span><span class="col-act">操作</span></div>
        <div v-for="u in users" :key="u.id" class="tbl-row tbl-6">
          <span>#{{ u.id }}</span><span>{{ u.userName }}</span><span>{{ u.phone || '-' }}</span>
          <span :class="u.role==='ADMIN'?'text-green':''">{{ u.role }}</span>
          <span>{{ formatTime(u.createdAt) }}</span>
          <span class="col-act"><button class="act-btn" @click="openUserEdit(u)">编辑</button><button class="del-btn" @click="delUser(u)">删除</button></span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pager">
      <button :disabled="currentPage <= 0" @click="goPage(currentPage-1)">‹</button>
      <button v-for="p in pageNumbers" :key="p" class="page-btn" :class="{ 'page-btn--active': p === currentPage }" @click="goPage(p)">{{ p + 1 }}</button>
      <button :disabled="currentPage >= totalPages-1" @click="goPage(currentPage+1)">›</button>
    </div>

    <!-- 场馆弹窗 -->
    <div v-if="showVenueModal" class="modal-overlay" @click.self="showVenueModal=false">
      <div class="modal-card">
        <h3>{{ isCreate ? '新增场馆' : '编辑 ' + venueForm.name }}</h3>
        <label>名称</label><input v-model="venueForm.name" class="fld" />
        <label>区域</label><input v-model="venueForm.location" class="fld" />
        <label>简介</label><textarea v-model="venueForm.description" class="fld"></textarea>
        <label>营业时间</label><input v-model="venueForm.businessHours" class="fld" />
        <label>球台数量</label><input v-model.number="venueForm.tableCount" type="number" class="fld" />
        <label>每小时价格</label><input v-model.number="venueForm.pricePerHour" type="number" step="0.01" class="fld" />
        <label>状态</label><select v-model="venueForm.status" class="fld"><option value="AVAILABLE">可用</option><option value="MAINTENANCE">维护中</option></select>
        <div class="modal-actions"><button class="btn-save" @click="saveVenue">{{ isCreate ? '创建' : '保存' }}</button><button class="btn-cancel" @click="showVenueModal=false">取消</button></div>
      </div>
    </div>

    <!-- 用户弹窗 -->
    <div v-if="showUserModal" class="modal-overlay" @click.self="showUserModal=false">
      <div class="modal-card">
        <h3>编辑 {{ userForm.userName }}</h3>
        <label>角色</label><select v-model="userForm.role" class="fld"><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select>
        <label>手机号</label><input v-model="userForm.phone" class="fld" />
        <div class="modal-actions"><button class="btn-save" @click="saveUser">保存</button><button class="btn-cancel" @click="showUserModal=false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  adminListVenues, adminCreateVenue, adminUpdateVenue, adminDeleteVenue,
  adminListBookings, adminUpdateBookingStatus, adminDeleteBooking,
  adminListUsers, adminUpdateUser, adminDeleteUser
} from '../api'

const tabs = [{ key: 'venues', label: '场馆管理' },{ key: 'bookings', label: '预约管理' },{ key: 'users', label: '用户管理' }]
const activeTab = ref('venues')
const venues = ref([])
const bookings = ref([])
const users = ref([])
const loading = ref(true)
const loadErr = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const filters = reactive({ name: '', location: '', status: '', userName: '', role: '' })

const pageSize = 8

const showVenueModal = ref(false)
const isCreate = ref(false)
const venueForm = reactive({ id: null, name: '', location: '', description: '', businessHours: '09:00-21:00', tableCount: 1, pricePerHour: 30, status: 'AVAILABLE' })

const showUserModal = ref(false)
const userForm = reactive({ id: null, userName: '', phone: '', role: 'USER' })

const pageNumbers = computed(() => {
  const arr = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 3)
  for (let i = start; i < end; i++) arr.push(i)
  return arr
})

function switchTab(key) {
  activeTab.value = key
  filters.name = ''; filters.location = ''; filters.status = ''; filters.userName = ''; filters.role = ''
  currentPage.value = 0
  loadData()
}

function goPage(p) { currentPage.value = p; loadData() }

function buildParams() {
  const p = { page: currentPage.value, size: pageSize }
  if (filters.name) p.name = filters.name
  if (filters.location) p.location = filters.location
  if (filters.status) p.status = filters.status
  if (filters.userName) p.userName = filters.userName
  if (filters.role) p.role = filters.role
  return p
}

async function loadData() {
  loading.value = true; loadErr.value = ''
  try {
    let resp
    if (activeTab.value === 'venues') resp = await adminListVenues(buildParams())
    else if (activeTab.value === 'bookings') resp = await adminListBookings(buildParams())
    else if (activeTab.value === 'users') resp = await adminListUsers(buildParams())

    if (activeTab.value === 'venues') venues.value = resp.content
    else if (activeTab.value === 'bookings') bookings.value = resp.content
    else if (activeTab.value === 'users') users.value = resp.content

    totalPages.value = resp.totalPages
    totalElements.value = resp.totalElements
    currentPage.value = resp.currentPage
  } catch (e) { loadErr.value = e.message || '加载失败' }
  finally { loading.value = false }
}

// Venue
function openCreate() { venueForm.id = null; venueForm.name = ''; venueForm.location = ''; venueForm.description = ''; venueForm.businessHours = '09:00-21:00'; venueForm.tableCount = 1; venueForm.pricePerHour = 30; venueForm.status = 'AVAILABLE'; isCreate.value = true; showVenueModal.value = true }
function openVenueEdit(v) { venueForm.id = v.id; venueForm.name = v.name; venueForm.location = v.location || ''; venueForm.description = v.description || ''; venueForm.businessHours = v.businessHours || '09:00-21:00'; venueForm.tableCount = v.tableCount || 1; venueForm.pricePerHour = v.pricePerHour || 30; venueForm.status = v.status || 'AVAILABLE'; isCreate.value = false; showVenueModal.value = true }
async function saveVenue() {
  try {
    const data = { ...venueForm, id: undefined }
    if (isCreate.value) await adminCreateVenue(data); else await adminUpdateVenue(venueForm.id, data)
    showVenueModal.value = false; await loadData()
  } catch (e) { alert(e.message || '保存失败') }
}
async function delVenue(v) { if (!confirm(`确认删除「${v.name}」？`)) return; try { await adminDeleteVenue(v.id); await loadData() } catch (e) { alert(e.message || '删除失败') } }

// Booking
async function updateBookingStatus(b, status) { try { await adminUpdateBookingStatus(b.id, status); b.status = status } catch (e) { alert(e.message || '更新失败') } }
async function delBooking(b) { if (!confirm(`确认删除预约 #${b.id}？`)) return; try { await adminDeleteBooking(b.id); await loadData() } catch (e) { alert(e.message || '删除失败') } }

// User
function openUserEdit(u) { userForm.id = u.id; userForm.userName = u.userName; userForm.phone = u.phone || ''; userForm.role = u.role || 'USER'; showUserModal.value = true }
async function saveUser() {
  try { await adminUpdateUser(userForm.id, { role: userForm.role, phone: userForm.phone }); showUserModal.value = false; await loadData() }
  catch (e) { alert(e.message || '保存失败') }
}
async function delUser(u) { if (!confirm(`确认删除「${u.userName}」？`)) return; try { await adminDeleteUser(u.id); await loadData() } catch (e) { alert(e.message || '删除失败') } }

function statusClass(s) { if (s === 'CONFIRMED') return 'text-green'; if (s === 'CANCELLED') return 'text-danger'; return 'text-gold' }
function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }

onMounted(loadData)
</script>

<style scoped>
.page { padding: 40px 0; }
.page-hero { margin-bottom: 24px; }
.hero-overline { font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.15em; color: var(--accent); margin-bottom: 12px; font-weight: 500; }
.hero-title { font-size: 2rem; font-weight: 600; }
.hero-line { width: 48px; height: 3px; background: var(--accent); margin-top: 16px; border-radius: 2px; }

.tab-bar { display: flex; gap: 4px; margin-bottom: 16px; align-items: center; }
.tab-btn { padding: 10px 24px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-secondary); font-size: 0.85rem; cursor: pointer; transition: all 0.2s; font-weight: 500; }
.tab-btn:hover { border-color: var(--accent-dim); color: var(--text-primary); }
.tab-btn--active { background: rgba(74,222,128,0.08); border-color: var(--accent); color: var(--accent); }
.add-btn { margin-left: auto; padding: 10px 20px; background: var(--accent); color: var(--bg-root); border: none; border-radius: var(--radius); font-size: 0.85rem; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.add-btn:hover { background: #6aee9a; }

.filter-bar { display: flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap; }
.fld-inline { padding: 8px 14px; background: var(--bg-input); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-primary); font-size: 0.82rem; outline: none; min-width: 120px; }
.fld-inline:focus { border-color: var(--accent-dim); }
.btn-search { padding: 8px 20px; background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-primary); font-size: 0.82rem; cursor: pointer; }
.btn-search:hover { border-color: var(--accent-dim); }

.state-msg { text-align: center; padding: 48px 20px; color: var(--text-muted); font-size: 0.95rem; }
.state-err { color: var(--danger); background: var(--danger-bg); border-radius: var(--radius); }
.spinner { display: inline-block; width: 18px; height: 18px; border: 2px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin 0.8s linear infinite; margin-right: 8px; vertical-align: -4px; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: block; margin: 12px auto 0; padding: 8px 20px; background: var(--bg-elevated); color: var(--text-primary); border: 1px solid var(--border); border-radius: var(--radius); font-size: 0.85rem; cursor: pointer; }

.table-wrap { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.tbl-header, .tbl-row { display: grid; grid-template-columns: 2fr 1fr 1fr 0.8fr 1fr; gap: 8px; padding: 12px 18px; align-items: center; font-size: 0.85rem; }
.tbl-6 { grid-template-columns: 0.8fr 1.5fr 0.8fr 1.5fr 1fr 1.2fr; }
.tbl-header { background: var(--bg-surface); font-weight: 600; color: var(--text-muted); font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; }
.tbl-row { border-top: 1px solid var(--border); }
.tbl-row:hover { background: var(--bg-surface); }
.col-name { font-weight: 500; }
.col-act { display: flex; gap: 6px; align-items: center; justify-content: flex-end; }
.text-green { color: var(--accent); font-weight: 500; }
.text-gold { color: var(--gold); }
.text-danger { color: var(--danger); }
.act-btn { padding: 4px 12px; background: var(--accent); color: var(--bg-root); border-radius: var(--radius); font-size: 0.75rem; font-weight: 600; cursor: pointer; border: none; }
.del-btn { padding: 4px 12px; background: transparent; color: var(--danger); border: 1px solid var(--danger); border-radius: var(--radius); font-size: 0.75rem; cursor: pointer; transition: all 0.2s; }
.del-btn:hover { background: var(--danger-bg); }

.pager { display: flex; justify-content: center; gap: 4px; margin-top: 24px; }
.pager button { padding: 8px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-secondary); font-size: 0.85rem; cursor: pointer; transition: all 0.15s; }
.pager button:hover:not(:disabled) { border-color: var(--accent-dim); color: var(--text-primary); }
.pager button:disabled { opacity: 0.3; cursor: not-allowed; }
.page-btn--active { background: var(--accent) !important; color: var(--bg-root) !important; border-color: var(--accent) !important; }

.fld { width: 100%; padding: 10px; margin: 4px 0 12px; background: var(--bg-input); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-primary); font-size: 0.85rem; outline: none; }
.fld:focus { border-color: var(--accent-dim); }
.fld-sm { padding: 4px 8px; background: var(--bg-input); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text-primary); font-size: 0.75rem; outline: none; }

.modal-overlay { position: fixed; inset: 0; z-index: 200; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; }
.modal-card { width: 480px; max-height: 80vh; overflow-y: auto; padding: 32px; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); }
.modal-card h3 { margin-bottom: 16px; font-size: 1.2rem; }
.modal-actions { display: flex; gap: 10px; margin-top: 16px; }
.btn-save { padding: 10px 24px; background: var(--accent); color: var(--bg-root); border: none; border-radius: var(--radius); font-weight: 600; cursor: pointer; }
.btn-cancel { padding: 10px 24px; background: var(--bg-elevated); color: var(--text-secondary); border: 1px solid var(--border); border-radius: var(--radius); cursor: pointer; }
</style>
