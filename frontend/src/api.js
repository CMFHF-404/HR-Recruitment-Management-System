import axios from 'axios'
import { ElMessage } from 'element-plus'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 12000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('hrms_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.success === false) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body?.data ?? body
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('hrms_token')
      localStorage.removeItem('hrms_user')
      window.location.href = '/login'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export const positionStatusText = {
  OPEN: '招聘中',
  CLOSED: '已关闭',
}

export const screeningStatusText = {
  PENDING: '待筛选',
  PASSED: '通过',
  REJECTED: '未通过',
}

export const interviewStatusText = {
  NOT_SCHEDULED: '未安排',
  SCHEDULED: '已安排',
  COMPLETED: '已完成',
  CANCELED: '已取消',
}

export const offerStatusText = {
  PENDING: '待定',
  OFFERED: '录用',
  REJECTED: '未录用',
  ABANDONED: '放弃入职',
}

export const statusType = {
  OPEN: 'success',
  CLOSED: 'info',
  PENDING: 'warning',
  PASSED: 'success',
  REJECTED: 'danger',
  NOT_SCHEDULED: 'info',
  SCHEDULED: 'primary',
  COMPLETED: 'success',
  CANCELED: 'info',
  OFFERED: 'success',
  ABANDONED: 'info',
}
