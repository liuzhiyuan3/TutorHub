const { request } = require('../../utils/request')

Page({
  data: { list: [] },
  onShow() {
    this.load()
  },
  async load() {
    try {
      const data = await request({ url: '/api/requirement/page?pageNo=1&pageSize=20' })
      this.setData({ list: data.records || [] })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  async receive(e) {
    try {
      await request({
        url: `/api/order/receive/${e.currentTarget.dataset.id}`,
        method: 'POST'
      })
      wx.showToast({ title: '接单成功', icon: 'success' })
      this.load()
    } catch (err) {
      wx.showToast({ title: err.message, icon: 'none' })
    }
  }
})
