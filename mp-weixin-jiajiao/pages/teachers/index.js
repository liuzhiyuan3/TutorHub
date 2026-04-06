const { request } = require('../../utils/request')
const { getCurrentLocationWithAuth } = require('../../utils/location')
const globalStore = require('../../utils/global-store')
const { resolveRegionSmart } = require('../../utils/location-service')
const { normalizeMediaUrl, logMediaDebug, pickTeacherImage } = require('../../utils/media-url')

Page({
  data: {
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    list: [],
    filters: {
      regions: [],
      regionTree: []
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
    provinceOptions: [{ name: '全部省份' }],
    cityOptions: [{ name: '全部城市' }],
    provinceIndex: 0,
    cityIndex: 0,
    selectedProvinceName: '',
    selectedCityName: '',
    manualRegionChosen: false,
    selectedSubjectName: '科目',
    advancedVisible: false,
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
      const resolved = await resolveRegionSmart()
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
      const flatRegions = (data && data.regions) || []
      const regions = [{ id: '', name: '全部区域' }].concat(flatRegions)
      const rawTree = Array.isArray(data && data.regionTree) ? data.regionTree : []
      const globalRegion = globalStore.getRegion()
      if (!this.data.query.regionId && globalRegion && globalRegion.regionCode && !this.data.manualRegionChosen) {
        const exists = regions.find((r) => r.id === globalRegion.regionCode)
        if (exists) this.setData({ 'query.regionId': globalRegion.regionCode })
      }
      const regionTree = this.normalizeRegionTree(rawTree)
      const cascadeState = this.buildRegionCascadeState(regionTree, this.data.query.regionId, globalRegion)
      const regionIndex = regions.findIndex((r) => r.id === cascadeState.activeDistrictId)
      const tree = [{
        id: '',
        categoryName: '全部',
        categoryCode: 'ALL',
        categorySort: 0,
        subjects: []
      }].concat(categoryTree || [])
      this.setData({
        'filters.regions': regions,
        'filters.regionTree': regionTree,
        regionIndex: regionIndex >= 0 ? regionIndex : 0,
        districtChips: cascadeState.districtChips,
        activeDistrictId: cascadeState.activeDistrictId,
        provinceOptions: cascadeState.provinceOptions,
        cityOptions: cascadeState.cityOptions,
        provinceIndex: cascadeState.provinceIndex,
        cityIndex: cascadeState.cityIndex,
        selectedProvinceName: cascadeState.selectedProvinceName,
        selectedCityName: cascadeState.selectedCityName,
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
    const pathMap = this.buildDistrictPathMap(this.data.filters.regionTree || [])
    const path = pathMap[id]
    const nextState = path ? this.buildRegionCascadeState(this.data.filters.regionTree || [], id, null, true) : null
    this.setData({
      activeDistrictId: id,
      regionIndex: regionIndex >= 0 ? regionIndex : 0,
      'query.regionId': id,
      manualRegionChosen: true,
      provinceOptions: nextState ? nextState.provinceOptions : this.data.provinceOptions,
      cityOptions: nextState ? nextState.cityOptions : this.data.cityOptions,
      provinceIndex: nextState ? nextState.provinceIndex : this.data.provinceIndex,
      cityIndex: nextState ? nextState.cityIndex : this.data.cityIndex,
      selectedProvinceName: path ? path.provinceName : this.data.selectedProvinceName,
      selectedCityName: path ? path.cityName : this.data.selectedCityName
    }, () => this.load(true))
  },
  onProvinceChange(e) {
    const index = Number(e.detail.value)
    const provinceOptions = this.data.provinceOptions || [{ name: '全部省份' }]
    const provinceName = (provinceOptions[index] && provinceOptions[index].name) || ''
    const nextState = this.buildRegionCascadeState(
      this.data.filters.regionTree || [],
      '',
      null,
      true,
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
      'query.regionId': '',
      manualRegionChosen: true
    }, () => this.load(true))
  },
  onCityChange(e) {
    const index = Number(e.detail.value)
    const cityOptions = this.data.cityOptions || [{ name: '全部城市' }]
    const cityName = (cityOptions[index] && cityOptions[index].name) || ''
    const nextState = this.buildRegionCascadeState(
      this.data.filters.regionTree || [],
      '',
      null,
      true,
      this.data.selectedProvinceName,
      cityName
    )
    this.setData({
      cityIndex: nextState.cityIndex,
      selectedCityName: nextState.selectedCityName,
      districtChips: nextState.districtChips,
      activeDistrictId: '',
      'query.regionId': '',
      manualRegionChosen: true
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
        if (this.data.nearbyOnly && this.data.userLatitude != null && this.data.userLongitude != null) {
          params.push(`maxDistanceKm=${encodeURIComponent(this.data.nearbyKm)}`)
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
  toggleAdvanced() {
    this.setData({ advancedVisible: !this.data.advancedVisible })
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
  normalizeRegionText(text) {
    return String(text || '').replace(/特别行政区|自治州|自治县|地区|省|市|区|县/g, '').trim()
  },
  buildDistrictPathMap(regionTree) {
    const map = {}
    ;(regionTree || []).forEach((province, pIndex) => {
      ;((province && province.cities) || []).forEach((city, cIndex) => {
        ;((city && city.districts) || []).forEach((district, dIndex) => {
          if (!district || !district.id) return
          map[district.id] = {
            provinceIndex: pIndex,
            cityIndex: cIndex,
            districtIndex: dIndex,
            provinceName: province.name,
            cityName: city.name
          }
        })
      })
    })
    return map
  },
  findBestPathByGeo(regionTree, geo) {
    if (!geo) return null
    const targetProvince = this.normalizeRegionText(geo.province)
    const targetCity = this.normalizeRegionText(geo.city)
    const targetDistrict = this.normalizeRegionText(geo.district || geo.regionName)
    let best = null
    let bestScore = -1
    ;(regionTree || []).forEach((province, pIndex) => {
      const provinceNorm = this.normalizeRegionText(province.name)
      ;((province && province.cities) || []).forEach((city, cIndex) => {
        const cityNorm = this.normalizeRegionText(city.name)
        ;((city && city.districts) || []).forEach((district, dIndex) => {
          const districtNorm = this.normalizeRegionText(district.name)
          let score = 0
          if (targetProvince && provinceNorm && provinceNorm === targetProvince) score += 1
          if (targetCity && cityNorm && cityNorm === targetCity) score += 2
          if (targetDistrict && districtNorm && districtNorm === targetDistrict) score += 3
          if (score > bestScore) {
            bestScore = score
            best = { provinceIndex: pIndex, cityIndex: cIndex, districtIndex: dIndex, districtId: district.id }
          }
        })
      })
    })
    return bestScore > 0 ? best : null
  },
  buildRegionCascadeState(regionTree, regionId, globalRegion, useCurrentSelection, forcedProvinceName, forcedCityName) {
    const tree = Array.isArray(regionTree) && regionTree.length ? regionTree : []
    if (!tree.length) {
      const allDistricts = ((this.data.filters && this.data.filters.regions) || []).filter((item) => item.id)
        .map((item) => ({ id: item.id, name: item.regionName || item.name || '区域' }))
      return {
        provinceOptions: [{ name: '全部省份' }],
        cityOptions: [{ name: '全部城市' }],
        provinceIndex: 0,
        cityIndex: 0,
        selectedProvinceName: '',
        selectedCityName: '',
        districtChips: [{ id: '', name: '全部' }].concat(allDistricts),
        activeDistrictId: regionId || ''
      }
    }

    const provinceOptions = [{ name: '全部省份' }].concat(tree.map((item) => ({ name: item.name })))
    const pathMap = this.buildDistrictPathMap(tree)
    let path = regionId ? pathMap[regionId] : null
    if (!path && globalRegion && globalRegion.regionCode) path = pathMap[globalRegion.regionCode]
    if (!path && !useCurrentSelection) {
      path = this.findBestPathByGeo(tree, globalRegion)
    }

    let selectedProvinceName = forcedProvinceName != null ? forcedProvinceName : ''
    let selectedCityName = forcedCityName != null ? forcedCityName : ''
    if (!selectedProvinceName && path) selectedProvinceName = (tree[path.provinceIndex] && tree[path.provinceIndex].name) || ''
    if (!selectedCityName && path && selectedProvinceName) {
      const city = (((tree[path.provinceIndex] || {}).cities || [])[path.cityIndex] || {}).name || ''
      selectedCityName = city
    }
    if (useCurrentSelection && !selectedProvinceName) selectedProvinceName = this.data.selectedProvinceName || ''
    if (useCurrentSelection && !selectedCityName) selectedCityName = this.data.selectedCityName || ''

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

    const validDistrictIds = new Set(districts.map((item) => item.id))
    const activeDistrictId = regionId && validDistrictIds.has(regionId) ? regionId : ''
    return {
      provinceOptions,
      cityOptions,
      provinceIndex,
      cityIndex,
      selectedProvinceName: provinceIndex > 0 ? provinceOptions[provinceIndex].name : '',
      selectedCityName: cityIndex > 0 ? cityOptions[cityIndex].name : '',
      districtChips: [{ id: '', name: '全部' }].concat(districts),
      activeDistrictId
    }
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
    if (this.data.advancedVisible) {
      this.setData({ advancedVisible: false })
    }
  },
  resetFilters() {
    this.setData({
      query: {
        subjectId: '',
        regionId: '',
        schoolKeyword: '',
        keyword: '',
        sortBy: 'hot'
      },
      sortIndex: 0,
      nearbyOnly: false,
      advancedVisible: false,
      selectedSubjectName: '科目',
      showSubjectPanel: false,
      activeCategoryIndex: 0,
      selectedProvinceName: '',
      selectedCityName: '',
      provinceIndex: 0,
      cityIndex: 0,
      activeDistrictId: '',
      districtChips: [{ id: '', name: '全部' }]
    }, () => {
      const nextState = this.buildRegionCascadeState(this.data.filters.regionTree || [], '', null, false)
      this.setData({
        provinceOptions: nextState.provinceOptions,
        cityOptions: nextState.cityOptions,
        provinceIndex: nextState.provinceIndex,
        cityIndex: nextState.cityIndex,
        selectedProvinceName: nextState.selectedProvinceName,
        selectedCityName: nextState.selectedCityName,
        districtChips: nextState.districtChips,
        activeDistrictId: nextState.activeDistrictId
      }, () => this.load(true))
    })
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
