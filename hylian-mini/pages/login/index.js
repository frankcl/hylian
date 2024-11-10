// pages/login/index.js
const app = getApp()
const defaultAvatarUrl = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'

Page({
  data: {
    key: undefined,
    register: false,
    showLoading: false,
    canUseGetUserProfile: wx.canIUse('getUserProfile'),
    canUseNicknameComp: wx.canIUse('input.type.nickname'),
    avatarUrl: defaultAvatarUrl,
    nickName: '',
    authorizeURL: app.globalData.serverBaseURL + '/api/wechat/user/authorize'
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
        if (!serverResponse.status) {
          wx.navigateTo({
            url: '/pages/index/prompt?success=false&message=上传头像失败'
          })
          return
        }
        currentPage.setData({ avatarUrl: serverResponse.data })
      },
      fail: error => {
        wx.navigateTo({
          url: '/pages/index/prompt?success=false&message=服务器连接异常 ' + error.errMsg
        })
      }
    })
  },
  onInputChange(e) {
    this.setData({ nickName: e.detail.value })
  },
  isRegister() {
    const currentPage = this
    wx.login({
      success: res => {
        wx.request({
          url: app.globalData.serverBaseURL + '/api/wechat/user/exists?code=' + res.code,
          success: res => {
            const serverResponse = res.data
            if (!serverResponse.status) {
              wx.navigateTo({
                url: '/pages/index/prompt?success=false&message=服务端异常：' + serverResponse.message
              })
              return
            }
            currentPage.setData({ register: serverResponse.data })
          },
          fail: error => {
            wx.navigateTo({
              url: '/pages/index/prompt?success=false&message=服务器连接异常 ' + error.errMsg
            })
          }
        })
      }
    })
  },
  getUserProfile(e) {
    const currentPage = this
    wx.getUserProfile({
      desc: '获取用户信息',
      success: res => {
        currentPage.setData({ 
          nickName: res.userInfo.nickName,
          avatarUrl: res.userInfo.avatarUrl
        })
        currentPage.login()
      }
    })
  },
  login() {
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
          url: currentPage.data.authorizeURL,
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
            if (serverResponse.status && serverResponse.data) {
              wx.navigateTo({
                url: '/pages/index/prompt?success=true&message=授权成功'
              })
            } else {
              wx.navigateTo({
                url: '/pages/index/prompt?success=false&message=授权失败：' + serverResponse.message
              })
            }
          },
          fail: error => {
            currentPage.setData({ showLoading: false })
            wx.navigateTo({
              url: '/pages/index/prompt?success=false&message=服务器连接异常 ' + error.errMsg
            })
          }
        })
      }
    })
  }
})