import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

/* 请求拦截：自动带 Token */
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/* 响应拦截：统一解包 ApiResponse + 401 跳转登录 */
api.interceptors.response.use(
  res => {
    const body = res.data
    if (body && typeof body.code === 'number') {
      if (body.code === 200) return body.data
      return Promise.reject({ message: body.message || '请求失败', code: body.code })
    }
    return body
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userName')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

/* Auth */
export const login = data => api.post('/auth/login', data)
export const register = data => api.post('/auth/register', data)

/* Venues */
export const fetchVenues = (location) =>
  api.get('/venues', { params: location ? { location } : {} })

/* Slots */
export const fetchSlots = (venueId, date) =>
  api.get(`/bookings/slots/${venueId}`, { params: { date } })

/* Bookings */
export const createBooking = data => api.post('/bookings', data)
export const fetchMyBookings = () => api.get('/bookings')
export const confirmBooking = id => api.put(`/bookings/${id}/confirm`)
export const cancelBooking = id => api.put(`/bookings/${id}/cancel`)

/* Notifications */
export const fetchNotifications = () => api.get('/notifications')
export const fetchUnreadCount = () => api.get('/notifications/unread-count')
export const markNotificationRead = id => api.put(`/notifications/${id}/read`)

/* Admin - Venues */
export const adminListVenues = (params = {}) => api.get('/admin/venues', { params })
export const adminCreateVenue = data => api.post('/admin/venues', data)
export const adminUpdateVenue = (id, data) => api.put(`/admin/venues/${id}`, data)
export const adminDeleteVenue = id => api.delete(`/admin/venues/${id}`)

/* Admin - Bookings */
export const adminListBookings = (params = {}) => api.get('/admin/bookings', { params })
export const adminUpdateBookingStatus = (id, status) =>
  api.put(`/admin/bookings/${id}/status?status=${status}`)
export const adminDeleteBooking = id => api.delete(`/admin/bookings/${id}`)

/* Admin - Users */
export const adminListUsers = (params = {}) => api.get('/admin/users', { params })
export const adminUpdateUser = (id, data) => api.put(`/admin/users/${id}`, data)
export const adminDeleteUser = id => api.delete(`/admin/users/${id}`)
