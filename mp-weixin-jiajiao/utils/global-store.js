const CACHE_KEY = 'globalRegionState'

const state = {
  regionCode: '',
  regionName: '',
  province: '',
  city: '',
  district: '',
  latitude: null,
  longitude: null,
  source: 'default',
  syncAt: 0
}

const listeners = []

function cloneState() {
  return { ...state }
}

function notify() {
  const snapshot = cloneState()
  listeners.slice().forEach((handler) => {
    try {
      handler(snapshot)
    } catch (e) {
      // ignore individual listener errors
    }
  })
}

function setRegion(payload) {
  const next = payload || {}
  const prevRegionCode = state.regionCode
  state.regionCode = String(next.regionCode || '')
  state.regionName = String(next.regionName || '')
  state.province = String(next.province || '')
  state.city = String(next.city || '')
  state.district = String(next.district || '')
  state.latitude = next.latitude == null ? null : Number(next.latitude)
  state.longitude = next.longitude == null ? null : Number(next.longitude)
  state.source = String(next.source || 'default')
  state.syncAt = Number(next.syncAt || Date.now())
  wx.setStorageSync(CACHE_KEY, cloneState())
  if (prevRegionCode !== state.regionCode || !prevRegionCode) {
    notify()
    return
  }
  notify()
}

function getRegion() {
  return cloneState()
}

function subscribeRegion(handler) {
  if (typeof handler !== 'function') return
  if (listeners.includes(handler)) return
  listeners.push(handler)
}

function unsubscribeRegion(handler) {
  const index = listeners.indexOf(handler)
  if (index >= 0) listeners.splice(index, 1)
}

function hydrateRegionFromCache() {
  const cached = wx.getStorageSync(CACHE_KEY)
  if (!cached || typeof cached !== 'object') return cloneState()
  setRegion({
    regionCode: cached.regionCode,
    regionName: cached.regionName,
    province: cached.province,
    city: cached.city,
    district: cached.district,
    latitude: cached.latitude,
    longitude: cached.longitude,
    source: cached.source || 'cache',
    syncAt: cached.syncAt || Date.now()
  })
  return cloneState()
}

module.exports = {
  setRegion,
  getRegion,
  subscribeRegion,
  unsubscribeRegion,
  hydrateRegionFromCache
}
