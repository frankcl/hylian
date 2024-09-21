import router from '@/router'
import { useUserStoreHook } from '@/store'
import { httpRequest } from '@/utils/http'
import { ElMessage } from 'element-plus'

const userStore = useUserStoreHook()

export async function logout() {
  await httpRequest({
    method: 'get',
    url: '/api/logout'
  })
  userStore.$reset()
  await router.push('/')
}

export function hasAccessPermission() {
  if (!userStore.isLogin) ElMessage.warning('未登录')
  return userStore.isLogin
}

export function createVerifyCodeImage(verifyCode, userConfig = {}) {
  const defaultConfig = {
    width: 70,
    height: 30,
    backgroundColor: '#f9f9f9',
    fontColor: '#000000',
    font: 'italic 14px Arial',
    lineColor: '#a5a5a5',
    lineNum: 20
  }
  const config = { ...defaultConfig, ...userConfig }
  const canvas = document.createElement('canvas')
  canvas.width = config.width
  canvas.height = config.height
  const context = canvas.getContext('2d')
  context.fillStyle = config.backgroundColor
  context.fillRect(0, 0, canvas.width, canvas.height)
  context.fillStyle = config.fontColor
  context.font = config.font
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(verifyCode, canvas.width / 2, canvas.height / 2)
  for(let i = 0; i < config.lineNum; i++) {
    context.beginPath();
    context.moveTo(Math.random() * canvas.width, Math.random() * canvas.height);
    context.lineTo(Math.random() * canvas.width, Math.random() * canvas.height);
    context.strokeStyle = config.lineColor;
    context.stroke();
  }
  return canvas.toDataURL('image/png')
}