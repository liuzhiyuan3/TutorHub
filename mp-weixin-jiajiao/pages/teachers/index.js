const { request } = require('../../utils/request')
const { getCurrentLocationWithAuth } = require('../../utils/location')
const globalStore = require('../../utils/global-store')
const { resolveRegionWithFallback } = require('../../utils/location-service')
const { normalizeMediaUrl, logMediaDebug, pickTeacherImage } = require('../../utils/media-url')

Page({
  data: {
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    list: [],
    filters: {
      regions: []
    },
    query: {
      subjectId: '',
      regionId: '',
      schoolKeyword: '',
      keyword: '',
      sortBy: 'hot'
    },
    nearbyOnly: false,
    nearbyKm: 5,
    currentDistrictText: '未定位',
    districtChips: [{ id: '', name: '全部' }],
    activeDistrictId: '',
    selectedSubjectName: '科目',
    showSubjectPanel: false,
    subjectCategoryTree: [],
    activeCategoryIndex: 0,
    currentCategorySubjects: [],
    regionIndex: 0,
    sortIndex: 0,
    sortOptions: [
      { label: '最热', value: 'hot' },
      { label: '最新', value: 'latest' },
      { label: '成功次数', value: 'success' },
      { label: '距离最近', value: 'distance' }
    ],
    userLatitude: null,
    userLongitude: null,
    locationText: '未定位',
    loading: false,
    error: ''
  },
  onLoad(options) {
    const subjectId = (options && options.subjectId) || ''
    const regionId = (options && options.regionId) || ''
    const schoolKeyword = (options && options.schoolKeyword) || ''
    if (subjectId) {
      this.setData({ 'query.subjectId': subjectId })
    }
    if (regionId) {
      this.setData({ 'query.regionId': regionId })
    }
    if (schoolKeyword) {
      this.setData({ 'query.schoolKeyword': decodeURIComponent(schoolKeyword) })
    }
    this.loadFilters().then(() => this.load(true))
  },
  onShow() {
    this.applyRegionState(globalStore.getRegion())
    this._regionListener = (region) => this.applyRegionState(region)
    globalStore.subscribeRegion(this._regionListener)
    this.refreshRegionOnShow()
    if (!this.data.subjectCategoryTree.length && !this.data.loading) {
      this.loadFilters()
    }
    if (!this.data.list.length && !this.data.loading) {
      this.load(true)
    }
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
      // ignore: do not block list rendering
    }
  },
  applyRegionState(region) {
    if (!region) return
    const currentDistrictText = String(
      (region && (region.district || region.regionName || region.city || region.province)) || '未定位'
    )
    if (region.latitude != null && region.longitude != null) {
      this.setData({
        userLatitude: Number(region.latitude),
        userLongitude: Number(region.longitude),
        locationText: currentDistrictText,
        currentDistrictText
      })
    } else {
      this.setData({
        userLatitude: null,
        userLongitude: null,
        locationText: currentDistrictText,
        currentDistrictText
      })
    }
  },
  async initUserLocation(silent) {
    try {
      const pos = await getCurrentLocationWithAuth()
      this.setData({
        userLatitude: pos.latitude,
        userLongitude: pos.longitude,
        locationText: `${pos.longitude.toFixed(4)}, ${pos.latitude.toFixed(4)}`
      })
      return true
    } catch (e) {
      if (!silent) {
        wx.showToast({ title: e.message || '定位失败', icon: 'none' })
      }
      return false
    }
  },
  onPullDownRefresh() {
    this.load(true).finally(() => wx.stopPullDownRefresh())
  },
  async loadFilters() {
    try {
      const [data, categoryTree] = await Promise.all([
        request({ url: '/api/home/filters', authMode: 'optional' }),
        request({ url: '/api/content/subject-categories', authMode: 'optional' })
      ])
      const regions = [{ id: '', name: '全部区域' }].concat((data && data.regions) || [])
      const districtChips = [{ id: '', name: '全部' }].concat(
        (data && data.regions ? data.regions : []).map((r) => ({ id: r.id, name: r.regionName || r.name || '区域' }))
      )
      const globalRegion = globalStore.getRegion()
      if (!this.data.query.regionId && globalRegion && globalRegion.regionCode) {
        const exists = regions.find((r) => r.id === globalRegion.regionCode)
        if (exists) {
          this.setData({ 'query.regionId': globalRegion.regionCode })
        }
      }
      const regionIndex = regions.findIndex((r) => r.id === this.data.query.regionId)
      const activeDistrictId = this.data.query.regionId || ''
      const tree = [{
        id: '',
        categoryName: '全部',
        categoryCode: 'ALL',
        categorySort: 0,
        subjects: []
      }].concat(categoryTree || [])
      this.setData({
        'filters.regions': regions,
        regionIndex: regionIndex >= 0 ? regionIndex : 0,
        districtChips,
        activeDistrictId,
        subjectCategoryTree: tree,
        currentCategorySubjects: (tree[0] && tree[0].subjects) || []
      })
      this.syncSubjectName()
    } catch (e) {
      this.setData({ error: e.message || '筛选数据加载失败' })
    }
  },
  onTapCurrentDistrict() {
    // 轻交互：点击当前区=回到“全部区域”
    if (this.data.activeDistrictId === '') return
    this.chooseDistrict({ currentTarget: { dataset: { id: '' } } })
  },
  toggleNearby() {
    const next = !this.data.nearbyOnly
    if (!next) {
      this.setData({
        nearbyOnly: false,
        sortIndex: 0,
        'query.sortBy': 'hot'
      }, () => this.load(true))
      return
    }
    if (this.data.userLatitude == null || this.data.userLongitude == null) {
      this.initUserLocation(false).then((ok) => {
        if (!ok) return
        this.setData({
          nearbyOnly: true,
          sortIndex: 3,
          'query.sortBy': 'distance'
        }, () => this.load(true))
      })
      return
    }
    this.setData({
      nearbyOnly: true,
      sortIndex: 3,
      'query.sortBy': 'distance'
    }, () => this.load(true))
  },
  chooseDistrict(e) {
    const id = (e && e.currentTarget && e.currentTarget.dataset && e.currentTarget.dataset.id) || ''
    const regions = this.data.filters.regions || []
    const regionIndex = regions.findIndex((r) => String(r.id || '') === String(id || ''))
    this.setData({
      activeDistrictId: id,
      regionIndex: regionIndex >= 0 ? regionIndex : 0,
      'query.regionId': id
    }, () => this.load(true))
  },
  async load(reset = false) {
    const nextPage = reset ? 1 : this.data.pageNo
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return
    this.setData({ loading: true, error: '' })
    try {
      const { query, pageSize } = this.data
      const buildParams = (pageNo) => {
        const params = [
          `pageNo=${pageNo}`,
          `pageSize=${pageSize}`,
          'auditStatus=1',
          `sortBy=${encodeURIComponent(query.sortBy)}`
        ]
        if (query.subjectId) params.push(`subjectId=${encodeURIComponent(query.subjectId)}`)
        if (query.regionId) params.push(`regionId=${encodeURIComponent(query.regionId)}`)
        if (query.keyword) params.push(`keyword=${encodeURIComponent(query.keyword)}`)
        if (query.schoolKeyword) params.push(`schoolKeyword=${encodeURIComponent(query.schoolKeyword)}`)
        if (query.sortBy === 'distance' && this.data.userLatitude != null && this.data.userLongitude != null) {
          params.push(`userLat=${encodeURIComponent(this.data.userLatitude)}`)
          params.push(`userLng=${encodeURIComponent(this.data.userLongitude)}`)
        }
        return params
      }

      const mapRecords = (raw) => (raw || []).map((item) => {
        const rawPhoto = pickTeacherImage(item)
        const normalized = normalizeMediaUrl(rawPhoto)
        return {
          ...item,
          avatarError: false,
          teacherPhotoRaw: rawPhoto,
          teacherPhoto: normalized.url,
          teacherPhotoWarn: normalized.warn,
          teacherPhotoReason: normalized.reason,
          historyDealCount: Number(item.historyDealCount || 0),
          hireCount: Number(item.hireCount || 0),
          subjectText: (item.subjectNames && item.subjectNames.length) ? item.subjectNames.join('、') : '-',
          regionText: (item.regionNames && item.regionNames.length) ? item.regionNames.join('、') : '-',
          identityText: item.teacherIdentity || '教员',
          lastOrderTimeText: item.lastOrderTime ? String(item.lastOrderTime).slice(0, 10) : '暂无',
          distanceText: item.distanceKm == null ? '' : `${Number(item.distanceKm).toFixed(2)} km`
        }
      })

      const filterNearby = (records) => {
        if (!this.data.nearbyOnly || this.data.userLatitude == null || this.data.userLongitude == null) return records
        const km = Number(this.data.nearbyKm) || 5
        return records.filter((item) => item.distanceKm == null || Number(item.distanceKm) <= km)
      }

      let pageNo = nextPage
      let fetchedTotal = 0
      let appended = reset ? [] : this.data.list.slice()
      let hasMore = true
      let rawTotal = 0

      // nearbyOnly 下：如果过滤后太少，自动补拉 1-2 页（避免空列表假象）
      const maxExtraPages = this.data.nearbyOnly ? 2 : 0
      let extraCount = 0

      while (true) {
        const params = buildParams(pageNo)
        const data = await request({ url: `/api/home/teachers/search?${params.join('&')}`, authMode: 'optional' })
        rawTotal = Number((data && data.total) || 0)
        let records = mapRecords((data && data.records) || [])
        records = filterNearby(records)
        appended = appended.concat(records)
        fetchedTotal += Number(((data && data.records) || []).length)
        pageNo += 1
        hasMore = fetchedTotal < rawTotal

        if (!this.data.nearbyOnly) break
        if (appended.length >= pageSize) break
        if (!hasMore) break
        if (extraCount >= maxExtraPages) break
        extraCount += 1
      }

      this.setData({
        list: appended,
        hasMore,
        pageNo,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '教员列表加载失败' })
    }
  },
  onKeywordInput(e) {
    this.setData({ 'query.keyword': e.detail.value || '' })
  },
  onSchoolKeywordInput(e) {
    this.setData({ 'query.schoolKeyword': e.detail.value || '' })
  },
  openSubjectPanel() {
    this.setData({ showSubjectPanel: true })
  },
  closeSubjectPanel() {
    this.setData({ showSubjectPanel: false })
  },
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
      'query.subjectId': id,
      selectedSubjectName: name,
      showSubjectPanel: false
    }, () => this.load(true))
  },
  resetSubjectFilter() {
    this.setData({
      'query.subjectId': '',
      selectedSubjectName: '科目',
      showSubjectPanel: false,
      activeCategoryIndex: 0
    }, () => this.load(true))
  },
  syncSubjectName() {
    const targetId = this.data.query.subjectId
    if (!targetId) {
      this.setData({ selectedSubjectName: '科目' })
      return
    }
    const categories = this.data.subjectCategoryTree || []
    for (let i = 0; i < categories.length; i += 1) {
      const subjects = categories[i].subjects || []
      const found = subjects.find((item) => item.id === targetId)
      if (found) {
        this.setData({
          selectedSubjectName: found.name || '科目',
          activeCategoryIndex: i,
          currentCategorySubjects: subjects
        })
        return
      }
    }
    this.setData({ selectedSubjectName: '科目' })
  },
  onRegionChange(e) {
    const index = Number(e.detail.value)
    const item = this.data.filters.regions[index] || { id: '' }
    this.setData({
      regionIndex: index,
      activeDistrictId: item.id || '',
      'query.regionId': item.id || ''
    })
  },
  onSortChange(e) {
    const index = Number(e.detail.value)
    const item = this.data.sortOptions[index] || this.data.sortOptions[0]
    if (item.value === 'distance' && (this.data.userLatitude == null || this.data.userLongitude == null)) {
      this.initUserLocation(false).then((ok) => {
        if (!ok) return
        this.setData({
          nearbyOnly: true,
          sortIndex: index,
          'query.sortBy': item.value
        }, () => this.load(true))
      })
      return
    }
    this.setData({
      nearbyOnly: item.value === 'distance',
      sortIndex: index,
      'query.sortBy': item.value
    })
  },
  onSearch() {
    this.load(true)
  },
  onLoadMore() {
    this.load(false)
  },
  onAvatarError(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    const next = (this.data.list || []).map((item) => {
      if (String(item.teacherId) !== String(id)) return item
      logMediaDebug('teachers-list-avatar-error', {
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
    this.setData({ list: next })
  },
  noop() {},
  goDetail(e) {
    wx.navigateTo({ url: `/pages/teacher-detail/index?id=${e.currentTarget.dataset.id}` })
  }
})
