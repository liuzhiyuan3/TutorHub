function getAuthSetting() {
  return new Promise((resolve, reject) => {
    wx.getSetting({
      success: (res) => resolve((res && res.authSetting) || {}),
      fail: (err) => reject(err)
    })
  })
}

function authorizeLocation() {
  return new Promise((resolve, reject) => {
    wx.authorize({
      scope: 'scope.userLocation',
      success: () => resolve(true),
      fail: (err) => reject(err)
    })
  })
}

function openSettingForLocation() {
  return new Promise((resolve, reject) => {
    wx.openSetting({
      success: (res) => {
        const auth = (res && res.authSetting) || {}
        resolve(!!auth['scope.userLocation'])
      },
      fail: (err) => reject(err)
    })
  })
}

function classifyLocationError(error) {
  if (error && error.reason) return String(error.reason)
  const text = String((error && (error.errMsg || error.message)) || '').toLowerCase()
  if (!text) return 'unknown'
  if (text.includes('cancel')) return 'cancel'
  if (text.includes('auth deny') || text.includes('auth denied') || text.includes('permission') || text.includes('authorize')) return 'auth'
  if (text.includes('locationswitchoff') || text.includes('location service') || text.includes('gps') || text.includes('system location')) return 'service_off'
  if (text.includes('timeout')) return 'timeout'
  if (text.includes('network')) return 'network'
  if (text.includes('chooselocation')) return 'choose'
  return 'unknown'
}

function toLocationErrorMessage(error) {
  const reason = classifyLocationError(error)
  if (reason === 'auth') return '未授权定位，请在设置中开启定位权限后重试'
  if (reason === 'service_off') return '系统定位服务未开启，请打开手机定位后重试'
  if (reason === 'timeout') return '定位超时，请稍后重试'
  if (reason === 'cancel') return '已取消定位'
  if (reason === 'network') return '网络异常，请检查网络后重试'
  if (reason === 'choose') return '地图选点失败，请稍后重试'
  return '定位失败，请稍后重试'
}

function buildLocationError(error, fallbackReason) {
  const reason = fallbackReason || classifyLocationError(error)
  const normalized = new Error(toLocationErrorMessage({ reason }))
  normalized.reason = reason
  normalized.errMsg = (error && error.errMsg) || ''
  normalized.rawError = error || null
  return normalized
}

function tryReadSystemLocationEnabled() {
  try {
    if (wx.getAppAuthorizeSetting) {
      const setting = wx.getAppAuthorizeSetting()
      if (setting && setting.locationAuthorized === false) return false
    } else if (wx.getSystemInfoSync) {
      const info = wx.getSystemInfoSync()
      if (info && info.locationEnabled === false) return false
    }
  } catch (e) {}
  return true
}

function getLocationRaw(type) {
  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: type || 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 4000,
      success: (res) => resolve(res),
      fail: (err) => reject(buildLocationError(err))
    })
  })
}

function normalizeCoordinate(value) {
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

function normalizeNumber(value, fallback) {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

function chooseLocation(options) {
  const params = options || {}
  const latitude = normalizeCoordinate(params.latitude)
  const longitude = normalizeCoordinate(params.longitude)
  const payload = {}
  if (latitude !== null) payload.latitude = latitude
  if (longitude !== null) payload.longitude = longitude

  return new Promise((resolve, reject) => {
    wx.chooseLocation({
      ...payload,
      success: (res) => {
        const normalized = {
          name: String((res && res.name) || ''),
          address: String((res && res.address) || ''),
          latitude: normalizeCoordinate(res && res.latitude),
          longitude: normalizeCoordinate(res && res.longitude)
        }
        if (normalized.latitude === null || normalized.longitude === null) {
          reject(buildLocationError({ errMsg: 'chooseLocation invalid coordinate' }, 'choose'))
          return
        }
        resolve(normalized)
      },
      fail: (err) => reject(buildLocationError(err))
    })
  })
}

function openLocation(params) {
  return new Promise((resolve, reject) => {
    wx.openLocation({
      latitude: Number(params && params.latitude),
      longitude: Number(params && params.longitude),
      name: (params && params.name) || '当前位置',
      address: (params && params.address) || '',
      scale: 16,
      fail: (err) => reject(buildLocationError(err)),
      success: () => resolve(true)
    })
  })
}

async function ensureLocationAuth() {
  const setting = await getAuthSetting()
  if (setting['scope.userLocation'] === true) return true
  if (setting['scope.userLocation'] === false) {
    const granted = await openSettingForLocation()
    if (!granted) throw buildLocationError({ errMsg: 'scope.userLocation denied' }, 'auth')
    return true
  }
  try {
    await authorizeLocation()
    return true
  } catch (e) {
    throw buildLocationError(e, 'auth')
  }
}

async function getCurrentLocationWithAuth() {
  if (!tryReadSystemLocationEnabled()) {
    throw buildLocationError({ errMsg: 'system location disabled' }, 'service_off')
  }
  await ensureLocationAuth()
  try {
    const location = await getLocationRaw('gcj02')
    return {
      latitude: normalizeNumber(location.latitude, 0),
      longitude: normalizeNumber(location.longitude, 0),
      speed: normalizeNumber(location.speed, 0),
      accuracy: normalizeNumber(location.accuracy, 0),
      altitude: normalizeNumber(location.altitude, null),
      verticalAccuracy: normalizeNumber(location.verticalAccuracy, 0),
      horizontalAccuracy: normalizeNumber(location.horizontalAccuracy, 0),
      source: 'gps'
    }
  } catch (e) {
    throw buildLocationError(e)
  }
}

async function chooseLocationWithAuth(options) {
  await ensureLocationAuth()
  try {
    return await chooseLocation(options)
  } catch (e) {
    throw buildLocationError(e)
  }
}

module.exports = {
  getCurrentLocationWithAuth,
  chooseLocationWithAuth,
  chooseLocation,
  openLocation,
  classifyLocationError,
  toLocationErrorMessage
}
