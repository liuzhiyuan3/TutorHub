const sessionState = require('./session-state')

function resolveBaseUrl() {
  const app = getApp && getApp()
  const fromGlobal = app && app.globalData ? app.globalData.baseUrl : ''
  return String(fromGlobal || 'http://localhost:8080').trim()
}

function clearLoginState() {
  sessionState.clearState()
}

function buildError(message, code, httpStatus) {
  const err = new Error(message || '请求失败')
  if (code !== undefined) err.code = code
  if (httpStatus !== undefined) err.httpStatus = httpStatus
  return err
}

function normalizeErrorMessage(message, fallback) {
  const text = String(message || '').trim()
  if (!text) return fallback || '请求失败'
  if (text.toLowerCase().includes('maximum upload size exceeded')) {
    return '图片过大，请选择更小图片或压缩后重试'
  }
  return text
}

function handleUnauthorized(options) {
  clearLoginState()
  if (options && options.silentAuthError) return

  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current && current.route ? `/${current.route}` : '/pages/home/index'
  if (route === '/pages/login/index') return

  if (current && typeof current.openAuthSheet === 'function') {
    current.openAuthSheet('', '登录已失效，请重新授权登录')
    return
  }

  const redirect = encodeURIComponent(route)
  wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
  setTimeout(() => {
    wx.navigateTo({ url: `/pages/login/index?redirect=${redirect}` })
  }, 120)
}

function shouldRetryAsAnonymous({ authMode, retryAnonymousOn401, usedAuth }) {
  return authMode === 'optional' && retryAnonymousOn401 && usedAuth
}

function request({
  url,
  method = 'GET',
  data = {},
  silentAuthError = false,
  authMode = 'optional',
  retryAnonymousOn401 = true
}) {
  const baseUrl = resolveBaseUrl()

  const doRequest = (useAuth) => new Promise((resolve, reject) => {
    const session = sessionState.getState()
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      timeout: 20000,
      header: {
        Authorization: useAuth && session.token ? `Bearer ${session.token}` : ''
      },
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })

  const handleFail = (reject, err) => {
    const text = String((err && err.errMsg) || '')
    if (text.includes('timeout')) {
      reject(buildError('请求超时，请确认后端服务已启动'))
      return
    }
    if (text.includes('fail')) {
      reject(buildError('无法连接后端服务，请检查 baseUrl 和端口'))
      return
    }
    reject(buildError('网络异常，请稍后重试'))
  }

  const parseBodyResult = ({ res, usedAuth, resolve, reject, retryFn }) => {
    const body = res.data || {}
    if (res.statusCode === 401) {
      if (shouldRetryAsAnonymous({ authMode, retryAnonymousOn401, usedAuth })) {
        clearLoginState()
        retryFn()
        return
      }
      if (authMode === 'required') {
        handleUnauthorized({ silentAuthError })
      } else {
        clearLoginState()
      }
      reject(buildError('登录已过期，请重新登录', 401, 401))
      return
    }

    if (body.code !== 0) {
      const message = normalizeErrorMessage(body.message, '请求失败')
      const isUnauthorized = body.code === 401 || message.includes('登录')
      if (isUnauthorized && shouldRetryAsAnonymous({ authMode, retryAnonymousOn401, usedAuth })) {
        clearLoginState()
        retryFn()
        return
      }
      if (isUnauthorized && authMode === 'required') {
        handleUnauthorized({ silentAuthError })
      } else if (isUnauthorized) {
        clearLoginState()
      }
      reject(buildError(message, body.code || res.statusCode, res.statusCode))
      return
    }

    resolve(body.data)
  }

  return new Promise((resolve, reject) => {
    const session = sessionState.getState()
    const firstUseAuth = authMode === 'required' ? true : !!session.token
    let retriedAnonymous = false

    const run = (useAuth) => {
      doRequest(useAuth)
        .then((res) => {
          parseBodyResult({
            res,
            usedAuth: useAuth,
            resolve,
            reject,
            retryFn: () => {
              if (retriedAnonymous) {
                reject(buildError('请求失败'))
                return
              }
              retriedAnonymous = true
              run(false)
            }
          })
        })
        .catch((err) => handleFail(reject, err))
    }

    run(firstUseAuth)
  })
}

function uploadFile({
  url,
  filePath,
  name = 'file',
  formData = {},
  silentAuthError = false,
  authMode = 'required',
  retryAnonymousOn401 = false
}) {
  const baseUrl = resolveBaseUrl()

  const doUpload = (useAuth) => new Promise((resolve, reject) => {
    const session = sessionState.getState()
    wx.uploadFile({
      url: `${baseUrl}${url}`,
      filePath,
      name,
      formData,
      timeout: 20000,
      header: {
        Authorization: useAuth && session.token ? `Bearer ${session.token}` : ''
      },
      success: (res) => resolve(res),
      fail: (err) => reject(err)
    })
  })

  return new Promise((resolve, reject) => {
    const session = sessionState.getState()
    const firstUseAuth = authMode === 'required' ? true : !!session.token
    let retriedAnonymous = false

    const run = (useAuth) => {
      doUpload(useAuth)
        .then((res) => {
          let body = {}
          try {
            body = JSON.parse(res.data || '{}')
          } catch (e) {
            reject(buildError('上传失败：返回数据解析异常'))
            return
          }

          const unauthorized = res.statusCode === 401 || body.code === 401
          if (unauthorized && shouldRetryAsAnonymous({ authMode, retryAnonymousOn401, usedAuth })) {
            if (retriedAnonymous) {
              reject(buildError('上传失败'))
              return
            }
            clearLoginState()
            retriedAnonymous = true
            run(false)
            return
          }

          if (res.statusCode === 401) {
            if (authMode === 'required') {
              handleUnauthorized({ silentAuthError })
            } else {
              clearLoginState()
            }
            reject(buildError('登录已过期，请重新登录', 401, 401))
            return
          }

          if (body.code !== 0) {
            const message = normalizeErrorMessage(body.message, '上传失败')
            reject(buildError(message, body.code || res.statusCode, res.statusCode))
            return
          }

          resolve(body.data)
        })
        .catch((err) => {
          const text = String((err && err.errMsg) || '')
          if (text.includes('timeout')) {
            reject(buildError('上传超时，请稍后重试'))
            return
          }
          reject(buildError('上传失败，请检查网络连接'))
        })
    }

    run(firstUseAuth)
  })
}

module.exports = { request, uploadFile }
