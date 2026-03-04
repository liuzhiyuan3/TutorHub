const { request } = require('../../utils/request')

Page({
  data: {
    subjects: [],
    regions: [],
    subjectIndex: 0,
    regionIndex: 0,
    form: {
      requirementTitle: '',
      requirementDescription: '',
      requirementGrade: '',
      requirementAddress: '',
      requirementSalary: '150',
      requirementTutoringMethod: 1
    }
  },
  onShow() {
    this.loadMeta()
  },
  async loadMeta() {
    try {
      const [subjects, regions] = await Promise.all([
        request({ url: '/api/content/subjects' }),
        request({ url: '/api/content/regions' })
      ])
      this.setData({ subjects, regions })
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  onInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },
  onSubjectChange(e) {
    this.setData({ subjectIndex: Number(e.detail.value) })
  },
  onRegionChange(e) {
    this.setData({ regionIndex: Number(e.detail.value) })
  },
  async submit() {
    const subject = this.data.subjects[this.data.subjectIndex]
    const region = this.data.regions[this.data.regionIndex]
    if (!subject || !region) {
      wx.showToast({ title: '请先选择学科和区域', icon: 'none' })
      return
    }
    try {
      await request({
        url: '/api/requirement',
        method: 'POST',
        data: {
          ...this.data.form,
          subjectId: subject.id,
          regionId: region.id,
          requirementSalary: Number(this.data.form.requirementSalary)
        }
      })
      wx.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/requirements/index' }), 600)
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  }
})
