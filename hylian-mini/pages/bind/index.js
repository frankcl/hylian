// pages/bind/index.js
const app = getApp()

Page({
  data: {
    key: undefined,
    showLoading: false
  },
  onLoad(options) {
    const key = app.parseQRCodeKey(options.scene)
    this.setData({ key: key })
    app.updateQRCodeStatus(key)
  },
  bind() {
    const currentPage = this
    this.setData({ showLoading: true })
    wx.login({
      success: res => {
        wx.request({
          url: app.globalData.serverBaseURL + '/api/wechat/user/bind',
          method: 'POST',
          data: {
            key: currentPage.data.key,
            code: res.code
          },
          success: res => {
            currentPage.setData({ showLoading: false })
            const serverResponse = res.data
            app.handleServerResponse(serverResponse, '绑定成功', '绑定失败：' + serverResponse.message)
          },
          fail: error => {
            currentPage.setData({ showLoading: false })
            app.handleServerError(error)
          }
        })
      }
    })
  }
})