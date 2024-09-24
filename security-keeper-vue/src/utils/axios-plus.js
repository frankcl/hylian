import Qs from 'qs'
import Axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'
import { isJsonStr, sweepToken } from './routine'

const pendingRequests = new Map()

const buildRequestKey = config => {
  if (config && config.data && isJsonStr(config.data)) config.data = JSON.parse(config.data)
  const { url, method, params, data } = config
  return [url, method, Qs.stringify(params), Qs.stringify(data)].join('&')
}

const addPendingRequest = config => {
  if (!config || !config.cancelRequest) return
  const key = buildRequestKey(config)
  config.cancelToken = pendingRequests.has(key) ?
    new Axios.CancelToken(cancel => cancel(`重复请求取消：${config.url}`)) :
    config.cancelToken || new Axios.CancelToken(cancel => pendingRequests.set(key, cancel))
}

const removePendingRequest = response => {
  if (!response || !response.config || !response.config.cancelRequest) return
  const key = buildRequestKey(response.config)
  if (!pendingRequests.has(key)) return
  const cancelToken = pendingRequests.get(key)
  cancelToken(key)
  pendingRequests.delete(key)
  response.config.cancelToken = undefined
}

const repeatRequest = error => {
  const config = error.config
  if (!config || !config.retry) return Promise.reject(error)
  config.__retryCount = config.__retryCount || 0
  if (config.__retryCount >= config.retry) return Promise.reject(error)
  config.__retryCount += 1
  const backOff = new Promise(resolve => {
    setTimeout(() => resolve(), config.retryDelay || 1000)
  })
  backOff.then(() => {
    if (config.data && isJsonStr(config.data)) config.data = JSON.parse(config.data)
    return axios(config)
  })
}

const axios = Axios.create({
  timeout: 6000,
  retry: 3,
  retryDelay: 1000,
  method: 'get',
  cancelRequest: true,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  }
})

const responseHandle = {
  200: response => response.data.data,
  401: async () => {
    ElMessage.error('登录状态已过期，请重新登录')
    const userStore = useUserStore()
    userStore.clear()
    sweepToken()
    await router.push('/')
  },
  default: response => {
    ElMessage.error(response.data.message)
    return Promise.reject(response)
  }
}

axios.interceptors.request.use(
  config => {
    const method = config.method.toLowerCase()
    if (method !== 'get' && method !== 'post' && method !== 'put' && method !== 'delete') {
      ElMessage.warning('非法请求方法：' + config.method)
    }
    addPendingRequest(config)
    return config
  },
  error => Promise.reject(error)
)

axios.interceptors.response.use(
  response => {
    removePendingRequest(response)
    let responseCode = response.data.code
    if (responseCode && responseCode !== 200 && responseCode !== 401) responseCode = 'default'
    return responseHandle[responseCode || 'default'](response)
  },
  async error => {
    removePendingRequest(error.response || {})
    if (!Axios.isCancel(error)) return repeatRequest(error)
    return Promise.reject(error)
  }
)

export default axios