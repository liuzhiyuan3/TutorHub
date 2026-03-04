const { request } = require('../../utils/request')

Page({
  data: { list: [] },
  onShow() {
    this.load()
  },
  async load() {
    try {
      const data = await request({ url: '/api/teacher/page?pageNo=1&pageSize=20&auditStatus=1' })
      this.setData({ list: data.records || [] })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/teacher-detail/index?id=${e.currentTarget.dataset.id}` })
  }
})
