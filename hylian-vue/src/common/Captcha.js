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