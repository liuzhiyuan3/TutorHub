const { request } = require('../../utils/request')
const { ensureRole, getRoleMode, getLoginState } = require('../../utils/auth-guard')
const { normalizeMediaUrl, pickTeacherImage } = require('../../utils/media-url')

Page({
  data: {
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    list: [],
    filters: { regions: [], regionTree: [] },
    regionIndex: 0,
    activeDistrictId: '',
    districtChips: [{ id: '', name: '全部' }],
    provinceOptions: [{ name: '全部省份' }],
    cityOptions: [{ name: '全部城市' }],
    provinceIndex: 0,
    cityIndex: 0,
    selectedProvinceName: '',
    selectedCityName: '',
    selectedRegionLabel: '区域',
    selectedSubjectId: '',
    selectedSubjectName: '科目',
    showSubjectPanel: false,
    subjectCategoryTree: [],
    activeCategoryIndex: 0,
    currentCategorySubjects: [],
    budgetRangeIndex: 0,
    showRegionSheet: false,
    showBudgetSheet: false,
    budgetActions: [],
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
    authSheetRole: '',
    teacherStatusMap: ['待审核', '已通过', '已拒绝']
  },

  onLoad() {
    const roleMode = getRoleMode()
    this.setData({ roleMode })
    this.loadFilters().then(() => this.load(true))
  },

  onShow() {
    const roleMode = getRoleMode()
    this.setData({ roleMode })
    if (!this.data.list.length && !this.data.loading) {
      this.load(true)
    }
  },

  onPullDownRefresh() {
    this.load(true).finally(() => wx.stopPullDownRefresh())
  },

  async loadFilters() {
    try {
      const pair = await Promise.allSettled([
        request({ url: '/api/home/filters', authMode: 'optional' }),
        request({ url: '/api/content/subject-categories', authMode: 'optional' })
      ])
      const filterMeta = pair && pair[0] && pair[0].status === 'fulfilled' ? pair[0].value : {}
      const categoryTreeRaw = pair && pair[1] && pair[1].status === 'fulfilled' ? pair[1].value : []
      const categoryTree = Array.isArray(categoryTreeRaw) ? categoryTreeRaw : []
      const regionSource = Array.isArray(filterMeta && filterMeta.regions) ? filterMeta.regions : []
      const regionTree = this.normalizeRegionTree(Array.isArray(filterMeta && filterMeta.regionTree) ? filterMeta.regionTree : [])
      const tree = [{
        id: '',
        categoryName: '全部',
        categoryCode: 'ALL',
        categorySort: 0,
        subjects: []
      }].concat(categoryTree || [])
      const regions = [{ id: '', name: '全部区域' }].concat(regionSource)
      const cascadeState = this.buildRegionCascadeState(regionTree, '')
      const regionIndex = regions.findIndex((item) => item.id === cascadeState.activeDistrictId)
      const budgetActions = (this.data.budgetRangeOptions || []).map((item, index) => ({
        name: item.label || '未命名预算',
        index
      }))
      this.setData({
        'filters.regions': regions,
        'filters.regionTree': regionTree,
        regionIndex: regionIndex >= 0 ? regionIndex : 0,
        activeDistrictId: cascadeState.activeDistrictId,
        districtChips: cascadeState.districtChips,
        provinceOptions: cascadeState.provinceOptions,
        cityOptions: cascadeState.cityOptions,
        provinceIndex: cascadeState.provinceIndex,
        cityIndex: cascadeState.cityIndex,
        selectedProvinceName: cascadeState.selectedProvinceName,
        selectedCityName: cascadeState.selectedCityName,
        selectedRegionLabel: cascadeState.selectedRegionLabel,
        budgetActions,
        subjectCategoryTree: tree,
        currentCategorySubjects: (tree[0] && tree[0].subjects) || []
      })
    } catch (e) {
      const fallbackRegions = [{ id: '', name: '全部区域' }]
      const fallbackBudgetActions = (this.data.budgetRangeOptions || []).map((item, index) => ({
        name: item.label || '未命名预算',
        index
      }))
      this.setData({
        error: e.message || '筛选项加载失败',
        'filters.regions': fallbackRegions,
        'filters.regionTree': [],
        regionIndex: 0,
        activeDistrictId: '',
        districtChips: [{ id: '', name: '全部' }],
        provinceOptions: [{ name: '全部省份' }],
        cityOptions: [{ name: '全部城市' }],
        provinceIndex: 0,
        cityIndex: 0,
        selectedProvinceName: '',
        selectedCityName: '',
        selectedRegionLabel: '区域',
        budgetActions: fallbackBudgetActions,
        subjectCategoryTree: [{
          id: '',
          categoryName: '全部',
          categoryCode: 'ALL',
          categorySort: 0,
          subjects: []
        }],
        currentCategorySubjects: []
      })
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
    if (this.data.roleMode === 'teacher') {
      return this.loadRequirements(reset)
    }
    return this.loadTeachers(reset)
  },

  async loadRequirements(reset = false) {
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

  async loadTeachers(reset = false) {
    const nextPage = reset ? 1 : this.data.pageNo
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return
    this.setData({ loading: true, error: '' })
    try {
      const data = await request({ url: `/api/home/teachers/search?${this.buildTeacherQueryParams(nextPage)}`, authMode: 'optional' })
      const records = ((data && data.records) || []).map((item) => ({
        teacherPhoto: normalizeMediaUrl(pickTeacherImage(item)).url,
        id: item.teacherId || item.id,
        cardType: 'teacher',
        teacherName: item.userName || item.teacherName || '教员',
        teacherIdentity: item.teacherIdentity || '教员',
        teacherSchool: item.teacherSchool || '-',
        teacherMajor: item.teacherMajor || '-',
        teacherTeachingYears: Number(item.teacherTeachingYears || 0),
        teacherAuditStatus: Number(item.teacherAuditStatus || 0),
        subjectText: Array.isArray(item.subjectNames) && item.subjectNames.length ? item.subjectNames.join('、') : '-',
        regionText: Array.isArray(item.regionNames) && item.regionNames.length ? item.regionNames.join('、') : '-',
        hireCount: Number(item.hireCount || 0),
        historyDealCount: Number(item.historyDealCount || 0)
      }))
      const merged = reset ? records : this.data.list.concat(records)
      const hasMore = merged.length < Number(data && data.total ? data.total : 0)
      this.setData({
        list: merged,
        loading: false,
        hasMore,
        pageNo: nextPage + 1
      })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '教员加载失败' })
    }
  },

  buildTeacherQueryParams(pageNo) {
    const params = [`pageNo=${pageNo}`, `pageSize=${this.data.pageSize}`, 'auditStatus=1']
    const subjectId = this.data.selectedSubjectId
    const region = this.data.filters.regions[this.data.regionIndex]
    const advanced = this.data.advancedFilters
    const sortBy = advanced.sortBy === 'latest' ? 'latest' : 'hot'
    if (subjectId) params.push(`subjectId=${encodeURIComponent(subjectId)}`)
    if (region && region.id) params.push(`regionId=${encodeURIComponent(region.id)}`)
    if (advanced.keyword) params.push(`keyword=${encodeURIComponent(advanced.keyword)}`)
    params.push(`sortBy=${encodeURIComponent(sortBy)}`)
    return params.join('&')
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

  parseSelectIndex(detail, fallbackIndex, optionList) {
    if (detail && detail.item && Number.isFinite(Number(detail.item.index))) {
      return Number(detail.item.index)
    }
    if (Number.isFinite(Number(detail && detail.value))) {
      return Number(detail.value)
    }
    if (Number.isFinite(Number(detail && detail.index))) {
      return Number(detail.index)
    }
    const selectedName = (detail && detail.item && detail.item.name) || (detail && detail.name)
    if (selectedName && Array.isArray(optionList) && optionList.length) {
      const hitIndex = optionList.findIndex((item) => String(item && item.name) === String(selectedName))
      if (hitIndex >= 0) return hitIndex
    }
    return Number.isFinite(Number(fallbackIndex)) ? Number(fallbackIndex) : 0
  },

  openRegionSheet() {
    this.setData({ showRegionSheet: true })
  },
  closeRegionSheet() {
    this.setData({ showRegionSheet: false })
  },
  onProvinceChange(e) {
    const index = Number((e && e.detail && e.detail.value) || 0)
    const provinceOptions = this.data.provinceOptions || [{ name: '全部省份' }]
    const provinceName = (provinceOptions[index] && provinceOptions[index].name) || ''
    const nextState = this.buildRegionCascadeState(
      this.data.filters.regionTree || [],
      '',
      provinceName,
      ''
    )
    this.setData({
      provinceIndex: nextState.provinceIndex,
      cityIndex: nextState.cityIndex,
      selectedProvinceName: nextState.selectedProvinceName,
      selectedCityName: nextState.selectedCityName,
      cityOptions: nextState.cityOptions,
      districtChips: nextState.districtChips,
      activeDistrictId: '',
      selectedRegionLabel: nextState.selectedRegionLabel,
      regionIndex: 0
    }, () => this.load(true))
  },
  onCityChange(e) {
    const index = Number((e && e.detail && e.detail.value) || 0)
    const cityOptions = this.data.cityOptions || [{ name: '全部城市' }]
    const cityName = (cityOptions[index] && cityOptions[index].name) || ''
    const nextState = this.buildRegionCascadeState(
      this.data.filters.regionTree || [],
      '',
      this.data.selectedProvinceName,
      cityName
    )
    this.setData({
      cityIndex: nextState.cityIndex,
      selectedCityName: nextState.selectedCityName,
      districtChips: nextState.districtChips,
      activeDistrictId: '',
      selectedRegionLabel: nextState.selectedRegionLabel,
      regionIndex: 0
    }, () => this.load(true))
  },
  chooseDistrict(e) {
    const id = (e && e.currentTarget && e.currentTarget.dataset && e.currentTarget.dataset.id) || ''
    const regions = this.data.filters.regions || []
    const regionIndex = regions.findIndex((item) => String(item.id || '') === String(id || ''))
    const nextState = this.buildRegionCascadeState(
      this.data.filters.regionTree || [],
      id,
      this.data.selectedProvinceName,
      this.data.selectedCityName
    )
    this.setData({
      activeDistrictId: id,
      regionIndex: regionIndex >= 0 ? regionIndex : 0,
      selectedRegionLabel: nextState.selectedRegionLabel,
      showRegionSheet: false
    }, () => this.load(true))
  },

  openBudgetSheet() {
    this.setData({ showBudgetSheet: true })
  },
  closeBudgetSheet() {
    this.setData({ showBudgetSheet: false })
  },
  onBudgetSelect(e) {
    const index = this.parseSelectIndex(e && e.detail, this.data.budgetRangeIndex, this.data.budgetActions)
    const next = (this.data.budgetRangeOptions || [])[index] ? index : 0
    this.setData({ showBudgetSheet: false, budgetRangeIndex: next }, () => this.load(true))
  },
  toggleAdvancedPanel() { this.setData({ showAdvanced: !this.data.showAdvanced }) },
  closeAdvancedPanel() { this.setData({ showAdvanced: false }) },

  onKeywordInput(e) {
    const value = (e && e.detail && (e.detail.value !== undefined ? e.detail.value : e.detail)) || ''
    this.setData({ 'advancedFilters.keyword': value })
  },

  onUrgencyChange(e) {
    const index = this.parseSelectIndex(e && e.detail, this.data.urgencyIndex, this.data.urgencyOptions)
    const item = this.data.urgencyOptions[index] || this.data.urgencyOptions[0]
    this.setData({ urgencyIndex: index, 'advancedFilters.urgency': item.value })
  },

  onTutoringMethodChange(e) {
    const index = this.parseSelectIndex(e && e.detail, this.data.tutoringMethodIndex, this.data.tutoringMethodOptions)
    const item = this.data.tutoringMethodOptions[index] || this.data.tutoringMethodOptions[0]
    this.setData({ tutoringMethodIndex: index, 'advancedFilters.tutoringMethod': item.value })
  },

  onSortChange(e) {
    const index = this.parseSelectIndex(e && e.detail, this.data.sortIndex, this.data.sortOptions)
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
    const resetRegion = this.buildRegionCascadeState(this.data.filters.regionTree || [], '')
    this.setData({
      regionIndex: 0,
      activeDistrictId: '',
      districtChips: resetRegion.districtChips,
      provinceOptions: resetRegion.provinceOptions,
      cityOptions: resetRegion.cityOptions,
      provinceIndex: resetRegion.provinceIndex,
      cityIndex: resetRegion.cityIndex,
      selectedProvinceName: resetRegion.selectedProvinceName,
      selectedCityName: resetRegion.selectedCityName,
      selectedRegionLabel: resetRegion.selectedRegionLabel,
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
  goTeacherDetail(e) {
    const id = e && e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset.id : ''
    if (!id) return
    wx.navigateTo({ url: `/pages/teacher-detail/index?id=${id}` })
  },

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
  },

  normalizeRegionTree(rawTree) {
    if (!Array.isArray(rawTree)) return []
    return rawTree.map((province) => ({
      name: province && province.name ? province.name : '未分省',
      cities: Array.isArray(province && province.cities) ? province.cities.map((city) => ({
        name: city && city.name ? city.name : '未分市',
        districts: Array.isArray(city && city.districts) ? city.districts.map((district) => ({
          id: district && district.id ? district.id : '',
          code: district && district.code ? district.code : '',
          name: district && district.name ? district.name : '未命名区域'
        })) : []
      })) : []
    }))
  },

  buildRegionCascadeState(regionTree, activeDistrictId, selectedProvinceName, selectedCityName) {
    const tree = Array.isArray(regionTree) && regionTree.length ? regionTree : []
    if (!tree.length) {
      return {
        provinceOptions: [{ name: '全部省份' }],
        cityOptions: [{ name: '全部城市' }],
        provinceIndex: 0,
        cityIndex: 0,
        selectedProvinceName: '',
        selectedCityName: '',
        districtChips: [{ id: '', name: '全部' }],
        activeDistrictId: '',
        selectedRegionLabel: '区域'
      }
    }
    const provinceOptions = [{ name: '全部省份' }].concat(tree.map((item) => ({ name: item.name })))
    const provinceIndex = selectedProvinceName
      ? Math.max(0, provinceOptions.findIndex((item) => item.name === selectedProvinceName))
      : 0
    const provinceNode = provinceIndex > 0 ? tree[provinceIndex - 1] : null
    const cityOptions = [{ name: '全部城市' }].concat((provinceNode && provinceNode.cities ? provinceNode.cities : []).map((item) => ({ name: item.name })))
    const cityIndex = selectedCityName
      ? Math.max(0, cityOptions.findIndex((item) => item.name === selectedCityName))
      : 0
    const cityNode = (provinceNode && cityIndex > 0) ? provinceNode.cities[cityIndex - 1] : null
    let districts = []
    if (cityNode && cityNode.districts) {
      districts = cityNode.districts.map((item) => ({ id: item.id, name: item.name }))
    } else if (provinceNode && provinceNode.cities) {
      provinceNode.cities.forEach((city) => {
        ;(city.districts || []).forEach((item) => districts.push({ id: item.id, name: item.name }))
      })
    } else {
      tree.forEach((province) => {
        ;(province.cities || []).forEach((city) => {
          ;(city.districts || []).forEach((item) => districts.push({ id: item.id, name: item.name }))
        })
      })
    }
    const validIds = new Set(districts.map((item) => item.id))
    const safeActiveId = activeDistrictId && validIds.has(activeDistrictId) ? activeDistrictId : ''
    const selectedDistrict = districts.find((item) => item.id === safeActiveId)
    return {
      provinceOptions,
      cityOptions,
      provinceIndex,
      cityIndex,
      selectedProvinceName: provinceIndex > 0 ? provinceOptions[provinceIndex].name : '',
      selectedCityName: cityIndex > 0 ? cityOptions[cityIndex].name : '',
      districtChips: [{ id: '', name: '全部' }].concat(districts),
      activeDistrictId: safeActiveId,
      selectedRegionLabel: selectedDistrict ? selectedDistrict.name : '区域'
    }
  }
})
