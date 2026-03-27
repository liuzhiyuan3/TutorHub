const { request, uploadFile } = require('../../utils/request')
const { ensureRole } = require('../../utils/auth-guard')
const { prepareImageForUpload } = require('../../utils/image-upload')

Page({
  data: {
    loading: false,
    saving: false,
    teacherPhotoUploading: false,
    teacher: {
      teacherIdentity: '',
      teacherPhoto: '',
      teacherTutoringMethod: 2,
      teacherTeachingYears: 0,
      teacherSchool: '',
      teacherMajor: '',
      teacherEducation: '',
      teacherExperience: '',
      teacherSelfDescription: ''
    },
    tutoringMethodIndex: 2,
    authSheetVisible: false,
    authSheetRole: 'teacher',
    tutoringMethodOptions: [
      { label: '线上', value: 0 },
      { label: '线下', value: 1 },
      { label: '线上线下', value: 2 }
    ]
  },
  onShow() {
    if (!ensureRole('teacher', '仅教员可编辑资料，是否前往登录？')) return
    this.load()
  },
  async load() {
    this.setData({ loading: true })
    try {
      const teacher = await request({ url: '/api/teacher/profile/me', authMode: 'required' })
      const nextTeacher = { ...this.data.teacher, ...(teacher || {}) }
      const idx = this.data.tutoringMethodOptions.findIndex((item) => item.value === nextTeacher.teacherTutoringMethod)
      this.setData({
        teacher: nextTeacher,
        tutoringMethodIndex: idx >= 0 ? idx : 2,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
      wx.showToast({ title: e.message || '资料加载失败', icon: 'none' })
    }
  },
  onTeacherInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`teacher.${key}`]: e.detail.value })
  },
  onTutoringMethodChange(e) {
    const index = Number(e.detail.value)
    const option = this.data.tutoringMethodOptions[index] || this.data.tutoringMethodOptions[2]
    this.setData({
      tutoringMethodIndex: index,
      'teacher.teacherTutoringMethod': option.value
    })
  },
  async uploadImage(filePath, biz) {
    const prepared = await prepareImageForUpload(filePath, { maxBytes: 700 * 1024 })
    const data = await uploadFile({
      url: '/api/file/upload',
      filePath: prepared.filePath,
      name: 'file',
      authMode: 'required',
      formData: { biz }
    })
    return (data && data.url) || ''
  },
  chooseTeacherPhoto() {
    if (this.data.teacherPhotoUploading) return
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const filePath = res.tempFilePaths[0]
        if (!filePath) return
        this.setData({ teacherPhotoUploading: true })
        try {
          const url = await this.uploadImage(filePath, 'mini-teacher-photo')
          if (!url) throw new Error('上传失败')
          this.setData({ 'teacher.teacherPhoto': url })
          wx.showToast({ title: '照片已上传', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: e.message || '上传失败', icon: 'none' })
        } finally {
          this.setData({ teacherPhotoUploading: false })
        }
      }
    })
  },
  async submitTeacherProfile() {
    if (!ensureRole('teacher', '仅教员可提交资料，是否前往登录？')) return
    if (this.data.saving) return
    if (!String(this.data.teacher.teacherIdentity || '').trim()) {
      wx.showToast({ title: '请填写教员身份', icon: 'none' })
      return
    }
    if (!String(this.data.teacher.teacherSchool || '').trim()) {
      wx.showToast({ title: '请填写学校', icon: 'none' })
      return
    }
    if (!String(this.data.teacher.teacherMajor || '').trim()) {
      wx.showToast({ title: '请填写专业', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      await request({ url: '/api/teacher/profile', method: 'POST', authMode: 'required', data: this.data.teacher })
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => wx.navigateBack({ delta: 1 }), 400)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  },
  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || 'teacher' })
  },
  onAuthSheetClose() {
    this.setData({ authSheetVisible: false })
  },
  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false })
    this.load()
  }
})
