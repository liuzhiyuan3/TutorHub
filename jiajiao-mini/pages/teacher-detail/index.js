const { request } = require('../../utils/request')

Page({
  data: { teacher: null, id: '' },
  onLoad(options) {
    this.setData({ id: options.id || '' })
  },
  onShow() {
    this.load()
  },
  async load() {
    try {
      const data = await request({ url: '/api/teacher/page?pageNo=1&pageSize=100' })
      const teacher = (data.records || []).find((x) => x.id === this.data.id)
      this.setData({ teacher: teacher || null })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  }
})
