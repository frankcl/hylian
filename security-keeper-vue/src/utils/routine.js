import Cookies from 'js-cookie'
import { useUserStore } from '@/store'
import { getCurrentUser } from './backend'

const COOKIE_TOKEN = 'TOKEN'

export const isJsonStr = str => {
  if (typeof str === 'string') {
    try {
      const obj = JSON.parse(str)
      return typeof obj === 'object' && obj
    } catch (e) {
      console.log('error: not json string[' + str + ']!!!' + e)
      return false
    }
  }
}

export const isLogin = () => {
  const token = Cookies.get(COOKIE_TOKEN)
  return token !== undefined
}

export const refreshUser = async () => {
  if (!isLogin()) return
  const userStore = useUserStore()
  if (userStore.injected) return
  const user = await getCurrentUser()
  userStore.inject(user)
}

export const paintCaptcha = (captcha, userConfig = {}) => {
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
  captcha.split('').forEach((letter, i) => {
    context.fillText(letter, canvas.width / captcha.length * (i + 0.5), canvas.height / (Math.random() * 3 + 1))
  })
  for(let i = 0; i < config.lineNum; i++) {
    context.beginPath()
    context.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    context.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    context.strokeStyle = config.lineColor
    context.stroke()
  }
  return canvas.toDataURL('image/png')
}