// app.js
App({
  onLaunch() {
  },
  parseQRCodeKey(v) {
    const scene = decodeURIComponent(v)
    if (!scene || scene === 'undefined') {
      this.navigateToPrompt(false, '小程序码缺失参数scene')
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
      this.navigateToPrompt(false, 'scene缺失参数key')
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
      success: res => this.handleServerResponse(res.data, undefined, '小程序码已过期，请刷新后重新扫码'),
      fail: error => this.handleServerError(error)
    })
  },
  handleServerResponse(serverResponse, successMsg, errorMsg, focusData = true) {
    if (serverResponse.status && (!focusData || (focusData && serverResponse.data))) {
      if (successMsg) this.navigateToPrompt(true, successMsg)
    } else {
      if (errorMsg) this.navigateToPrompt(false, errorMsg)
    }
  },
  handleServerError(error) {
    this.navigateToPrompt(false, '服务器连接异常 ' + error.errMsg)
  },
  navigateToPrompt: (success, msg) => {
    wx.navigateTo({
      url: '/pages/index/prompt?success=' + success + '&message=' + msg
    })
  },
  globalData: {
    serverBaseURL: 'http://192.168.3.43:9000',
    userInfo: null
  }
})
