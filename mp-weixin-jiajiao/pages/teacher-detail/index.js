const { request } = require('../../utils/request')
const { ensureRole, getLoginState } = require('../../utils/auth-guard')
const { normalizeMediaUrl, logMediaDebug, pickTeacherImage } = require('../../utils/media-url')

Page({
  data: {
    teacher: null,
    id: '',
    loading: false,
    error: '',
    teacherPhotoError: false,
    methodText: '-',
    subjectText: '-',
    regionText: '-',
    successRecords: [],
    favoriteLoading: false,
    reserveLoading: false,
    favoriteAdded: false,
    reservePanelVisible: false,
    authSheetVisible: false,
    authSheetRole: '',
    reserveForm: {
      appointmentSubject: '',
      appointmentGrade: '',
      appointmentAddress: '',
      appointmentDate: '',
      appointmentTimeText: '19:00',
      appointmentRemark: ''
    }
  },
  onLoad(options) {
    this.setData({ id: options.id || '' })
  },
  onShow() {
    this.load()
  },
  async load() {
    this.setData({ loading: true, error: '' })
    try {
      let detail = null
      try {
        detail = await request({ url: `/api/teacher/public/${this.data.id}`, authMode: 'optional' })
      } catch (err) {
        const pageData = await request({ url: '/api/teacher/page?pageNo=1&pageSize=100&auditStatus=1', authMode: 'optional' })
        detail = (pageData.records || []).find((x) => String(x.id || x.teacherId) === String(this.data.id)) || null
      }
      const normalizedDetail = detail ? {
        ...detail,
        teacherPhotoRaw: pickTeacherImage(detail),
        historyDealCount: Number(detail.historyDealCount || 0),
        hireCount: Number(detail.hireCount || 0)
      } : null
      if (normalizedDetail) {
        const media = normalizeMediaUrl(normalizedDetail.teacherPhotoRaw)
        normalizedDetail.teacherPhoto = media.url
        normalizedDetail.teacherPhotoWarn = media.warn
        normalizedDetail.teacherPhotoReason = media.reason
      }
      this.setData({
        teacher: normalizedDetail,
        teacherPhotoError: false,
        methodText: this.getMethodText(normalizedDetail ? normalizedDetail.teacherTutoringMethod : null),
        subjectText: (normalizedDetail && normalizedDetail.subjectNames && normalizedDetail.subjectNames.length) ? normalizedDetail.subjectNames.join('、') : '-',
        regionText: (normalizedDetail && normalizedDetail.regionNames && normalizedDetail.regionNames.length) ? normalizedDetail.regionNames.join('、') : '-',
        successRecords: (normalizedDetail && normalizedDetail.successRecords) || [],
        loading: false
      })
      this.syncFavoriteStatus()
    } catch (e) {
      this.setData({
        loading: false,
        teacher: null,
        teacherPhotoError: false,
        error: e.message || '教员详情加载失败'
      })
    }
  },
  onTeacherPhotoError() {
    if (this.data.teacher) {
      logMediaDebug('teacher-detail-avatar-error', {
        teacherId: this.data.id,
        raw: this.data.teacher.teacherPhotoRaw,
        normalized: this.data.teacher.teacherPhoto,
        reason: this.data.teacher.teacherPhotoReason
      })
      if (this.data.teacher.teacherPhotoWarn) {
        wx.showToast({ title: '该图片链接为http，真机可能被拦截', icon: 'none' })
      }
    }
    this.setData({ teacherPhotoError: true })
  },
  async syncFavoriteStatus() {
    const loginState = getLoginState()
    if (!loginState.loggedIn || !this.data.id) return
    try {
      const data = await request({ url: '/api/favorite/my/page?pageNo=1&pageSize=200', authMode: 'required' })
      const list = (data && data.records) || []
      const favoriteAdded = list.some((item) => String(item.teacherId) === String(this.data.id))
      this.setData({ favoriteAdded })
    } catch (e) {
      // 收藏状态读取失败不阻断详情渲染
    }
  },
  getMethodText(method) {
    const map = ['线上', '线下', '线上线下']
    return map[Number(method)] || '-'
  },
  gateParentAction(message, action) {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingAction = action
    if (!ensureRole('parent', message)) {
      if (state.loggedIn) this._pendingAction = ''
      return false
    }
    this._pendingAction = ''
    return true
  },
  openReservePanelAction() {
    const form = this.data.reserveForm
    const today = new Date()
    const mm = String(today.getMonth() + 1).padStart(2, '0')
    const dd = String(today.getDate()).padStart(2, '0')
    const date = `${today.getFullYear()}-${mm}-${dd}`
    this.setData({
      reservePanelVisible: true,
      reserveForm: {
        ...form,
        appointmentSubject: form.appointmentSubject || ((this.data.teacher && this.data.teacher.subjectNames && this.data.teacher.subjectNames[0]) || ''),
        appointmentGrade: form.appointmentGrade || '小学四年级',
        appointmentAddress: form.appointmentAddress || '请填写详细地址',
        appointmentDate: form.appointmentDate || date
      }
    })
  },
  onReserve() {
    if (!this.gateParentAction('预约教员仅家长可操作，是否前往登录？', 'openReservePanelAction')) return
    this.openReservePanelAction()
  },
  closeReservePanel() {
    this.setData({ reservePanelVisible: false })
  },
  onReserveInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`reserveForm.${key}`]: e.detail.value || '' })
  },
  onReserveDateChange(e) {
    this.setData({ 'reserveForm.appointmentDate': e.detail.value })
  },
  onReserveTimeChange(e) {
    this.setData({ 'reserveForm.appointmentTimeText': e.detail.value })
  },
  async submitReserve() {
    if (this.data.reserveLoading) return
    const form = this.data.reserveForm
    if (!String(form.appointmentSubject || '').trim()) {
      wx.showToast({ title: '请填写预约科目', icon: 'none' })
      return
    }
    if (!String(form.appointmentGrade || '').trim()) {
      wx.showToast({ title: '请填写预约年级', icon: 'none' })
      return
    }
    if (!String(form.appointmentAddress || '').trim()) {
      wx.showToast({ title: '请填写预约地址', icon: 'none' })
      return
    }
    if (!form.appointmentDate) {
      wx.showToast({ title: '请选择预约日期', icon: 'none' })
      return
    }
    const appointmentTime = `${form.appointmentDate}T${form.appointmentTimeText || '19:00'}:00`
    this.setData({ reserveLoading: true })
    try {
      await request({
        url: '/api/appointment',
        method: 'POST',
        authMode: 'required',
        data: {
          teacherId: this.data.id,
          appointmentSubject: form.appointmentSubject,
          appointmentGrade: form.appointmentGrade,
          appointmentAddress: form.appointmentAddress,
          appointmentRemark: form.appointmentRemark || '',
          appointmentTime
        }
      })
      wx.showToast({ title: '预约成功', icon: 'success' })
      this.setData({ reservePanelVisible: false })
    } catch (e) {
      wx.showToast({ title: e.message || '预约失败', icon: 'none' })
    } finally {
      this.setData({ reserveLoading: false })
    }
  },
  async onFavorite() {
    if (!this.gateParentAction('加入备选仅家长可操作，是否前往登录？', 'onFavorite')) return
    if (this.data.favoriteLoading) return
    this.setData({ favoriteLoading: true })
    try {
      if (this.data.favoriteAdded) {
        await request({ url: `/api/favorite/${this.data.id}`, method: 'DELETE', authMode: 'required' })
        wx.showToast({ title: '已取消备选', icon: 'success' })
        this.setData({ favoriteAdded: false })
      } else {
        await request({ url: `/api/favorite/${this.data.id}`, method: 'POST', authMode: 'required' })
        wx.showToast({ title: '已加入备选', icon: 'success' })
        this.setData({ favoriteAdded: true })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ favoriteLoading: false })
    }
  },
  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || '' })
  },
  onAuthSheetClose() {
    this._pendingAction = ''
    this.setData({ authSheetVisible: false, authSheetRole: '' })
  },
  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false, authSheetRole: '' })
    this.syncFavoriteStatus()
    const action = this._pendingAction
    this._pendingAction = ''
    if (action && typeof this[action] === 'function') {
      this[action]()
    }
  }
})
