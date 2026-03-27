const authService = require('../../utils/auth-service')
const globalStore = require('../../utils/global-store')
const { resolveRegionWithFallback, toFallbackMessage } = require('../../utils/location-service')
const { normalizeUserType } = require('../../utils/session-state')

Page({
  data: {
    redirect: '',
    fromSwitch: false,
    submitting: false,
    authStage: 'idle',
    showConfirmSheet: false,
    loginEntryMode: 'wechat',
    submitText: '账号登录',
    primaryBtnText: '微信一键登录',
    errorText: '',
    showProfileCompleteSheet: false,
    profileRole: 'parent',
    profileCanSkip: true,
    profileMissingFields: [],
    profileInitNickName: '',
    profileInitAvatarUrl: '',
    currentUserTypeLabel: '家长',
    form: {
      account: '',
      password: '',
      userType: 0
    },
    userTypeOptions: [
      { label: '家长', value: 0 },
      { label: '教员', value: 1 }
    ]
  },

  onLoad(options) {
    const next = {
      redirect: (options && options.redirect) || '',
      fromSwitch: options && options.fromSwitch === '1'
    }
    if (options && options.userType !== undefined) {
      const parsed = Number(options.userType)
      if (parsed === 0 || parsed === 1) {
        next['form.userType'] = parsed
      }
    }
    if (next.fromSwitch) {
      next['form.account'] = ''
      next['form.password'] = ''
    }
    this.setData(next)
    this.resetSubmitState()
  },

  onShow() {
    if (!this.data.submitting && this.data.authStage !== 'idle') {
      this.resetSubmitState()
    }
    this.refreshRegionOnShow()
  },

  async refreshRegionOnShow() {
    try {
      const resolved = await resolveRegionWithFallback()
      globalStore.setRegion(resolved)
    } catch (e) {
      // keep silent on login page
    }
  },

  switchToWechatLogin() {
    if (this.data.submitting) return
    this.setData({ loginEntryMode: 'wechat', errorText: '' })
  },

  switchToAccountLogin() {
    if (this.data.submitting) return
    this.setData({
      loginEntryMode: 'account',
      showConfirmSheet: false,
      authStage: 'idle',
      errorText: ''
    }, () => this.refreshViewState())
  },

  refreshViewState() {
    const selectedType = Number(this.data.form.userType)
    const options = this.data.userTypeOptions || []
    const current = options.find((item) => Number(item.value) === selectedType) || options[0]
    let primaryBtnText = '微信一键登录'
    if (this.data.submitting || this.data.authStage === 'authorizing') {
      primaryBtnText = '授权中...'
    } else if (this.data.authStage === 'failed') {
      primaryBtnText = '重试微信登录'
    }

    this.setData({
      currentUserTypeLabel: current ? current.label : '家长',
      submitText: this.data.submitting ? '登录中...' : '账号登录',
      primaryBtnText
    })
  },

  resetSubmitState() {
    this.setData({
      submitting: false,
      authStage: 'idle',
      errorText: '',
      showConfirmSheet: false
    }, () => this.refreshViewState())
  },

  onPullDownRefresh() {
    this.resetSubmitState()
    wx.stopPullDownRefresh()
  },

  openConfirmSheet() {
    if (this.data.submitting) return
    this.setData({
      showConfirmSheet: true,
      authStage: 'confirm',
      errorText: ''
    })
  },

  closeConfirmSheet() {
    if (this.data.submitting) return
    this.setData({
      showConfirmSheet: false,
      authStage: 'idle'
    }, () => this.refreshViewState())
  },

  onInput(e) {
    const key = e.currentTarget.dataset.key
    const value = (e && e.detail && (e.detail.value !== undefined ? e.detail.value : e.detail)) || ''
    this.setData({ [`form.${key}`]: value })
  },

  onTypeChange(e) {
    const idx = Number(e.detail.value)
    const options = this.data.userTypeOptions || []
    const option = options[idx] || options[0] || { value: 0 }
    this.setData({ 'form.userType': Number(option.value) }, () => this.refreshViewState())
  },

  isNetworkLikeError(errorMessage) {
    const msg = String(errorMessage || '').toLowerCase()
    return msg.includes('timeout') || msg.includes('timed out') || msg.includes('network') || msg.includes('fail')
  },

  sleep(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms))
  },

  async withRetry(task, maxRetries, delayMs) {
    let lastError = null
    for (let i = 0; i <= maxRetries; i += 1) {
      try {
        return await task()
      } catch (e) {
        lastError = e
        if (i >= maxRetries || !this.isNetworkLikeError(e && e.message)) {
          throw e
        }
        await this.sleep(delayMs)
      }
    }
    throw lastError || new Error('请求失败')
  },

  async submit() {
    if (this.data.submitting) return
    if (!String(this.data.form.account || '').trim()) {
      wx.showToast({ title: '请输入账号', icon: 'none' })
      return
    }
    if (!String(this.data.form.password || '').trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ submitting: true, errorText: '' }, () => this.refreshViewState())

    try {
      const selectedUserType = Number(this.data.form.userType)
      const data = await this.withRetry(() => authService.passwordLogin({
        account: this.data.form.account,
        password: this.data.form.password
      }), 1, 450)

      if (!data) {
        throw new Error('仅支持用户登录')
      }
      await this.completeLogin(data, selectedUserType)
    } catch (e) {
      const msg = e && e.message ? e.message : '登录失败'
      wx.showToast({ title: msg, icon: 'none' })
      this.setData({ authStage: 'failed', errorText: msg })
    } finally {
      this.setData({ submitting: false }, () => this.refreshViewState())
    }
  },

  async confirmWechatLogin() {
    if (this.data.submitting) return
    this.setData({
      submitting: true,
      authStage: 'authorizing',
      showConfirmSheet: false,
      errorText: ''
    }, () => this.refreshViewState())

    try {
      const selectedUserType = Number(this.data.form.userType)
      const code = await this.withRetry(() => this.getWechatLoginCode(), 1, 300)
      const data = await this.withRetry(() => authService.wechatLogin({
        code,
        userType: selectedUserType
      }), 1, 450)
      await this.completeLogin(data, selectedUserType)
      this.setData({ authStage: 'success' })
    } catch (e) {
      const msg = e && e.message ? e.message : '微信登录失败'
      this.setData({ authStage: 'failed', errorText: msg })
      wx.showToast({ title: msg, icon: 'none' })
    } finally {
      this.setData({ submitting: false }, () => this.refreshViewState())
    }
  },

  getWechatLoginCode() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          const code = res && res.code
          if (!code) {
            reject(new Error('微信登录失败：未获取到 code'))
            return
          }
          resolve(code)
        },
        fail: () => reject(new Error('微信登录失败：调用 wx.login 异常'))
      })
    })
  },

  async syncRegionAfterLogin() {
    const resolved = await resolveRegionWithFallback()
    globalStore.setRegion(resolved)
    if (resolved && resolved.source !== 'gps') {
      wx.showToast({
        title: toFallbackMessage(resolved.fallbackReason),
        icon: 'none'
      })
    }
    try {
      await authService.updateUserRegion({
        regionCode: resolved.regionCode || '',
        regionName: resolved.regionName || '',
        regionProvince: resolved.province || '',
        regionCity: resolved.city || '',
        regionDistrict: resolved.district || '',
        regionSource: resolved.source || 'default',
        userLocationAddress: [resolved.province, resolved.city, resolved.district].filter(Boolean).join(''),
        userLocationLongitude: resolved.longitude,
        userLocationLatitude: resolved.latitude
      })
    } catch (e) {
      // region persistence failure should not block login navigation
    }
  },

  async completeLogin(data, selectedUserType) {
    const realUserType = normalizeUserType(
      data && (data.userType !== undefined ? data.userType : (data.role !== undefined ? data.role : data.userRole))
    )
    if (selectedUserType === 0 || selectedUserType === 1) {
      if (realUserType !== null && realUserType !== selectedUserType) {
        this.clearLoginState()
        wx.showModal({
          title: '身份不匹配',
          content: `当前账号是${realUserType === 1 ? '教员' : '家长'}身份，请使用${selectedUserType === 1 ? '教员' : '家长'}身份登录`,
          showCancel: false
        })
        return
      }
    }

    authService.saveSession(data)
    const routed = await this.routeByCompleteness(data)
    if (!routed) return

    await this.syncRegionAfterLogin()
    this.navigateAfterLogin()
  },

  async routeByCompleteness(loginData) {
    try {
      const status = await authService.getProfileCompleteness()
      if (status && status.ready) return true

      const loginUserType = normalizeUserType(
        loginData && (loginData.userType !== undefined ? loginData.userType : (loginData.role !== undefined ? loginData.role : loginData.userRole))
      )
      const role = status && status.role ? status.role : (loginUserType === 1 ? 'teacher' : 'parent')
      const canSkip = !(role === 'teacher')
      const userName = status && status.userName ? status.userName : ''
      const userPortrait = status && status.userPortrait ? status.userPortrait : ''

      this.setData({
        showProfileCompleteSheet: true,
        profileRole: role,
        profileCanSkip: canSkip,
        profileMissingFields: (status && status.missingFields) || [],
        profileInitNickName: userName,
        profileInitAvatarUrl: userPortrait || ''
      })
      return false
    } catch (e) {
      const msg = e && e.message ? e.message : '资料检查失败'
      wx.showToast({ title: msg, icon: 'none' })
      return true
    }
  },

  navigateAfterLogin() {
    const redirect = this.data.redirect ? decodeURIComponent(this.data.redirect) : ''
    const tabPaths = ['/pages/home/index', '/pages/requirements/index', '/pages/orders/index', '/pages/profile/index']

    if (redirect) {
      const purePath = redirect.split('?')[0]
      if (tabPaths.includes(purePath)) {
        wx.switchTab({ url: purePath })
      } else {
        wx.redirectTo({ url: redirect })
      }
    } else {
      wx.switchTab({ url: '/pages/home/index' })
    }
  },

  onProfileCompleteSuccess() {
    this.setData({ showProfileCompleteSheet: false })
    this.syncRegionAfterLogin().finally(() => this.navigateAfterLogin())
  },

  onProfileCompleteSkip() {
    if (this.data.profileRole === 'teacher') {
      wx.showToast({ title: '教员需完成资料后才能继续', icon: 'none' })
      return
    }
    this.setData({ showProfileCompleteSheet: false })
    this.syncRegionAfterLogin().finally(() => this.navigateAfterLogin())
  },

  onProfileCompleteClose() {
    if (this.data.profileRole === 'teacher') {
      wx.showToast({ title: '请先完善教员资料', icon: 'none' })
      return
    }
    this.setData({ showProfileCompleteSheet: false })
    this.syncRegionAfterLogin().finally(() => this.navigateAfterLogin())
  },

  clearLoginState() {
    authService.logout()
  },

  goRegister() {
    const userType = Number(this.data.form.userType) === 1 ? 1 : 0
    wx.navigateTo({ url: `/pages/register/index?userType=${userType}` })
  }
})
