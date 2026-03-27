Page({
  data: {
    orderId: '',
    ok: false,
    message: ''
  },
  onLoad(options) {
    const ok = String((options && options.ok) || '') === '1'
    this.setData({
      orderId: (options && options.orderId) || '',
      ok,
      message: decodeURIComponent((options && options.message) || (ok ? '支付成功' : '订单已取消'))
    })
  },
  goOrders() {
    wx.switchTab({ url: '/pages/orders/index' })
  }
})
