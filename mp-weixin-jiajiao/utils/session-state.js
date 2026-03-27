function normalizeUserType(raw) {
  if (raw === '' || raw === undefined || raw === null) return null
  if (raw === 0 || raw === 1) return raw
  const text = String(raw).trim()
  if (!text) return null
  const lower = text.toLowerCase()
  if (lower === 'parent') return 0
  if (lower === 'teacher') return 1
  const value = Number(text)
  return value === 0 || value === 1 ? value : null
}

function getState() {
  const app = getApp()
  const token = (app && app.globalData && app.globalData.token) || wx.getStorageSync('token') || ''
  const globalUserType = normalizeUserType(app && app.globalData ? app.globalData.userType : null)
  const storageUserType = normalizeUserType(wx.getStorageSync('userType'))
  const userType = globalUserType !== null ? globalUserType : storageUserType
  const userOpenid = (app && app.globalData && app.globalData.userOpenid) || wx.getStorageSync('userOpenid') || ''
  return { token, userType, userOpenid }
}

function setState(payload) {
  const data = payload || {}
  const token = String(data.token || '')
  const userType = normalizeUserType(data.userType)
  const userOpenid = String(data.userOpenid || '')
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = token
    app.globalData.userType = userType
    app.globalData.userOpenid = userOpenid
  }
  wx.setStorageSync('token', token)
  if (userType === 0 || userType === 1) {
    wx.setStorageSync('userType', userType)
  } else {
    wx.removeStorageSync('userType')
  }
  if (userOpenid) {
    wx.setStorageSync('userOpenid', userOpenid)
  } else {
    wx.removeStorageSync('userOpenid')
  }
}

function clearState() {
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = ''
    app.globalData.userType = null
    app.globalData.userOpenid = ''
  }
  wx.removeStorageSync('token')
  wx.removeStorageSync('userType')
  wx.removeStorageSync('userOpenid')
}

module.exports = {
  getState,
  setState,
  clearState,
  normalizeUserType
}
