import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'

const httpClient = axios.create()

httpClient.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

httpClient.interceptors.response.use(
  async response => {
    const code = response.data.code
    const message = response.data.message
    switch (code) {
      case 200:
        return response.data
      default:
        ElMessage.error(message)
        if (code === 401) await router.push('/')
        return Promise.reject(new Error(message))
    }
  },
  async error => {
    const code = error.response ? error.response.status : -1
    if (code >= 400) {
      ElMessage.error(error.message)
      if (code === 401) await router.push('/')
    }
    return Promise.reject(error)
  }
)

export const httpRequest = (config) => {
  const defaultConfig = {
    timeout: 6000,
    method: 'get',
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
    }
  }
  return httpClient({ ...defaultConfig, ...config })
}