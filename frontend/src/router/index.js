import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    name: 'home',
    component: () => import('../views/Home.vue'),
    meta: { auth: true }
  },
  {
    path: '/venues',
    name: 'venues',
    component: () => import('../views/VenueList.vue'),
    meta: { auth: true }
  },
  {
    path: '/book/:venueId',
    name: 'booking-create',
    component: () => import('../views/BookingCreate.vue'),
    meta: { auth: true }
  },
  {
    path: '/bookings',
    name: 'my-bookings',
    component: () => import('../views/MyBookings.vue'),
    meta: { auth: true }
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('../views/Notifications.vue'),
    meta: { auth: true }
  },
  {
    path: '/monitor',
    name: 'monitor',
    component: () => import('../views/Monitor.vue'),
    meta: { auth: true, admin: true }
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { auth: true, admin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.auth && !token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.admin && role !== 'ADMIN') {
    next({ name: 'home' })
    return
  }

  if (to.meta.guest && token) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
