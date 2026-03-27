const { request } = require('../../utils/request')

Page({
  data: {
    submitting: false,
    userTypeOptions: [
      { label: '家长', value: 0 },
      { label: '教员', value: 1 }
    ],
    currentUserTypeLabel: '家长',
    agreeProtocol: false,
    form: {
      name: '',
      phone: '',
      account: '',
      password: '',
      confirmPassword: '',
      userType: 0
    }
  },
  onLoad(options) {
    const nextType = Number(options && options.userType)
    if (nextType === 0 || nextType === 1) {
      this.setData({ 'form.userType': nextType }, () => this.refreshTypeLabel())
      return
    }
    this.refreshTypeLabel()
  },
  refreshTypeLabel() {
    const idx = Number(this.data.form.userType) || 0
    const current = this.data.userTypeOptions[idx] || this.data.userTypeOptions[0]
    this.setData({ currentUserTypeLabel: current.label })
  },
  onTypeChange(e) {
    this.setData({ 'form.userType': Number(e.detail.value) }, () => this.refreshTypeLabel())
  },
  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value || '' })
  },
  onToggleProtocol() {
    this.setData({ agreeProtocol: !this.data.agreeProtocol })
  },
  validate() {
    const form = this.data.form
    if (!String(form.name || '').trim()) return '请输入姓名'
    if (!/^1\d{10}$/.test(String(form.phone || '').trim())) return '请输入正确手机号'
    if (!/^[a-zA-Z0-9_]{4,20}$/.test(String(form.account || '').trim())) return '账号需4-20位字母数字或下划线'
    if (!/^.{6,20}$/.test(String(form.password || ''))) return '密码需6-20位'
    if (form.password !== form.confirmPassword) return '两次密码不一致'
    if (!this.data.agreeProtocol) return '请先阅读并同意协议'
    return ''
  },
  async submitRegister() {
    if (this.data.submitting) return
    const message = this.validate()
    if (message) {
      wx.showToast({ title: message, icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await request({
        url: '/api/auth/register',
        method: 'POST',
        authMode: 'optional',
        data: {
          name: this.data.form.name.trim(),
          phone: this.data.form.phone.trim(),
          account: this.data.form.account.trim(),
          password: this.data.form.password,
          userType: Number(this.data.form.userType) === 1 ? 1 : 0
        }
      })
      wx.showToast({ title: '注册成功，请登录', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/login/index?userType=${Number(this.data.form.userType) === 1 ? 1 : 0}`
        })
      }, 500)
    } catch (e) {
      const text = String((e && e.message) || '注册失败')
      if (text.includes('账号已存在') || text.includes('手机号已存在')) {
        wx.showToast({ title: text, icon: 'none' })
        return
      }
      wx.showToast({ title: `注册失败：${text}`, icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
