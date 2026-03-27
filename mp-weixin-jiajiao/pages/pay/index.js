const { request } = require('../../utils/request')
const { ensureRole, getLoginState } = require('../../utils/auth-guard')

function formatRemain(ms) {
  const total = Math.max(0, Math.floor(ms / 1000))
  const m = Math.floor(total / 60)
  const s = total % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

Page({
  data: {
    orderId: '',
    orderNumber: '',
    orderAmount: 0,
    payToken: '',
    expireTime: '',
    remainMs: 0,
    remainText: '00:00',
    loading: false,
    paying: false,
    timedOut: false,
    authSheetVisible: false,
    authSheetRole: 'parent'
  },
  timer: null,
  onLoad(options) {
    const orderId = (options && options.orderId) || ''
    const orderNumber = (options && options.orderNumber) || ''
    const amount = Number((options && options.amount) || 0)
    if (!orderId) {
      wx.showToast({ title: '订单参数缺失', icon: 'none' })
      return
    }
    this.setData({ orderId, orderNumber, orderAmount: amount })
    const state = getLoginState()
    if (!state.loggedIn) this._pendingCreatePay = true
    if (!ensureRole('parent', '支付仅家长可操作，是否前往登录？')) {
      if (state.loggedIn) this._pendingCreatePay = false
      return
    }
    this._pendingCreatePay = false
    this.createPay()
  },
  onUnload() {
    this.clearTimer()
  },
  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  startTimer() {
    this.clearTimer()
    this.timer = setInterval(() => {
      const remain = this.data.remainMs - 1000
      if (remain <= 0) {
        this.clearTimer()
        this.setData({ remainMs: 0, remainText: '00:00', timedOut: true })
        return
      }
      this.setData({ remainMs: remain, remainText: formatRemain(remain) })
    }, 1000)
  },
  async createPay() {
    this.setData({ loading: true, timedOut: false })
    try {
      const data = await request({
        url: '/api/pay/mock/create',
        method: 'POST',
        authMode: 'required',
        data: { orderId: this.data.orderId }
      })
      const expireMs = new Date(data.expireTime).getTime()
      const remain = Math.max(0, expireMs - Date.now())
      this.setData({
        orderNumber: data.orderNumber || this.data.orderNumber,
        orderAmount: Number(data.orderAmount || this.data.orderAmount || 0),
        payToken: data.payToken,
        expireTime: data.expireTime,
        remainMs: remain,
        remainText: formatRemain(remain),
        loading: false,
        timedOut: remain <= 0
      })
      if (remain > 0) {
        this.startTimer()
      }
    } catch (e) {
      this.setData({ loading: false })
      wx.showToast({ title: e.message || '创建支付失败', icon: 'none' })
    }
  },
  async confirmPay() {
    if (this.data.paying) return
    if (this.data.timedOut) {
      wx.showToast({ title: '支付已超时，请重新发起', icon: 'none' })
      return
    }
    this.setData({ paying: true })
    try {
      const data = await request({
        url: '/api/pay/mock/confirm',
        method: 'POST',
        authMode: 'required',
        data: {
          orderId: this.data.orderId,
          payToken: this.data.payToken
        }
      })
      wx.redirectTo({
        url: `/pages/pay-result/index?orderId=${encodeURIComponent(data.orderId)}&ok=1&message=${encodeURIComponent(data.message || '支付成功')}`
      })
    } catch (e) {
      wx.showToast({ title: e.message || '支付失败', icon: 'none' })
    } finally {
      this.setData({ paying: false })
    }
  },
  async cancelPay() {
    try {
      const data = await request({
        url: '/api/pay/mock/cancel',
        method: 'POST',
        authMode: 'required',
        data: {
          orderId: this.data.orderId,
          reason: this.data.timedOut ? '支付超时自动取消' : '用户主动取消支付'
        }
      })
      wx.redirectTo({
        url: `/pages/pay-result/index?orderId=${encodeURIComponent(data.orderId)}&ok=0&message=${encodeURIComponent(data.message || '订单已取消')}`
      })
    } catch (e) {
      wx.showToast({ title: e.message || '取消失败', icon: 'none' })
    }
  },
  onRetryPay() {
    this.createPay()
  },
  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || 'parent' })
  },
  onAuthSheetClose() {
    this._pendingCreatePay = false
    this.setData({ authSheetVisible: false })
  },
  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false })
    if (this._pendingCreatePay) {
      this._pendingCreatePay = false
      this.createPay()
    }
  }
})
