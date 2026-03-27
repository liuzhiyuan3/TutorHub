const { request, uploadFile } = require('../../utils/request')
const authService = require('../../utils/auth-service')
const { getLoginState, getRoleMode, ensureLogin, ensureRole } = require('../../utils/auth-guard')
const {
  getCurrentLocationWithAuth,
  chooseLocationWithAuth,
  openLocation,
  classifyLocationError,
  toLocationErrorMessage
} = require('../../utils/location')
const globalStore = require('../../utils/global-store')
const { resolveRegionWithFallback, toFallbackMessage } = require('../../utils/location-service')
const sessionState = require('../../utils/session-state')

const LEGACY_BROKEN_AVATAR_PREFIX = 'https://mmbiz.qpic.cn/mmbiz/icTDbqWNOwNRna42FI242Lcia07jQodd2FJc8qfQ7Aiaf6v0Owr4KJ6SJ6VQkPqP5wM5v5v6fR5bLJ6f8uQ94m4A'

Page({
  data: {
    loading: false,
    error: '',
    guestMode: false,
    roleMode: 'guest',
    portraitUploading: false,
    stats: {
      favoriteCount: 0,
      appointmentCount: 0
    },
    teacherAuditText: '待完善',
    teacherTagClass: 'tag pending',
    teacherActionText: '完善资料',
    teacherActionHint: '完善后可参与接单',
    user: {},
    teacher: {
      teacherIdentity: '',
      teacherTutoringMethod: 2,
      teacherTeachingYears: 0,
      teacherSchool: '',
      teacherMajor: '',
      teacherEducation: '',
      teacherExperience: '',
      teacherSelfDescription: ''
    },
    authSheetVisible: false,
    authSheetRole: '',
    locationUpdating: false,
    currentRegionText: '未定位'
  },

  sanitizePortraitUrl(url) {
    const text = String(url || '').trim()
    if (!text) return ''
    if (text.startsWith(LEGACY_BROKEN_AVATAR_PREFIX)) return ''
    return text
  },

  async uploadImage(filePath, biz) {
    const data = await uploadFile({
      url: '/api/file/upload',
      filePath,
      name: 'file',
      authMode: 'required',
      formData: { biz }
    })
    return (data && data.url) || ''
  },

  chooseUserPortrait() {
    if (!ensureLogin('上传头像需要先登录，是否前往登录？')) return
    if (this.data.portraitUploading) return
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const filePath = res.tempFilePaths[0]
        if (!filePath) return
        this.setData({ portraitUploading: true })
        try {
          const url = await this.uploadImage(filePath, 'mini-user-portrait')
          if (!url) throw new Error('上传失败')
          this.setData({ 'user.userPortrait': url })
          await request({ url: '/api/user/me', method: 'PUT', authMode: 'required', data: this.data.user })
          wx.showToast({ title: '头像已更新', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: e.message || '头像上传失败', icon: 'none' })
        } finally {
          this.setData({ portraitUploading: false })
        }
      }
    })
  },

  onShow() {
    this.applyRegionState(globalStore.getRegion())
    this._regionListener = (region) => this.applyRegionState(region)
    globalStore.subscribeRegion(this._regionListener)
    this.refreshRegionOnShow()
    const state = getLoginState()
    if (!state.loggedIn) {
      this.setData({
        guestMode: true,
        roleMode: 'guest',
        loading: false,
        error: '',
        user: {},
        teacher: { ...this.data.teacher }
      })
      return
    }
    this.setData({ guestMode: false, roleMode: getRoleMode() })
    this.load()
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

  async refreshRegionOnShow() {
    try {
      const resolved = await resolveRegionWithFallback()
      globalStore.setRegion(resolved)
    } catch (e) {
      // ignore: location fallback already handled in service
    }
  },

  applyRegionState(region) {
    const text = (region && (region.regionName || region.district || region.city || region.province)) || '未定位'
    if (text === this.data.currentRegionText) return
    this.setData({ currentRegionText: text })
  },

  async load() {
    this.setData({ loading: true, error: '' })
    try {
      const user = await request({ url: '/api/user/me', authMode: 'required' })
      const nextUserType = sessionState.normalizeUserType(user && user.userType)
      if (nextUserType === 0 || nextUserType === 1) {
        const current = sessionState.getState()
        sessionState.setState({
          token: current.token,
          userType: nextUserType,
          userOpenid: current.userOpenid
        })
      }
      const nextRoleMode = nextUserType === 1 ? 'teacher' : (nextUserType === 0 ? 'parent' : this.data.roleMode)
      this.setData({
        roleMode: nextRoleMode,
        user: {
          ...user,
          userPortrait: this.sanitizePortraitUrl(user && user.userPortrait)
        }
      })
      const [favorites, appointments] = await Promise.all([
        request({ url: '/api/favorite/my/page?pageNo=1&pageSize=1', authMode: 'required' }),
        request({ url: '/api/appointment/my/page?pageNo=1&pageSize=1', authMode: 'required' })
      ])
      this.setData({
        'stats.favoriteCount': Number((favorites && favorites.total) || 0),
        'stats.appointmentCount': Number((appointments && appointments.total) || 0)
      })

      if (user.userType === 1) {
        const teacher = await request({ url: '/api/teacher/profile/me', authMode: 'required' })
        const nextTeacher = teacher ? { ...this.data.teacher, ...teacher } : { ...this.data.teacher }
        const meta = this.resolveTeacherAuditMeta(nextTeacher)
        this.setData({
          teacher: nextTeacher,
          teacherAuditText: meta.text,
          teacherTagClass: meta.tagClass,
          teacherActionText: meta.actionText,
          teacherActionHint: meta.hint
        })
      }
      this.setData({ loading: false })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '个人信息加载失败' })
    }
  },

  resolveTeacherAuditMeta(teacher) {
    const status = Number(teacher && teacher.teacherAuditStatus)
    const completed = !!(
      teacher &&
      String(teacher.teacherIdentity || '').trim() &&
      String(teacher.teacherSchool || '').trim() &&
      String(teacher.teacherMajor || '').trim()
    )
    if (!completed) {
      return {
        text: '待完善',
        tagClass: 'tag pending',
        actionText: '完善资料',
        hint: '请先完善资料，提交后等待审核'
      }
    }
    if (status === 2) {
      return {
        text: '审核拒绝',
        tagClass: 'tag error',
        actionText: '重新完善',
        hint: '资料被拒绝，请按要求修改后重新提交'
      }
    }
    if (status === 1) {
      return {
        text: '审核通过',
        tagClass: 'tag success',
        actionText: '编辑资料',
        hint: '资料已通过审核，可按需更新'
      }
    }
    return {
      text: '待审核',
      tagClass: 'tag info',
      actionText: '查看提交内容',
      hint: '资料已提交，等待平台审核'
    }
  },

  goTeacherProfileEdit() {
    if (!ensureRole('teacher', '仅教员可完善资料，是否前往登录？')) return
    wx.navigateTo({ url: '/pages/teacher-profile-edit/index' })
  },

  logout() {
    this.clearLoginState()
    wx.redirectTo({ url: '/pages/login/index' })
  },

  clearLoginState() {
    authService.logout()
  },

  switchIdentity() {
    if (!ensureLogin('切换身份需要先登录，是否前往登录？')) return
    const nextType = this.data.user && this.data.user.userType === 1 ? 0 : 1
    const nextLabel = nextType === 1 ? '教员' : '家长'
    wx.showModal({
      title: '切换身份',
      content: `将退出当前账号，并前往${nextLabel}登录页，是否继续？`,
      success: (res) => {
        if (!res.confirm) return
        this.clearLoginState()
        wx.redirectTo({
          url: `/pages/login/index?fromSwitch=1&userType=${nextType}&redirect=%2Fpages%2Fprofile%2Findex`
        })
      }
    })
  },

  goTeachers() {
    wx.navigateTo({ url: '/pages/teachers/index' })
  },

  goRequirementCreate() {
    if (!ensureRole('parent', '发布需求仅家长可操作，是否前往登录？')) return
    wx.navigateTo({ url: '/pages/requirement-create/index' })
  },

  goRequirements() {
    wx.switchTab({ url: '/pages/requirements/index' })
  },

  goOrders() {
    wx.switchTab({ url: '/pages/orders/index' })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/index?redirect=%2Fpages%2Fprofile%2Findex' })
  },

  formatCoordinateAddress(pos) {
    return `lng:${Number(pos.longitude).toFixed(6)},lat:${Number(pos.latitude).toFixed(6)}`
  },

  normalizeChosenLocation(selected) {
    return {
      longitude: Number(selected && selected.longitude),
      latitude: Number(selected && selected.latitude),
      accuracy: 0,
      source: 'choose',
      address: String((selected && (selected.address || selected.name)) || '').trim()
    }
  },

  pickLocationOnMap() {
    return new Promise((resolve, reject) => {
      wx.showModal({
        title: '定位失败',
        content: '可改用地图手动选点继续更新位置',
        confirmText: '地图选点',
        cancelText: '取消',
        success: async (res) => {
          if (!res.confirm) {
            reject(new Error('已取消定位'))
            return
          }
          try {
            const user = this.data.user || {}
            const seedLongitude = Number(user.userLocationLongitude)
            const seedLatitude = Number(user.userLocationLatitude)
            const chooseOptions = {}
            if (Number.isFinite(seedLatitude)) chooseOptions.latitude = seedLatitude
            if (Number.isFinite(seedLongitude)) chooseOptions.longitude = seedLongitude
            const selected = await chooseLocationWithAuth(chooseOptions)
            resolve(this.normalizeChosenLocation(selected))
          } catch (e) {
            reject(e)
          }
        },
        fail: () => reject(new Error('打开地图选点失败'))
      })
    })
  },

  async resolvePositionWithFallback() {
    try {
      return await getCurrentLocationWithAuth()
    } catch (e) {
      const reason = classifyLocationError(e)
      if (reason === 'cancel') throw e
      return this.pickLocationOnMap()
    }
  },

  async saveUserLocation(pos) {
    const address = (pos && pos.address) ? String(pos.address).trim() : this.formatCoordinateAddress(pos)
    await request({
      url: '/api/user/location',
      method: 'PUT',
      authMode: 'required',
      data: {
        userLocationAddress: address,
        userLocationLongitude: Number(pos.longitude),
        userLocationLatitude: Number(pos.latitude)
      }
    })
    return address
  },

  async syncRegionFromPosition(pos, savedAddress) {
    const resolved = await resolveRegionWithFallback()
    globalStore.setRegion(resolved)
    await authService.updateUserRegion({
      regionCode: resolved.regionCode || '',
      regionName: resolved.regionName || '',
      regionProvince: resolved.province || '',
      regionCity: resolved.city || '',
      regionDistrict: resolved.district || '',
      regionSource: resolved.source || (pos.source || 'gps'),
      userLocationAddress: [resolved.province, resolved.city, resolved.district].filter(Boolean).join('') || savedAddress,
      userLocationLongitude: resolved.longitude == null ? Number(pos.longitude) : resolved.longitude,
      userLocationLatitude: resolved.latitude == null ? Number(pos.latitude) : resolved.latitude
    })
    return resolved
  },

  async updateMyLocation() {
    if (!ensureLogin('更新位置需要先登录，是否前往登录？')) return
    if (this.data.locationUpdating) return
    this.setData({ locationUpdating: true })
    try {
      const pos = await this.resolvePositionWithFallback()
      const savedAddress = await this.saveUserLocation(pos)
      let resolved = null
      let regionSyncWarning = ''
      try {
        resolved = await this.syncRegionFromPosition(pos, savedAddress)
      } catch (regionErr) {
        regionSyncWarning = toFallbackMessage(regionErr && regionErr.message ? regionErr.message : 'region_sync_failed')
      }
      await this.load()
      if (regionSyncWarning) {
        wx.showToast({ title: regionSyncWarning, icon: 'none' })
        return
      }
      if (resolved && resolved.source !== 'gps') {
        wx.showToast({ title: toFallbackMessage(resolved.fallbackReason), icon: 'none' })
        return
      }
      wx.showToast({ title: '位置已更新', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: toLocationErrorMessage(e), icon: 'none' })
    } finally {
      this.setData({ locationUpdating: false })
    }
  },

  async viewMyLocation() {
    const user = this.data.user || {}
    if (!user.userLocationLongitude || !user.userLocationLatitude) {
      wx.showToast({ title: '暂无已保存位置', icon: 'none' })
      return
    }
    try {
      await openLocation({
        longitude: Number(user.userLocationLongitude),
        latitude: Number(user.userLocationLatitude),
        address: user.userLocationAddress || '',
        name: '我的位置'
      })
    } catch (e) {
      wx.showToast({ title: '打开地图失败', icon: 'none' })
    }
  },

  onUserPortraitError() {
    this.setData({ 'user.userPortrait': '' })
  },

  onTeacherPhotoError() {
    this.setData({ 'teacher.teacherPhoto': '' })
  },

  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || '' })
  },

  onAuthSheetClose() {
    this.setData({ authSheetVisible: false, authSheetRole: '' })
  },

  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false, authSheetRole: '', guestMode: false, roleMode: getRoleMode() })
    this.load()
  }
})
