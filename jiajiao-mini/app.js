App({
  globalData: {
    baseUrl: 'http://localhost:8080',
    token: wx.getStorageSync('token') || '',
    userType: wx.getStorageSync('userType') ?? null
  }
})
