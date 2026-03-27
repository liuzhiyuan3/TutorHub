const { request } = require('../../utils/request')

Page({
  data: {
    orderId: '',
    loading: false,
    error: '',
    detail: null
  },

  onLoad(options) {
    this.setData({ orderId: (options && options.orderId) || '' })
  },

  onShow() {
    this.load()
  },

  async load() {
    if (!this.data.orderId) {
      this.setData({ error: '缺少订单号' })
      return
    }
    this.setData({ loading: true, error: '' })
    try {
      const detail = await request({ url: `/api/dispatch/public/${encodeURIComponent(this.data.orderId)}` })
      this.setData({ loading: false, detail: detail || null })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '详情加载失败' })
    }
  },

  goLogin() {
    const redirect = encodeURIComponent(`/pages/dispatch-detail/index?orderId=${this.data.orderId}`)
    wx.navigateTo({ url: `/pages/login/index?redirect=${redirect}` })
  }
})
