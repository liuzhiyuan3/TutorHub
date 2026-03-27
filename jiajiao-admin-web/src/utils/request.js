import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({
  baseURL,
  timeout: Number(import.meta.env.VITE_API_TIMEOUT || 20000)
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use((response) => {
  const result = response.data
  if (!result || typeof result.code === 'undefined') {
    return Promise.reject(new Error('响应格式不正确'))
  }
  if (result.code !== 0) {
    return Promise.reject(new Error(result.message || '请求失败'))
  }
  return result.data
}, (error) => {
  if (error?.response?.status === 401) {
    localStorage.removeItem('admin_token')
    if (window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }
  let msg = error?.response?.data?.message || error?.message || '网络异常，请稍后重试'
  if (error?.code === 'ECONNABORTED' || /timeout/i.test(String(error?.message || ''))) {
    msg = '请求超时，请检查后端服务或数据库连接状态'
  } else if (/Network Error/i.test(String(error?.message || ''))) {
    msg = '无法连接后端服务，请确认后端已启动并检查代理或 API 地址配置'
  }
  return Promise.reject(new Error(msg))
})

export default request
