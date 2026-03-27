const { request } = require('../../utils/request')
const { ensureRole, getRoleMode, getLoginState } = require('../../utils/auth-guard')

Page({
  data: {
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    list: [],
    filters: { regions: [] },
    regionIndex: 0,
    selectedSubjectId: '',
    selectedSubjectName: '科目',
    showSubjectPanel: false,
    subjectCategoryTree: [],
    activeCategoryIndex: 0,
    currentCategorySubjects: [],
    budgetRangeIndex: 0,
    showAdvanced: false,
    advancedFilters: {
      keyword: '',
      urgency: '',
      tutoringMethod: '',
      sortBy: 'latest'
    },
    sortOptions: [
      { label: '最新发布', value: 'latest' },
      { label: '薪资从低到高', value: 'salaryAsc' },
      { label: '薪资从高到低', value: 'salaryDesc' }
    ],
    sortIndex: 0,
    budgetRangeOptions: [
      { label: '预算不限', min: '', max: '' },
      { label: '100-150元/小时', min: 100, max: 150 },
      { label: '150-200元/小时', min: 150, max: 200 },
      { label: '200-300元/小时', min: 200, max: 300 },
      { label: '300元以上', min: 300, max: '' }
    ],
    urgencyIndex: 0,
    urgencyOptions: [
      { label: '紧急程度不限', value: '' },
      { label: '普通', value: '0' },
      { label: '加急', value: '1' },
      { label: '紧急', value: '2' }
    ],
    tutoringMethodOptions: [
      { label: '授课方式不限', value: '' },
      { label: '线上', value: '0' },
      { label: '线下', value: '1' },
      { label: '线上线下', value: '2' }
    ],
    tutoringMethodIndex: 0,
    loading: false,
    error: '',
    receivingId: '',
    roleMode: 'guest',
    statusMap: ['待接单', '已接单', '已完成', '已取消'],
    authSheetVisible: false,
    authSheetRole: ''
  },

  onLoad() {
    this.setData({ roleMode: getRoleMode() })
    this.loadFilters().then(() => this.load(true))
  },

  onShow() {
    this.setData({ roleMode: getRoleMode() })
    if (!this.data.list.length && !this.data.loading) {
      this.load(true)
    }
  },

  onPullDownRefresh() {
    this.load(true).finally(() => wx.stopPullDownRefresh())
  },

  async loadFilters() {
    try {
      const [filterMeta, categoryTree] = await Promise.all([
        request({ url: '/api/home/filters', authMode: 'optional' }),
        request({ url: '/api/content/subject-categories', authMode: 'optional' })
      ])
      const tree = [{
        id: '',
        categoryName: '全部',
        categoryCode: 'ALL',
        categorySort: 0,
        subjects: []
      }].concat(categoryTree || [])
      const regions = [{ id: '', name: '全部区域' }].concat((filterMeta && filterMeta.regions) || [])
      this.setData({
        'filters.regions': regions,
        subjectCategoryTree: tree,
        currentCategorySubjects: (tree[0] && tree[0].subjects) || []
      })
    } catch (e) {
      this.setData({ error: e.message || '筛选项加载失败' })
    }
  },

  buildReceiveActionText(item) {
    const status = item && item.requirementStatus
    if (status !== 0) return '不可接单'
    const visibility = String((item && item.teacherProfileVisibility) || 'HIDDEN').toUpperCase()
    if (visibility === 'VISIBLE') return '我要接单'
    if (visibility === 'WAITING' || visibility === 'PENDING' || visibility === 'IN_REVIEW' || visibility === 'AUDITING') {
      return '资料审核中'
    }
    if (visibility === 'REJECTED' || visibility === 'DENIED') {
      return '资料被驳回，去完善'
    }
    return '完善资料后可接单'
  },

  async load(reset = false) {
    const nextPage = reset ? 1 : this.data.pageNo
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return
    this.setData({ loading: true, error: '' })
    try {
      const data = await request({ url: `/api/home/requirements/search?${this.buildQueryParams(nextPage)}`, authMode: 'optional' })
      const records = ((data && data.records) || []).map((item) => ({
        ...item,
        requirementStatus: item.requirementStatus === undefined ? null : Number(item.requirementStatus),
        teacherProfileVisibility: item.teacherProfileVisibility || 'HIDDEN',
        receiveActionText: this.buildReceiveActionText(item),
        expectedTimeSlotsText: Array.isArray(item.requirementExpectedTimeSlots)
          ? item.requirementExpectedTimeSlots.join(' / ')
          : ''
      }))
      const merged = reset ? records : this.data.list.concat(records)
      const hasMore = merged.length < Number(data.total || 0)
      this.setData({
        list: merged,
        loading: false,
        hasMore,
        pageNo: nextPage + 1
      })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '需求加载失败' })
    }
  },

  buildQueryParams(pageNo) {
    const params = [`pageNo=${pageNo}`, `pageSize=${this.data.pageSize}`]
    const subjectId = this.data.selectedSubjectId
    const region = this.data.filters.regions[this.data.regionIndex]
    const budget = this.data.budgetRangeOptions[this.data.budgetRangeIndex] || this.data.budgetRangeOptions[0]
    const advanced = this.data.advancedFilters

    if (subjectId) params.push(`subjectId=${encodeURIComponent(subjectId)}`)
    if (region && region.id) params.push(`regionId=${encodeURIComponent(region.id)}`)
    if (budget && budget.min !== '') params.push(`budgetMin=${encodeURIComponent(String(budget.min))}`)
    if (budget && budget.max !== '') params.push(`budgetMax=${encodeURIComponent(String(budget.max))}`)
    if (advanced.keyword) params.push(`keyword=${encodeURIComponent(advanced.keyword)}`)
    if (advanced.urgency !== '') params.push(`urgency=${encodeURIComponent(advanced.urgency)}`)
    if (advanced.tutoringMethod !== '') params.push(`tutoringMethod=${encodeURIComponent(advanced.tutoringMethod)}`)
    params.push(`sortBy=${encodeURIComponent(advanced.sortBy || 'latest')}`)
    return params.join('&')
  },

  openSubjectPanel() { this.setData({ showSubjectPanel: true }) },
  closeSubjectPanel() { this.setData({ showSubjectPanel: false }) },

  chooseCategory(e) {
    const index = Number(e.currentTarget.dataset.index)
    const nextIndex = Number.isNaN(index) ? 0 : index
    const current = this.data.subjectCategoryTree[nextIndex] || {}
    this.setData({
      activeCategoryIndex: nextIndex,
      currentCategorySubjects: current.subjects || []
    })
  },

  chooseSubject(e) {
    const id = e.currentTarget.dataset.id || ''
    const name = e.currentTarget.dataset.name || '科目'
    this.setData({
      selectedSubjectId: id,
      selectedSubjectName: name || '科目',
      showSubjectPanel: false
    }, () => this.load(true))
  },

  resetSubjectFilter() {
    this.setData({
      selectedSubjectId: '',
      selectedSubjectName: '科目',
      showSubjectPanel: false,
      activeCategoryIndex: 0
    }, () => this.load(true))
  },

  onRegionChange(e) { this.setData({ regionIndex: Number(e.detail.value) }, () => this.load(true)) },
  onBudgetRangeChange(e) { this.setData({ budgetRangeIndex: Number(e.detail.value) }, () => this.load(true)) },
  toggleAdvancedPanel() { this.setData({ showAdvanced: !this.data.showAdvanced }) },
  closeAdvancedPanel() { this.setData({ showAdvanced: false }) },

  onKeywordInput(e) {
    const value = (e && e.detail && (e.detail.value !== undefined ? e.detail.value : e.detail)) || ''
    this.setData({ 'advancedFilters.keyword': value })
  },

  onUrgencyChange(e) {
    const index = Number(e.detail.value)
    const item = this.data.urgencyOptions[index] || this.data.urgencyOptions[0]
    this.setData({ urgencyIndex: index, 'advancedFilters.urgency': item.value })
  },

  onTutoringMethodChange(e) {
    const index = Number(e.detail.value)
    const item = this.data.tutoringMethodOptions[index] || this.data.tutoringMethodOptions[0]
    this.setData({ tutoringMethodIndex: index, 'advancedFilters.tutoringMethod': item.value })
  },

  onSortChange(e) {
    const index = Number(e.detail.value)
    const item = this.data.sortOptions[index] || this.data.sortOptions[0]
    this.setData({ sortIndex: index, 'advancedFilters.sortBy': item.value })
  },

  applyAdvancedFilter() { this.setData({ showAdvanced: false }, () => this.load(true)) },

  onResetAdvanced() {
    this.setData({
      sortIndex: 0,
      urgencyIndex: 0,
      tutoringMethodIndex: 0,
      advancedFilters: { keyword: '', urgency: '', tutoringMethod: '', sortBy: 'latest' }
    })
  },

  resetAllFilters() {
    this.setData({
      regionIndex: 0,
      budgetRangeIndex: 0,
      selectedSubjectId: '',
      selectedSubjectName: '科目',
      activeCategoryIndex: 0
    }, () => {
      this.onResetAdvanced()
      this.load(true)
    })
  },

  onLoadMore() { this.load(false) },
  noop() {},

  ensureTeacherForReceive(payload) {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingReceivePayload = payload
    if (!ensureRole('teacher', '接单仅教员可操作，是否前往登录？')) {
      if (state.loggedIn) this._pendingReceivePayload = null
      return false
    }
    this._pendingReceivePayload = null
    return true
  },

  receive(e) {
    const status = e.currentTarget.dataset.status
    const id = e.currentTarget.dataset.id
    const title = e.currentTarget.dataset.title
    const visibility = e.currentTarget.dataset.visibility
    if (!this.ensureTeacherForReceive({ status, id, title, visibility })) return
    this.receiveByPayload({ status, id, title, visibility })
  },

  receiveByPayload(payload) {
    const status = payload && payload.status
    const id = payload && payload.id
    const title = payload && payload.title
    const visibility = payload && payload.visibility

    if (status === null || status === undefined || Number.isNaN(Number(status))) {
      wx.showToast({ title: '状态异常，请刷新后重试', icon: 'none' })
      return
    }
    if (visibility && visibility !== 'VISIBLE') {
      const upperVisibility = String(visibility).toUpperCase()
      if (upperVisibility === 'WAITING' || upperVisibility === 'PENDING' || upperVisibility === 'IN_REVIEW' || upperVisibility === 'AUDITING') {
        wx.showToast({ title: '资料审核中，审核通过后可接单', icon: 'none' })
        return
      }
      if (upperVisibility === 'REJECTED' || upperVisibility === 'DENIED') {
        wx.showModal({
          title: '资料未通过审核',
          content: '请先完善教员资料后重新提交审核。',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({ url: '/pages/teacher-profile-edit/index' })
            }
          }
        })
        return
      }
      wx.showModal({
        title: '请先完善资料',
        content: '完善并提交教员资料后才可接单。',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/teacher-profile-edit/index' })
          }
        }
      })
      return
    }
    if (Number(status) !== 0) {
      wx.showToast({ title: '当前状态不可接单', icon: 'none' })
      return
    }
    if (this.data.receivingId) return

    wx.showModal({
      title: '确认接单',
      content: `确认接单「${title || '该需求'}」吗？`,
      success: async (res) => {
        if (!res.confirm) return
        await this.doReceive(id)
      }
    })
  },

  async doReceive(id) {
    this.setData({ receivingId: id })
    try {
      await request({ url: `/api/order/accept/${id}`, method: 'POST', authMode: 'required' })
      wx.showToast({ title: '接单成功', icon: 'success' })
      await this.load(true)
    } catch (err) {
      const code = Number(err && err.code)
      if (code === 401 || code === 403) {
        wx.showToast({ title: '身份无权限接单', icon: 'none' })
      } else if (code === 409) {
        wx.showToast({ title: '该需求已被其他教员承接', icon: 'none' })
      } else if (code === 422) {
        wx.showToast({ title: err.message || '请先完善资料并通过审核', icon: 'none' })
      } else {
        wx.showToast({ title: err.message || '接单失败，请稍后重试', icon: 'none' })
      }
    } finally {
      this.setData({ receivingId: '' })
    }
  },

  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || '' })
  },

  onAuthSheetClose() {
    this._pendingReceivePayload = null
    this.setData({ authSheetVisible: false, authSheetRole: '' })
  },

  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false, authSheetRole: '', roleMode: getRoleMode() })
    this.load(true)
    if (this._pendingReceivePayload) {
      const payload = this._pendingReceivePayload
      this._pendingReceivePayload = null
      this.receiveByPayload(payload)
    }
  }
})
