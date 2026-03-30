const { request, uploadFile } = require('../../utils/request')
const { ensureRole, getLoginState } = require('../../utils/auth-guard')
const { getCurrentLocationWithAuth, chooseLocationWithAuth } = require('../../utils/location')
const { reverseGeocode, classifyLocationError, toLocationErrorMessage } = require('../../utils/location-service')

function parseCoordinateSeed(raw, type) {
  if (raw === '' || raw === null || raw === undefined) return null
  const num = Number(raw)
  if (!Number.isFinite(num)) return null
  if (type === 'latitude' && (num < -90 || num > 90)) return null
  if (type === 'longitude' && (num < -180 || num > 180)) return null
  return num
}

Page({
  data: {
    subjects: [],
    regions: [],
    subjectIndex: 0,
    regionIndex: 0,
    methodIndex: 1,
    metaError: '',
    submitting: false,
    uploadingImages: false,
    requirementImages: [],
    salarySuggest: [120, 150, 180, 220, 260, 300],
    methodOptions: [
      { label: '线上', value: 0 },
      { label: '线下', value: 1 },
      { label: '线上线下', value: 2 }
    ],
    studentGenderOptions: ['ANY', 'MALE', 'FEMALE'],
    teacherGenderOptions: ['ANY', 'MALE', 'FEMALE'],
    studentGenderIndex: 0,
    teacherGenderIndex: 0,
    authSheetVisible: false,
    authSheetRole: 'parent',
    locationSheetVisible: false,
    locating: false,
    locationStatusText: '',
    locationStatusType: '',
    form: {
      requirementTitle: '',
      requirementDescription: '',
      requirementGrade: '',
      requirementAddress: '',
      requirementSalary: '150',
      requirementTutoringMethod: 1,
      studentGender: 'ANY',
      salaryText: '',
      crossStreet: '',
      studentDetail: '',
      teacherQualification: '',
      teacherGenderPreference: 'ANY',
      teacherRequirementText: '',
      requirementLongitude: '',
      requirementLatitude: ''
    }
  },
  onShow() {
    this.loadMeta()
  },
  async loadMeta() {
    this.setData({ metaError: '' })
    try {
      const [subjects, regions] = await Promise.all([
        request({ url: '/api/content/subjects', authMode: 'optional' }),
        request({ url: '/api/content/regions', authMode: 'optional' })
      ])
      this.setData({ subjects: subjects || [], regions: regions || [] })
    } catch (e) {
      this.setData({ metaError: e.message || '基础数据加载失败' })
    }
  },
  async uploadImage(filePath) {
    const data = await uploadFile({
      url: '/api/file/upload',
      filePath,
      name: 'file',
      authMode: 'required',
      formData: { biz: 'mini-requirement' }
    })
    return (data && data.url) || ''
  },
  chooseImages() {
    if (this.data.uploadingImages) return
    const remain = 6 - this.data.requirementImages.length
    if (remain <= 0) {
      wx.showToast({ title: '最多上传6张图片', icon: 'none' })
      return
    }
    wx.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const files = res.tempFilePaths || []
        if (!files.length) return
        this.setData({ uploadingImages: true })
        try {
          const uploaded = []
          for (let i = 0; i < files.length; i += 1) {
            const url = await this.uploadImage(files[i])
            if (url) uploaded.push(url)
          }
          const requirementImages = this.data.requirementImages.concat(uploaded).slice(0, 6)
          this.setData({ requirementImages })
          wx.showToast({ title: '图片上传成功', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: e.message || '图片上传失败', icon: 'none' })
        } finally {
          this.setData({ uploadingImages: false })
        }
      }
    })
  },
  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    const requirementImages = this.data.requirementImages.filter((_, idx) => idx !== index)
    this.setData({ requirementImages })
  },
  onInput(e) {
    const key = e.currentTarget.dataset.key
    const value = (e && e.detail && (e.detail.value !== undefined ? e.detail.value : e.detail)) || ''
    this.setData({ [`form.${key}`]: value })
  },
  openLocationSheet() {
    this.setData({ locationSheetVisible: true })
  },
  closeLocationSheet() {
    this.setData({ locationSheetVisible: false })
  },
  handleLocationFailure(error, opts = {}) {
    const reason = classifyLocationError(error)
    if (reason === 'cancel') return
    this.setData({
      locationStatusText: toLocationErrorMessage(error),
      locationStatusType: 'error'
    })

    if (reason === 'auth') {
      wx.showModal({
        title: '定位权限未开启',
        content: '请在设置中开启定位权限，或使用地图手动选点。',
        confirmText: '去设置',
        cancelText: '手动选点',
        success: (res) => {
          if (res.confirm) {
            wx.openSetting({})
          } else if (!opts.fromMap) {
            this.chooseLocationOnMap()
          }
        }
      })
      return
    }

    if (reason === 'service_off') {
      wx.showModal({
        title: '系统定位未开启',
        content: '请先开启手机定位服务，或直接使用地图手动选点。',
        confirmText: '地图选点',
        cancelText: '知道了',
        success: (res) => {
          if (res.confirm && !opts.fromMap) {
            this.chooseLocationOnMap()
          }
        }
      })
      return
    }

    wx.showToast({ title: toLocationErrorMessage(error), icon: 'none' })
  },
  async useCurrentLocation() {
    if (this.data.locating) return
    this.setData({ locating: true })
    try {
      const pos = await getCurrentLocationWithAuth()
      let addressText = `经度${pos.longitude.toFixed(6)}，纬度${pos.latitude.toFixed(6)}`
      try {
        const geo = await reverseGeocode(pos)
        const structured = [geo.province, geo.city, geo.district].filter(Boolean).join('')
        addressText = geo.address || structured || addressText
      } catch (geoError) {
        wx.showToast({ title: '地址解析失败，已记录坐标', icon: 'none' })
      }
      this.setData({
        'form.requirementAddress': addressText,
        'form.requirementLongitude': String(pos.longitude),
        'form.requirementLatitude': String(pos.latitude),
        locationSheetVisible: false,
        locationStatusText: '定位成功，地址已自动填充',
        locationStatusType: 'success'
      })
      wx.showToast({ title: '已获取当前位置', icon: 'success' })
    } catch (e) {
      this.handleLocationFailure(e, { fromMap: false })
    } finally {
      this.setData({ locating: false })
    }
  },
  async chooseLocationOnMap() {
    if (this.data.locating) return
    this.setData({ locating: true })
    try {
      const seedLongitude = parseCoordinateSeed(this.data.form.requirementLongitude, 'longitude')
      const seedLatitude = parseCoordinateSeed(this.data.form.requirementLatitude, 'latitude')
      const chooseOptions = {}
      if (seedLatitude !== null) chooseOptions.latitude = seedLatitude
      if (seedLongitude !== null) chooseOptions.longitude = seedLongitude
      const selected = await chooseLocationWithAuth(chooseOptions)
      const fallback = `经度${Number(selected.longitude).toFixed(6)}，纬度${Number(selected.latitude).toFixed(6)}`
      let address = selected.address || selected.name || ''
      if (!address) {
        try {
          const geo = await reverseGeocode({ latitude: selected.latitude, longitude: selected.longitude })
          const structured = [geo.province, geo.city, geo.district].filter(Boolean).join('')
          address = geo.address || structured || ''
        } catch (geoError) {
          // keep fallback coordinate text below
        }
      }
      this.setData({
        'form.requirementAddress': address || fallback,
        'form.requirementLongitude': String(selected.longitude),
        'form.requirementLatitude': String(selected.latitude),
        locationSheetVisible: false,
        locationStatusText: '地图选点成功，地址已更新',
        locationStatusType: 'success'
      })
      wx.showToast({ title: '地址已更新', icon: 'success' })
    } catch (e) {
      this.handleLocationFailure(e, { fromMap: true })
    } finally {
      this.setData({ locating: false })
    }
  },
  onPickSalary(e) {
    const value = e.currentTarget.dataset.value
    this.setData({ 'form.requirementSalary': String(value) })
  },
  onSubjectChange(e) {
    this.setData({ subjectIndex: Number(e.detail.value) })
  },
  onRegionChange(e) {
    this.setData({ regionIndex: Number(e.detail.value) })
  },
  onMethodChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      methodIndex: index,
      'form.requirementTutoringMethod': this.data.methodOptions[index].value
    })
  },
  onStudentGenderChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      studentGenderIndex: index,
      'form.studentGender': this.data.studentGenderOptions[index] || 'ANY'
    })
  },
  onTeacherGenderChange(e) {
    const index = Number(e.detail.value)
    this.setData({
      teacherGenderIndex: index,
      'form.teacherGenderPreference': this.data.teacherGenderOptions[index] || 'ANY'
    })
  },
  validate() {
    const { form } = this.data
    if (!form.requirementTitle.trim()) return '请填写需求标题'
    if (form.requirementTitle.trim().length > 60) return '需求标题不超过60个字'
    if (!form.requirementDescription.trim()) return '请填写需求描述'
    if (!form.requirementGrade.trim()) return '请填写需求年级'
    if (!form.requirementAddress.trim()) return '请填写授课地址'
    const salary = Number(form.requirementSalary)
    if (!salary || salary <= 0) return '请填写正确的薪资'
    if (salary < 20 || salary > 2000) return '薪资范围建议在20~2000元/小时'
    return ''
  },
  buildSubmitPayload(subject, region) {
    const form = this.data.form
    const salary = Number(form.requirementSalary)
    return {
      ...form,
      requirementTitle: form.requirementTitle.trim(),
      requirementDescription: form.requirementDescription.trim(),
      requirementGrade: form.requirementGrade.trim(),
      requirementAddress: form.requirementAddress.trim(),
      salaryText: (form.salaryText || '').trim() || `${salary}元/小时（可协商）`,
      crossStreet: (form.crossStreet || '').trim(),
      studentDetail: (form.studentDetail || '').trim(),
      teacherQualification: (form.teacherQualification || '').trim(),
      teacherRequirementText: (form.teacherRequirementText || '').trim(),
      subjectId: subject.id,
      regionId: region.id,
      requirementSalary: salary,
      requirementLongitude: form.requirementLongitude ? Number(form.requirementLongitude) : null,
      requirementLatitude: form.requirementLatitude ? Number(form.requirementLatitude) : null,
      requirementImages: this.data.requirementImages.join(',')
    }
  },
  ensureParentForSubmit() {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingSubmit = true
    if (!ensureRole('parent', '发布需求仅家长可操作，是否前往登录？')) {
      if (state.loggedIn) this._pendingSubmit = false
      return false
    }
    this._pendingSubmit = false
    return true
  },
  async submit() {
    if (!this.ensureParentForSubmit()) return
    if (this.data.submitting || this.data.uploadingImages) return
    const subject = this.data.subjects[this.data.subjectIndex]
    const region = this.data.regions[this.data.regionIndex]
    const message = this.validate()
    if (message) {
      wx.showToast({ title: message, icon: 'none' })
      return
    }
    if (!subject || !region) {
      wx.showToast({ title: '请先选择学科和区域', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const payload = this.buildSubmitPayload(subject, region)
      await request({
        url: '/api/requirement',
        method: 'POST',
        authMode: 'required',
        data: payload
      })
      wx.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/requirements/index' }), 600)
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || 'parent' })
  },
  onAuthSheetClose() {
    this._pendingSubmit = false
    this.setData({ authSheetVisible: false })
  },
  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false })
    if (this._pendingSubmit) {
      this._pendingSubmit = false
      this.submit()
    }
  }
})
