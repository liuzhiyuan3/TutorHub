const app = getApp()

function request({ url, method = 'GET', data = {} }) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${url}`,
      method,
      data,
      header: {
        Authorization: app.globalData.token ? `Bearer ${app.globalData.token}` : ''
      },
      success: (res) => {
        const body = res.data || {}
        if (body.code !== 0) {
          reject(new Error(body.message || '请求失败'))
          return
        }
        resolve(body.data)
      },
      fail: reject
    })
  })
}

module.exports = { request }
