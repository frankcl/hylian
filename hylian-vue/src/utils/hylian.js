import Cookies from 'js-cookie'
import { useUserStore } from '@/store'
import { getCurrentUser } from './hylian-service'

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

export const randomInt = (from = 0, to = 1) => {
  return from <= to ? Math.random() * (to - from) + from : Math.random() * (from - to) + to
}

export const paintCaptcha = (captcha, userConfig = {}) => {
  const randomColor = () => {
    const [r, g, b] = [randomInt(0, 255), randomInt(0, 255), randomInt(0, 255)]
    return `rgb(${r},${g},${b})`
  }
  const drawPoint = (context, width, height) => {
    const x = randomInt(0, width)
    const y = randomInt(0, height)
    context.fillStyle = randomColor()
    context.fillRect(x, y, 1, 1)
  }
  const drawLine = (context, width, height) => {
    context.beginPath()
    context.moveTo(Math.random() * width, Math.random() * height)
    context.lineTo(Math.random() * width, Math.random() * height)
    context.strokeStyle = randomColor()
    context.stroke()
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
  const context = canvas.getContext('2d')
  context.fillStyle = config.backgroundColor
  context.fillRect(0, 0, canvas.width, canvas.height)
  context.fillStyle = config.fontColor
  context.font = config.font
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  captcha.split('').forEach((letter, i) => {
    context.fillText(letter, canvas.width / captcha.length * (i + 0.5), canvas.height / randomInt(1, 4))
  })
  for(let i = 0; i < config.noiseLines; i++) {
    drawLine(context, canvas.width, canvas.height)
  }
  for(let i = 0; i < config.noisePoints; i++) {
    drawPoint(context, canvas.width, canvas.height)
  }
  return canvas.toDataURL('image/png')
}