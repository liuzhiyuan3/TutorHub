const sessionState = require('./session-state')

function getLoginState() {
  const state = sessionState.getState()
  const token = state.token
  const userType = state.userType
  return {
    loggedIn: !!token,
    token,
    userType,
    isParent: userType === 0,
    isTeacher: userType === 1
  }
}

function getRoleMode() {
  const state = getLoginState()
  if (!state.loggedIn) return 'guest'
  if (state.isTeacher) return 'teacher'
  if (state.isParent) return 'parent'
  return 'guest'
}

const rolePermissions = {
  guest: {
    canPublishRequirement: false,
    canReceiveRequirement: false,
    canUpdateOrderAsParent: false,
    canUpdateOrderAsTeacher: false
  },
  parent: {
    canPublishRequirement: true,
    canReceiveRequirement: false,
    canUpdateOrderAsParent: true,
    canUpdateOrderAsTeacher: false
  },
  teacher: {
    canPublishRequirement: false,
    canReceiveRequirement: true,
    canUpdateOrderAsParent: false,
    canUpdateOrderAsTeacher: true
  }
}

function getRolePermissions() {
  const roleMode = getRoleMode()
  return rolePermissions[roleMode] || rolePermissions.guest
}

function currentPathWithQuery() {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  if (!current) return '/pages/home/index'
  const route = current.route ? `/${current.route}` : '/pages/home/index'
  const options = current.options || {}
  const query = Object.keys(options)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(options[key])}`)
    .join('&')
  return query ? `${route}?${query}` : route
}

function goLoginWithRedirect() {
  const redirect = encodeURIComponent(currentPathWithQuery())
  wx.navigateTo({ url: `/pages/login/index?redirect=${redirect}` })
}

function tryOpenAuthSheet(role, message) {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  if (!current || typeof current.openAuthSheet !== 'function') {
    return false
  }
  current.openAuthSheet(role || '', message || '')
  return true
}

function clearLoginState() {
  sessionState.clearState()
}

function ensureLogin(message) {
  const state = getLoginState()
  if (state.loggedIn) return true
  if (tryOpenAuthSheet('', message)) return false
  wx.showModal({
    title: '请先登录',
    content: message || '该功能需要登录后使用，是否前往登录？',
    confirmText: '去登录',
    success: (res) => {
      if (res.confirm) goLoginWithRedirect()
    }
  })
  return false
}

function ensureRole(role, message) {
  const state = getLoginState()
  if (!state.loggedIn) {
    if (tryOpenAuthSheet(role, message)) return false
    if (!ensureLogin(message)) return false
  }
  if (role === 'parent' && state.isParent) return true
  if (role === 'teacher' && state.isTeacher) return true
  wx.showToast({
    title: role === 'teacher' ? '仅教员可操作' : '仅家长可操作',
    icon: 'none'
  })
  return false
}

function requireAction(role, options) {
  const opts = options || {}
  if (role && !ensureRole(role, opts.message)) return false
  if (!role && !ensureLogin(opts.message)) return false
  if (typeof opts.action === 'function') {
    opts.action()
  }
  return true
}

module.exports = {
  getLoginState,
  getRoleMode,
  getRolePermissions,
  clearLoginState,
  ensureLogin,
  ensureRole,
  requireAction,
  goLoginWithRedirect
}
