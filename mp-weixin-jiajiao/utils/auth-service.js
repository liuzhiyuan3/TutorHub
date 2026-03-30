const { request, uploadFile } = require('./request')
const sessionState = require('./session-state')
const { prepareImageForUpload } = require('./image-upload')

async function passwordLogin(payload) {
  const data = await request({
    url: '/api/auth/user/login',
    method: 'POST',
    authMode: 'optional',
    data: {
      account: payload && payload.account,
      password: payload && payload.password,
      userType: payload && payload.userType
    }
  })
  saveSession(data)
  return data
}

async function wechatLogin(payload) {
  const data = await request({
    url: '/api/auth/user/login/wechat',
    method: 'POST',
    authMode: 'optional',
    data: payload
  })
  saveSession(data)
  return data
}

async function wechatPhoneLogin(payload) {
  const data = await request({
    url: '/api/auth/user/login/wechat-phone',
    method: 'POST',
    authMode: 'optional',
    data: payload
  })
  saveSession(data)
  return data
}

function saveSession(data) {
  const source = data || {}
  const normalizedUserType = sessionState.normalizeUserType(
    source.userType !== undefined ? source.userType : (source.role !== undefined ? source.role : source.userRole)
  )
  sessionState.setState({
    token: source.token,
    userType: normalizedUserType,
    userOpenid: source.openid || source.userOpenid || ''
  })
}

function logout() {
  sessionState.clearState()
}

function getSessionState() {
  return sessionState.getState()
}

function getProfileCompleteness() {
  return request({
    url: '/api/user/profile/completeness',
    method: 'GET',
    authMode: 'required'
  })
}

function completeProfile(payload) {
  return request({
    url: '/api/user/profile/complete',
    method: 'PUT',
    authMode: 'required',
    data: payload
  })
}

async function uploadAvatar(filePath) {
  const prepared = await prepareImageForUpload(filePath, { maxBytes: 700 * 1024 })
  return uploadFile({
    url: '/api/file/upload',
    filePath: prepared.filePath,
    authMode: 'required',
    formData: { biz: 'avatar' }
  })
}

function updateUserRegion(payload) {
  return request({
    url: '/api/user/region',
    method: 'PUT',
    authMode: 'required',
    data: payload
  })
}

module.exports = {
  passwordLogin,
  wechatLogin,
  wechatPhoneLogin,
  saveSession,
  logout,
  getSessionState,
  getProfileCompleteness,
  completeProfile,
  uploadAvatar,
  updateUserRegion
}
