// app.js
App({
  onLaunch() {
  },
  parseQRCodeKey(v) {
    const scene = decodeURIComponent(v)
    if (!scene || scene === 'undefined') {
      wx.navigateTo({
        url: '/pages/index/prompt?success=false&message=小程序码缺失参数scene'
      })
      return
    }
    const queryMap = new Map()
    const queries = scene.split('&')
    queries.forEach(query => {
      const kv = query.split('=')
      if (kv.length !== 2) return
      queryMap.set(kv[0], kv[1])
    })
    if (!queryMap.has('key')) {
      wx.navigateTo({
        url: '/pages/index/prompt?success=false&message=scene缺失参数key'
      })
      return
    }
    return queryMap.get('key')
  },
  updateQRCodeStatus(key) {
    const updateURL = this.globalData.serverBaseURL + '/api/wechat/code/update'
    wx.request({
      url: updateURL,
      method: 'POST',
      data: {
        key: key,
        status: 1
      },
      success: res => {
        const serverResponse = res.data
        if (!serverResponse.status || !serverResponse.data) {
          wx.navigateTo({
            url: '/pages/index/prompt?success=false&message=小程序码已过期，请刷新后重新扫码'
          })
        }
      },
      fail: error => {
        wx.navigateTo({
          url: '/pages/index/prompt?success=false&message=服务器连接异常 ' + error.errMsg
        })
      }
    })
  },
  globalData: {
    serverBaseURL: 'http://192.168.3.43:8080',
    userInfo: null
  }
})
