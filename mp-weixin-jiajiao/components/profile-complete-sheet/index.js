const authService = require('../../utils/auth-service')

const DEFAULT_AVATAR_URL = '/static/imgDefault.png'
const LEGACY_BROKEN_AVATAR_PREFIX = 'https://mmbiz.qpic.cn/mmbiz/icTDbqWNOwNRna42FI242Lcia07jQodd2FJc8qfQ7Aiaf6v0Owr4KJ6SJ6VQkPqP5wM5v5v6fR5bLJ6f8uQ94m4A'
const MISSING_FIELD_LABEL_MAP = {
  avatarUrl: '头像',
  nickName: '昵称',
  teacherIdentity: '教员身份',
  teacherTutoringMethod: '授课方式',
  teacherTeachingYears: '教龄',
  teacherSchool: '学校',
  teacherMajor: '专业',
  teacherEducation: '学历',
  teacherExperience: '教学经验',
  teacherSelfDescription: '自我描述'
}

function formatMissingFields(fields) {
  if (!Array.isArray(fields)) return ''
  return fields
    .map((field) => MISSING_FIELD_LABEL_MAP[field] || field)
    .join('、')
}

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    role: {
      type: String,
      value: 'parent'
    },
    canSkip: {
      type: Boolean,
      value: true
    },
    missingFields: {
      type: Array,
      value: []
    },
    initNickName: {
      type: String,
      value: ''
    },
    initAvatarUrl: {
      type: String,
      value: ''
    }
  },
  data: {
    submitting: false,
    uploading: false,
    avatarLocalPath: '',
    avatarPreviewUrl: DEFAULT_AVATAR_URL,
    missingFieldsText: '',
    tutoringMethodOptions: [
      { label: '线上', value: 0 },
      { label: '线下', value: 1 },
      { label: '线上+线下', value: 2 }
    ],
    tutoringMethodLabel: '请选择',
    form: {
      nickName: '',
      avatarUrl: '',
      teacherIdentity: '',
      teacherTutoringMethod: '',
      teacherTeachingYears: '',
      teacherSchool: '',
      teacherMajor: '',
      teacherEducation: '',
      teacherExperience: '',
      teacherSelfDescription: ''
    }
  },
  observers: {
    visible(next) {
      if (next) {
        this.resetFormByProps()
      }
    },
    missingFields(next) {
      this.setData({
        missingFieldsText: formatMissingFields(next)
      })
    }
  },
  methods: {
    noop() {},
    onMaskTap() {
      if (!this.properties.canSkip || this.properties.role === 'teacher') {
        wx.showToast({ title: '请先完善资料', icon: 'none' })
        return
      }
      this.triggerEvent('close')
    },
    resetFormByProps() {
      const avatar = this.sanitizeAvatarUrl(this.properties.initAvatarUrl || DEFAULT_AVATAR_URL)
      const form = {
        nickName: this.properties.initNickName || '',
        avatarUrl: this.isRemoteUrl(avatar) ? avatar : '',
        teacherIdentity: '',
        teacherTutoringMethod: '',
        teacherTeachingYears: '',
        teacherSchool: '',
        teacherMajor: '',
        teacherEducation: '',
        teacherExperience: '',
        teacherSelfDescription: ''
      }
      this.setData({
        form,
        avatarLocalPath: '',
        avatarPreviewUrl: avatar,
        submitting: false,
        uploading: false,
        tutoringMethodLabel: '请选择',
        missingFieldsText: formatMissingFields(this.properties.missingFields || [])
      })
    },
    isRemoteUrl(url) {
      const value = String(url || '').trim()
      return value.startsWith('http://') || value.startsWith('https://')
    },
    sanitizeAvatarUrl(url) {
      const value = String(url || '').trim()
      if (!value) return DEFAULT_AVATAR_URL
      if (value.startsWith(LEGACY_BROKEN_AVATAR_PREFIX)) return DEFAULT_AVATAR_URL
      return value
    },
    onAvatarLoadError() {
      this.setData({
        avatarLocalPath: '',
        avatarPreviewUrl: DEFAULT_AVATAR_URL,
        'form.avatarUrl': ''
      })
    },
    onChooseAvatar(e) {
      const detail = (e && e.detail) || {}
      if (!detail.avatarUrl) return
      this.setData({
        avatarLocalPath: detail.avatarUrl,
        avatarPreviewUrl: detail.avatarUrl
      })
    },
    chooseFromAlbum() {
      if (this.data.uploading || this.data.submitting) return
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album'],
        success: (res) => {
          const files = (res && res.tempFiles) || []
          const first = files[0] || {}
          if (!first.tempFilePath) return
          this.setData({
            avatarLocalPath: first.tempFilePath,
            avatarPreviewUrl: first.tempFilePath
          })
        }
      })
    },
    onInput(e) {
      const key = e.currentTarget.dataset.key
      const value = (e && e.detail && (e.detail.value !== undefined ? e.detail.value : e.detail)) || ''
      this.setData({ [`form.${key}`]: value })
    },
    onTutoringMethodChange(e) {
      const index = Number((e && e.detail && e.detail.value) || 0)
      const item = this.data.tutoringMethodOptions[index]
      this.setData({
        'form.teacherTutoringMethod': item ? item.value : '',
        tutoringMethodLabel: item ? item.label : '请选择'
      })
    },
    skip() {
      this.triggerEvent('skip')
    },
    validateForm() {
      const nickName = String(this.data.form.nickName || '').trim()
      if (!nickName) {
        throw new Error('请填写昵称')
      }
      if (!this.data.avatarPreviewUrl) {
        throw new Error('请设置头像')
      }
      if (this.properties.role === 'teacher') {
        if (!String(this.data.form.teacherIdentity || '').trim()) throw new Error('请填写教员身份')
        if (this.data.form.teacherTutoringMethod === '') throw new Error('请选择授课方式')
        if (!String(this.data.form.teacherSchool || '').trim()) throw new Error('请填写学校')
        if (!String(this.data.form.teacherMajor || '').trim()) throw new Error('请填写专业')
        if (!String(this.data.form.teacherEducation || '').trim()) throw new Error('请填写学历')
      }
    },
    async resolveAvatarUrl() {
      if (this.isRemoteUrl(this.data.avatarPreviewUrl)) {
        return this.data.avatarPreviewUrl
      }
      const localPath = String(this.data.avatarLocalPath || '').trim()
      if (!localPath) {
        return ''
      }
      this.setData({ uploading: true })
      try {
        const result = await authService.uploadAvatar(localPath)
        return (result && result.url) || ''
      } finally {
        this.setData({ uploading: false })
      }
    },
    async submit() {
      if (this.data.submitting || this.data.uploading) return
      try {
        this.validateForm()
      } catch (e) {
        wx.showToast({ title: e.message || '请完善资料', icon: 'none' })
        return
      }
      this.setData({ submitting: true })
      try {
        const avatarUrl = await this.resolveAvatarUrl()
        const payload = {
          nickName: String(this.data.form.nickName || '').trim(),
          avatarUrl: avatarUrl || this.data.form.avatarUrl || '',
          role: this.properties.role,
          teacherIdentity: String(this.data.form.teacherIdentity || '').trim(),
          teacherTutoringMethod: this.data.form.teacherTutoringMethod === '' ? null : Number(this.data.form.teacherTutoringMethod),
          teacherTeachingYears: this.data.form.teacherTeachingYears === '' ? null : Number(this.data.form.teacherTeachingYears),
          teacherSchool: String(this.data.form.teacherSchool || '').trim(),
          teacherMajor: String(this.data.form.teacherMajor || '').trim(),
          teacherEducation: String(this.data.form.teacherEducation || '').trim(),
          teacherExperience: String(this.data.form.teacherExperience || '').trim(),
          teacherSelfDescription: String(this.data.form.teacherSelfDescription || '').trim()
        }
        await authService.completeProfile(payload)
        wx.showToast({ title: '资料已完善', icon: 'success' })
        this.triggerEvent('success')
      } catch (e) {
        wx.showToast({ title: e.message || '保存失败', icon: 'none' })
      } finally {
        this.setData({ submitting: false })
      }
    }
  }
})
