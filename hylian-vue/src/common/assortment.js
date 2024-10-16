import Cookies from 'js-cookie'
import { ElMessageBox, ElNotification } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store'
import {
  asyncCurrentUser,
  asyncLogout,
  asyncSearchApps,
  asyncSearchPermissions,
  asyncSearchRoles,
  asyncSearchTenants, asyncSearchUsers
} from './service'

export const isJsonStr = str => {
  if (typeof str === 'string') {
    try {
      const obj = JSON.parse(str)
      return typeof obj === 'object' && obj
    } catch (e) {
      console.log('error: not json str[' + str + ']!' + e)
      return false
    }
  }
}

export const copyToClipboard = async text => await navigator.clipboard.writeText(text)

export const searchQueryToRequest = query => {
  const request = {
    current: query.current || 1,
    size: query.size || 20
  }
  if (query.sort_field && query.sort_order) {
    request.order_by = JSON.stringify([{ field: query.sort_field, asc: query.sort_order === 'ascending' }])
  }
  return request
}

export const fillSearchQuerySort = (event, query) => {
  if (!event || !event.prop) return
  query.sort_field = event.prop
  query.sort_order = event.order
}

export const submitForm = async (formEl, formData, asyncFunc, successMsg, errorMsg) => {
  if (formEl && !await formEl.validate(valid => valid)) return false
  if (!await asyncFunc(formData)) {
    if (errorMsg) ElNotification.error(errorMsg)
    return false
  }
  if (successMsg) ElNotification.success(successMsg)
  return true
}

export const removeAfterConfirm = async (id, asyncFunc, title, message, successMsg, errorMsg) => {
  let success = false
  await popConfirmBox(title, message, async () => {
    if (!await asyncFunc(id)) {
      if (errorMsg) ElNotification.error(errorMsg)
      return success = false
    }
    if (successMsg) ElNotification.success(successMsg)
    return success = true
  })
  return success
}

export const isLogin = () => Cookies.get('TOKEN') !== undefined

export const logout = async () => {
  await asyncLogout()
  useUserStore().clear()
  await router.push('/')
}

export const refreshUser = async (force = false) => {
  if (isLogin()) {
    const userStore = useUserStore()
    if (!force && userStore.injected) return
    userStore.inject(await asyncCurrentUser())
  }
}

export const fetchAllApps = async (cancelRequest = true) => {
  const pager = await asyncSearchApps({ current: 1, size: 100 }, cancelRequest)
  return pager ? pager.records : []
}

export const fetchAllTenants = async (cancelRequest = true) => {
  const pager = await asyncSearchTenants({ current: 1, size: 100 }, cancelRequest)
  return pager ? pager.records : []
}

export const fetchAllUsers = async (cancelRequest = true) => {
  const pager = await asyncSearchUsers({ current: 1, size: 100 }, cancelRequest)
  return pager ? pager.records : []
}

export const fetchAppRoles = async appId => {
  const pager = await asyncSearchRoles({ app_id: appId, current: 1, size: 100 })
  return pager ? pager.records : []
}

export const fetchAppPermissions = async appId => {
  const pager = await asyncSearchPermissions({ app_id: appId, current: 1, size: 100 })
  return pager ? pager.records : []
}

export const popConfirmBox = (
  title, message,
  confirmFunc = undefined,
  cancelFunc = undefined) => {
  return ElMessageBox.confirm(
    message,
    title,
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(confirmFunc).catch(cancelFunc)
}

export const drawCaptcha = (captcha, userConfig = {}) => {
  const randomInt = (from, to) => Math.random() * (to - from) + from
  const randomRGB = () => `rgb(${randomInt(0, 255)},${randomInt(0, 255)},${randomInt(0, 255)})`
  const drawPoint = (ctx, width, height) => {
    const x = randomInt(0, width)
    const y = randomInt(0, height)
    ctx.fillStyle = randomRGB()
    ctx.fillRect(x, y, 1, 1)
  }
  const drawLine = (ctx, width, height) => {
    ctx.beginPath()
    ctx.moveTo(Math.random() * width, Math.random() * height)
    ctx.lineTo(Math.random() * width, Math.random() * height)
    ctx.strokeStyle = randomRGB()
    ctx.stroke()
  }
  const defaultConfig = {
    width: 84,
    height: 32,
    backgroundColor: '#f7f7f7',
    fontColor: '#8b8c8c',
    font: 'italic 18px Arial',
    noisePoints: 20,
    noiseLines: 10
  }
  const config = { ...defaultConfig, ...userConfig }
  const canvas = document.createElement('canvas')
  canvas.width = config.width
  canvas.height = config.height
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = config.backgroundColor
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.fillStyle = config.fontColor
  ctx.font = config.font
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  captcha.split('').forEach((letter, i) => {
    ctx.fillText(letter, canvas.width / captcha.length * (i + 0.5), canvas.height / randomInt(1, 4))
  })
  for(let i = 0; i < config.noiseLines; i++) {
    drawLine(ctx, canvas.width, canvas.height)
  }
  for(let i = 0; i < config.noisePoints; i++) {
    drawPoint(ctx, canvas.width, canvas.height)
  }
  return canvas.toDataURL('image/png')
}