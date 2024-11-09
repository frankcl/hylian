// pages/login/prompt.js
Page({
  data: {
    success: false,
    message: '系统错误'
  },
  onLoad(options) {
    this.setData({ 
      success: options.success === undefined ? false : options.success === 'true', 
      message: options.message === undefined ? '系统错误' : options.message
    })
  }
})