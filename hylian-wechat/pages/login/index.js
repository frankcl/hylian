// pages/login/index.js
const app = getApp()
const defaultAvatarUrl = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'

Page({
  data: {
    key: undefined,
    registered: false,
    showLoading: false,
    canUseGetUserProfile: wx.canIUse('getUserProfile'),
    canUseNicknameComp: wx.canIUse('input.type.nickname'),
    avatarUrl: defaultAvatarUrl,
    items: [{ name: '订阅消息通知', checked: false }],
    nickName: ''
  },
  onLoad(options) {
    this.isRegister()
    const key = app.parseQRCodeKey(options.scene)
    this.setData({ key: key })
    app.updateQRCodeStatus(key)
  },
  onChooseAvatar(e) {
    const currentPage = this
    wx.uploadFile({
      filePath: e.detail.avatarUrl,
      name: 'file',
      url: app.globalData.serverBaseURL + '/api/wechat/user/uploadAvatar',
      success: res => {
        const serverResponse = JSON.parse(res.data)
        app.handleServerResponse(serverResponse, undefined, '上传头像失败')
        if (!serverResponse.status || !serverResponse.data) return
        currentPage.setData({ avatarUrl: serverResponse.data })
      },
      fail: error => app.handleServerError(error)
    })
  },
  onInputChange(e) {
    this.setData({ nickName: e.detail.value })
  },
  onCheckboxChange(e) {
    this.data.items[0].checked = e.detail.value.length === 1
    if (this.data.items[0].checked) this.subscribeMessage()
  },
  isRegister() {
    const currentPage = this
    wx.login({
      success: res => {
        wx.request({
          url: app.globalData.serverBaseURL + '/api/wechat/user/exists?code=' + res.code,
          success: res => {
            const serverResponse = res.data
            app.handleServerResponse(serverResponse, undefined, '服务异常：' + serverResponse.message, false)
            if (!serverResponse.status) return
            currentPage.setData({ registered: serverResponse.data })
          },
          fail: error => app.handleServerError(error)
        })
      }
    })
  },
  subscribeMessage() {
    wx.requestSubscribeMessage({
      tmplIds: [
        'B4E0XRqlSC3Nnc70NPTDxHAUVZl-iAPmvAZh-x2DvNs', 
        '8BQJJagGrk2G8bwUZJ0p4v8xr3uXpCyut-B1U2pHAVw'
      ],
      success (res) {}
    })
  }, 
  registerWithUserProfile() {
    const currentPage = this
    wx.getUserProfile({
      desc: '获取用户信息',
      success: res => {
        currentPage.setData({ 
          nickName: res.userInfo.nickName,
          avatarUrl: res.userInfo.avatarUrl
        })
        currentPage.register()
      }
    })
  },
  authorize() {
    const currentPage = this
    this.setData({ showLoading: true })
    wx.login({
      success: res => {
        wx.request({
          url: app.globalData.serverBaseURL + '/api/wechat/user/authorize',
          method: 'POST',
          data: {
            key: currentPage.data.key,
            code: res.code
          },
          success: res => {
            currentPage.setData({ showLoading: false })
            const serverResponse = res.data
            app.handleServerResponse(serverResponse, '授权成功', '授权失败：' + serverResponse.message)
          },
          fail: error => {
            currentPage.setData({ showLoading: false })
            app.handleServerError(error)
          }
        })
      }
    })
  },
  register() {
    if (!this.data.nickName) {
      wx.showToast({
        title: '请输入用户昵称',
        icon: 'error'
      })
      return;
    }
    const currentPage = this
    this.setData({ showLoading: true })
    wx.login({
      success: res => {
        wx.request({
          url: app.globalData.serverBaseURL + '/api/wechat/user/register',
          method: 'POST',
          data: {
            key: currentPage.data.key,
            code: res.code,
            user: {
              nickName: currentPage.data.nickName,
              avatarUrl: currentPage.data.avatarUrl
            }
          },
          success: res => {
            currentPage.setData({ showLoading: false })
            const serverResponse = res.data
            app.handleServerResponse(serverResponse, '注册成功', '注册失败：' + serverResponse.message)
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