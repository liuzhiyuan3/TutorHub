const authService = require('../../utils/auth-service')

Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    role: {
      type: String,
      value: ''
    },
    message: {
      type: String,
      value: ''
    }
  },
  data: {
    userType: 0,
    submitting: false
  },
  observers: {
    role(next) {
      if (next === 'teacher') {
        this.setData({ userType: 1 })
      } else if (next === 'parent') {
        this.setData({ userType: 0 })
      }
    }
  },
  methods: {
    noop() {},
    close() {
      this.triggerEvent('close')
    },
    chooseParent() {
      if (this.data.role) return
      this.setData({ userType: 0 })
    },
    chooseTeacher() {
      if (this.data.role) return
      this.setData({ userType: 1 })
    },
    async onGetPhoneNumber(e) {
      if (this.data.submitting) return
      const detail = e.detail || {}
      if (!String(detail.errMsg || '').includes(':ok')) {
        wx.showToast({ title: '你已取消手机号授权', icon: 'none' })
        return
      }
      const phoneCode = detail.code
      if (!phoneCode) {
        wx.showToast({ title: '手机号授权失败，请重试', icon: 'none' })
        return
      }
      this.setData({ submitting: true })
      try {
        const loginRes = await new Promise((resolve, reject) => {
          wx.login({
            success: resolve,
            fail: reject
          })
        })
        const code = loginRes && loginRes.code
        if (!code) {
          throw new Error('微信登录失败：未获取到登录code')
        }
        const data = await authService.wechatPhoneLogin({
          code,
          phoneCode,
          userType: this.data.userType
        })
        this.triggerEvent('success', {
          userType: data.userType,
          openid: data.openid || '',
          userName: data.userName || ''
        })
      } catch (err) {
        wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      } finally {
        this.setData({ submitting: false })
      }
    }
  }
})
