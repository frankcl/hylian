import Qs from 'qs'
import axios, { CanceledError } from 'axios'
import router from '@/router'
import { useUserStore } from '@/store'
import { checkPermission } from '@/common/Permission'
import { ERROR, showMessage, WARNING } from '@/common/Feedback'

const axiosRequestMap = new Map()
let abortController = new AbortController()

const isJsonStr = str => {
  if (typeof str !== 'string') return false
  try {
    const o = JSON.parse(str)
    return typeof o === 'object' && o
  } catch {
    return false
  }
}

const buildAxiosRequestKey = config => {
  if (config.data && isJsonStr(config.data)) config.data = JSON.parse(config.data)
  if (config.params && isJsonStr(config.params)) config.params = JSON.parse(config.params)
  return [config.url, config.method, Qs.stringify(config.params), Qs.stringify(config.data)].join('&')
}

const putAxiosRequest = config => {
  if (!config.allowCancel) return
  const key = buildAxiosRequestKey(config)
  if (axiosRequestMap.has(key)) {
    abortController.abort(`重复请求被取消：${config.url}`)
    return
  }
  axiosRequestMap.set(key, key)
}

const removeAxiosRequest = response => {
  if (!response || !response.config || !response.config.allowCancel) return
  const key = buildAxiosRequestKey(response.config)
  if (axiosRequestMap.has(key)) axiosRequestMap.delete(key)
}

const repeatAxiosRequest = async error => {
  const config = error.config
  if (!config || !config.retry) return Promise.reject(error)
  config.currentRetry = config.currentRetry || 1
  if (config.currentRetry >= config.retry) return Promise.reject(error)
  config.currentRetry += 1
  const retryPromise = new Promise(executor => {
    setTimeout(() => executor(), config.retryDelay || 1000)
  })
  return await retryPromise.then(() => {
    if (config.data && isJsonStr(config.data)) config.data = JSON.parse(config.data)
    if (config.params && isJsonStr(config.params)) config.params = JSON.parse(config.params)
    return axios(config)
  })
}

const handleAxiosResponse = async response => {
  const contentType = response.headers['content-type'].toLowerCase()
  if (contentType !== 'application/json' && contentType !== 'multipart/form-data') {
    return Promise.reject(`不支持content-type:${contentType}`)
  }
  switch (response.data.code) {
    case 200: return response.data.data
    case 401:
      showMessage('请重新登录认证', WARNING)
      useUserStore().$reset()
      await router.push('/')
      return Promise.reject(response.data.message)
    default:
      showMessage(response.data.message, ERROR)
      return Promise.reject(response.data.message)
  }
}

const axiosInstance = axios.create({
  timeout: 6000,
  retry: 3,
  retryDelay: 1000,
  method: 'get',
  signal: abortController.signal,
  allowCancel: true,
  withCredentials: true,
  baseURL: import.meta.env.VITE_BASE_URL,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

axiosInstance.interceptors.request.use(
  config => {
    if (!checkPermission(config.url)) {
      showMessage('无权权限', WARNING)
      return Promise.reject('无权操作')
    }
    putAxiosRequest(config)
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

axiosInstance.interceptors.response.use(
  response => {
    removeAxiosRequest(response)
    return handleAxiosResponse(response)
  },
  async error => {
    removeAxiosRequest(error)
    if (error instanceof CanceledError) {
      showMessage(`取消重复请求:${error.config.url}`, WARNING)
      abortController = new AbortController()
      return Promise.reject(error)
    }
    if (error.response && error.response.status === 401) {
      showMessage('请重新登录认证', WARNING)
      useUserStore().$reset()
      await router.push('/')
      return Promise.reject(error)
    }
    return repeatAxiosRequest(error)
  }
)

export default axiosInstance