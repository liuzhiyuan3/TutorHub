const { request } = require('../../utils/request')

Page({
  data: {
    form: {
      account: 'parent01',
      password: '123456',
      userType: 0
    },
    userTypeOptions: [
      { label: '家长', value: 0 },
      { label: '教员', value: 1 }
    ]
  },
  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },
  onTypeChange(e) {
    this.setData({ 'form.userType': Number(e.detail.value) })
  },
  async submit() {
    const app = getApp()
    try {
      const url = this.data.form.userType === 0 || this.data.form.userType === 1 ? '/api/auth/user/login' : '/api/auth/admin/login'
      const data = await request({
        url,
        method: 'POST',
        data: {
          account: this.data.form.account,
          password: this.data.form.password
        }
      })
      app.globalData.token = data.token
      app.globalData.userType = data.userType
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userType', data.userType)
      wx.switchTab({ url: '/pages/home/index' })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  async goRegister() {
    try {
      await request({
        url: '/api/auth/register',
        method: 'POST',
        data: {
          account: `u${Date.now().toString().slice(-6)}`,
          password: '123456',
          name: '新用户',
          phone: `139${Date.now().toString().slice(-8)}`,
          userType: this.data.form.userType
        }
      })
      wx.showToast({ title: '已自动注册，请登录', icon: 'none' })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  }
})
