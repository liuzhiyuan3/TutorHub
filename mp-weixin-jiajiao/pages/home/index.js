const { request } = require('../../utils/request')
const { ensureRole, getLoginState, getRoleMode, ensureLogin } = require('../../utils/auth-guard')
const globalStore = require('../../utils/global-store')
const sessionState = require('../../utils/session-state')
const { normalizeMediaUrl, logMediaDebug, pickTeacherImage } = require('../../utils/media-url')

Page({
  data: {
    userTypeText: '游客',
    roleMode: 'guest',
    loading: false,
    error: '',
    cityName: '全国',
    serviceHotline: '4006-179-958',
    slides: [],
    ads: [],
    hotSubjects: [],
    hotRegions: [],
    hotSchools: [],
    latestRequirements: [],
    latestDispatchCards: [],
    featuredTeachers: [],
    authSheetVisible: false,
    authSheetRole: '',
    authSheetMessage: ''
  },

  gateLoginAction(message, action, payload) {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingAction = { action, payload }
    if (!ensureLogin(message)) {
      if (state.loggedIn) this._pendingAction = null
      return false
    }
    this._pendingAction = null
    return true
  },

  gateRoleAction(role, message, action, payload) {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingAction = { action, payload }
    if (!ensureRole(role, message)) {
      if (state.loggedIn) this._pendingAction = null
      return false
    }
    this._pendingAction = null
    return true
  },

  runPendingAction() {
    const pending = this._pendingAction
    this._pendingAction = null
    if (!pending || typeof this[pending.action] !== 'function') return
    this[pending.action](pending.payload)
  },

  goRequirementCreateAction() {
    wx.navigateTo({ url: '/pages/requirement-create/index' })
  },

  goOrdersAction() {
    wx.switchTab({ url: '/pages/orders/index' })
  },

  onShow() {
    this.applyRegionState(globalStore.getRegion())
    this._regionListener = (region) => this.applyRegionState(region)
    globalStore.subscribeRegion(this._regionListener)

    const state = getLoginState()
    const roleMode = getRoleMode()
    const userTypeText = state.isTeacher ? '教员' : state.isParent ? '家长' : '游客'
    this.setData({ roleMode, userTypeText })

    this.tryRecoverRoleFromProfile()
    this.loadOverview()
  },

  onHide() {
    if (this._regionListener) {
      globalStore.unsubscribeRegion(this._regionListener)
      this._regionListener = null
    }
  },

  onUnload() {
    if (this._regionListener) {
      globalStore.unsubscribeRegion(this._regionListener)
      this._regionListener = null
    }
  },

  applyRegionState(region) {
    const nextName = (region && (region.regionName || region.district || region.city || region.province)) || '全国'
    if (nextName === this.data.cityName) return
    this.setData({ cityName: nextName })
  },

  async tryRecoverRoleFromProfile() {
    const state = getLoginState()
    if (!state.loggedIn) return
    if (state.isParent || state.isTeacher) return

    try {
      const me = await request({ url: '/api/user/me', silentAuthError: true, authMode: 'required' })
      const nextUserType = sessionState.normalizeUserType(me && me.userType)
      if (nextUserType !== 0 && nextUserType !== 1) return

      const current = sessionState.getState()
      sessionState.setState({
        token: current.token,
        userType: nextUserType,
        userOpenid: current.userOpenid
      })
      this.setData({
        roleMode: nextUserType === 1 ? 'teacher' : 'parent',
        userTypeText: nextUserType === 1 ? '教员' : '家长'
      })
    } catch (e) {
      // keep silent, avoid blocking home data rendering
    }
  },

  async loadOverview() {
    this.setData({ loading: true, error: '' })
    try {
      const [overview, slides, ads, featured] = await Promise.all([
        request({ url: '/api/home/overview', authMode: 'optional' }),
        request({ url: '/api/content/slides?module=0', authMode: 'optional' }),
        request({ url: '/api/content/advertising?source=mini', authMode: 'optional' }),
        request({ url: '/api/home/teachers/search?pageNo=1&pageSize=3&auditStatus=1&sortBy=hot', authMode: 'optional' })
      ])
      this.setData({
        loading: false,
        hotSubjects: (overview && overview.hotSubjects) || [],
        hotRegions: (overview && overview.hotRegions) || [],
        hotSchools: (overview && overview.hotSchools) || [],
        latestRequirements: ((overview && overview.latestRequirements) || []).slice(0, 5),
        latestDispatchCards: ((overview && overview.latestDispatchCards) || []).slice(0, 5),
        featuredTeachers: ((featured && featured.records) || []).map((item) => {
          const raw = pickTeacherImage(item)
          const normalized = normalizeMediaUrl(raw)
          return {
            ...item,
            teacherPhotoRaw: raw,
            teacherPhoto: normalized.url,
            teacherPhotoWarn: normalized.warn,
            teacherPhotoReason: normalized.reason,
            avatarError: false
          }
        }),
        slides: slides || [],
        ads: ads || []
      })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '首页数据加载失败' })
    }
  },

  onTapSubject(e) {
    const subjectId = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/teachers/index?subjectId=${subjectId || ''}` })
  },

  goTeachers() {
    wx.navigateTo({ url: '/pages/teachers/index' })
  },

  goRequirementCreate() {
    const state = getLoginState()
    if (state.isTeacher) {
      wx.switchTab({ url: '/pages/profile/index' })
      return
    }
    if (!this.gateRoleAction('parent', '发布需求需要家长登录，是否前往登录？', 'goRequirementCreateAction')) return
    this.goRequirementCreateAction()
  },

  goRequirements() {
    wx.switchTab({ url: '/pages/requirements/index' })
  },

  onTapRegion(e) {
    const regionId = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/teachers/index?regionId=${regionId || ''}` })
  },

  onTapSchool(e) {
    const keyword = e.currentTarget.dataset.name || ''
    wx.navigateTo({ url: `/pages/teachers/index?schoolKeyword=${encodeURIComponent(keyword)}` })
  },

  goTeacherDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: `/pages/teacher-detail/index?id=${id}` })
  },

  onFeaturedAvatarError(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    const next = (this.data.featuredTeachers || []).map((item) => {
      if (String(item.teacherId) !== String(id)) return item
      logMediaDebug('home-featured-avatar-error', {
        teacherId: item.teacherId,
        raw: item.teacherPhotoRaw,
        normalized: item.teacherPhoto,
        reason: item.teacherPhotoReason
      })
      if (item.teacherPhotoWarn) {
        wx.showToast({ title: '该图片链接为http，真机可能被拦截', icon: 'none' })
      }
      return { ...item, avatarError: true }
    })
    this.setData({ featuredTeachers: next })
  },

  goDispatchDetail(e) {
    const orderId = e.currentTarget.dataset.id
    if (!orderId) return
    wx.navigateTo({ url: `/pages/dispatch-detail/index?orderId=${encodeURIComponent(orderId)}` })
  },

  goOrders() {
    if (!this.gateLoginAction('查看我的订单需要登录，是否前往登录？', 'goOrdersAction')) return
    this.goOrdersAction()
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/index' })
  },

  openSlide(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '链接已复制', icon: 'none' })
    })
  },

  openAd(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '广告链接已复制', icon: 'none' })
    })
  },

  openAuthSheet(role, message) {
    this.setData({
      authSheetVisible: true,
      authSheetRole: role || '',
      authSheetMessage: message || ''
    })
  },

  onAuthSheetClose() {
    this._pendingAction = null
    this.setData({ authSheetVisible: false, authSheetRole: '', authSheetMessage: '' })
  },

  onAuthSheetSuccess() {
    const roleMode = getRoleMode()
    const state = getLoginState()
    const userTypeText = state.isTeacher ? '教员' : state.isParent ? '家长' : '游客'
    this.setData({
      authSheetVisible: false,
      authSheetRole: '',
      authSheetMessage: '',
      roleMode,
      userTypeText
    })
    this.runPendingAction()
  }
})
