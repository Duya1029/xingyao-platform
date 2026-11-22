import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { DEMO, demoAdapter } from '../mock/adapter'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
  adapter: DEMO ? demoAdapter : undefined
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('xy_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      localStorage.removeItem('xy_token')
      router.push('/login')
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
