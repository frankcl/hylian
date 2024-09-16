import axios from 'axios'

const httpExecutor = axios.create()

httpExecutor.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
)

httpExecutor.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error)
)

export const httpRequest = (config) => {
  const defaultConfig = {
    timeout: 3000,
    baseURL: import.meta.env.VITE_BASE_URL,
    method: 'get',
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
    }
  }
  return httpExecutor({ ...defaultConfig, ...config })
}