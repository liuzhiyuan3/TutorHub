const sessionState = require('./utils/session-state')
const globalStore = require('./utils/global-store')
const { resolveRegionSmart } = require('./utils/location-service')
const runtimeConfig = require('./config/runtime')
const REGION_AUTO_REFRESH_TTL = 20 * 60 * 1000

App({
  globalData: {
    baseUrl: '',
    amapKey: String((runtimeConfig && runtimeConfig.amapKey) || '').trim(),
    baseUrlSource: '',
    envVersion: '',
    token: '',
    userType: null,
    userOpenid: wx.getStorageSync('userOpenid') || ''
  },
  _regionRefreshing: false,
  _regionRefreshAt: 0,
  onLaunch() {
    const resolvedRuntime = runtimeConfig.getRuntimeConfig ? runtimeConfig.getRuntimeConfig() : runtimeConfig
    this.globalData.baseUrl = String((resolvedRuntime && resolvedRuntime.baseUrl) || 'http://127.0.0.1:8080').trim()
    this.globalData.baseUrlSource = String((resolvedRuntime && resolvedRuntime.baseUrlSource) || 'unknown')
    this.globalData.envVersion = String((resolvedRuntime && resolvedRuntime.envVersion) || '')
    this.globalData.amapKey = String((resolvedRuntime && resolvedRuntime.amapKey) || '').trim()
    const state = sessionState.getState()
    sessionState.setState(state)
    globalStore.hydrateRegionFromCache()
    this.tryRefreshRegion()
  },
  onShow() {
    const now = Date.now()
    // 回前台时做轻量防抖，避免短时间重复触发检查
    if (now - Number(this._regionRefreshAt || 0) < 15 * 1000) {
      return
    }
    this.tryRefreshRegion()
  },
  async tryRefreshRegion(forceRefresh) {
    if (this._regionRefreshing) return
    this._regionRefreshing = true
    try {
      const resolved = await resolveRegionSmart({
        forceRefresh: !!forceRefresh,
        ttlMs: REGION_AUTO_REFRESH_TTL
      })
      if (resolved && resolved.regionName) {
        globalStore.setRegion(resolved)
      }
    } catch (e) {
      // 地区刷新失败不影响主流程
    } finally {
      this._regionRefreshing = false
      this._regionRefreshAt = Date.now()
    }
  },
  setBaseUrl(nextUrl, persist) {
    const normalized = String(nextUrl || '').trim().replace(/\/+$/, '')
    if (!normalized) return false
    this.globalData.baseUrl = normalized
    this.globalData.baseUrlSource = persist ? 'manual:override' : 'manual:session'
    if (persist !== false && wx && wx.setStorageSync && runtimeConfig.BASE_URL_OVERRIDE_KEY) {
      wx.setStorageSync(runtimeConfig.BASE_URL_OVERRIDE_KEY, normalized)
    }
    return true
  },
  clearBaseUrlOverride() {
    if (wx && wx.removeStorageSync && runtimeConfig.BASE_URL_OVERRIDE_KEY) {
      wx.removeStorageSync(runtimeConfig.BASE_URL_OVERRIDE_KEY)
    }
  }
})
