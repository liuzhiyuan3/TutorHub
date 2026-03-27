const sessionState = require('./utils/session-state')
const globalStore = require('./utils/global-store')
const { resolveRegionWithFallback } = require('./utils/location-service')

App({
  globalData: {
    baseUrl: 'http://localhost:8080',
    token: '',
    userType: null,
    userOpenid: wx.getStorageSync('userOpenid') || ''
  },
  _regionRefreshing: false,
  _regionRefreshAt: 0,
  onLaunch() {
    const state = sessionState.getState()
    sessionState.setState(state)
    globalStore.hydrateRegionFromCache()
    this.tryRefreshRegion()
  },
  onShow() {
    const now = Date.now()
    // 回前台时做轻量刷新，避免历史登录态长期停留在默认地区
    if (now - Number(this._regionRefreshAt || 0) < 60 * 1000) {
      return
    }
    this.tryRefreshRegion()
  },
  async tryRefreshRegion() {
    if (this._regionRefreshing) return
    this._regionRefreshing = true
    try {
      const resolved = await resolveRegionWithFallback()
      if (resolved && resolved.regionName) {
        globalStore.setRegion(resolved)
      }
    } catch (e) {
      // 地区刷新失败不影响主流程
    } finally {
      this._regionRefreshing = false
      this._regionRefreshAt = Date.now()
    }
  }
})
