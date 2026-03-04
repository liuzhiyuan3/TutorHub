const { request } = require('../../utils/request')

Page({
  data: {
    list: [],
    statusMap: ['待确认', '进行中', '已完成', '已取消']
  },
  onShow() {
    this.load()
  },
  async load() {
    try {
      const data = await request({ url: '/api/order/my/page?pageNo=1&pageSize=20' })
      this.setData({ list: data.records || [] })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  async updateStatus(e) {
    const id = e.currentTarget.dataset.id
    const status = Number(e.currentTarget.dataset.status)
    try {
      await request({
        url: `/api/order/${id}/status`,
        method: 'PUT',
        data: { orderStatus: status, orderRemark: '' }
      })
      this.load()
    } catch (err) {
      wx.showToast({ title: err.message, icon: 'none' })
    }
  }
})
