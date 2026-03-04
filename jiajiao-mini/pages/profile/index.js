const { request } = require('../../utils/request')

Page({
  data: {
    user: {},
    teacher: {
      teacherIdentity: '在校大学生',
      teacherTutoringMethod: 2,
      teacherTeachingYears: 1,
      teacherSchool: '',
      teacherMajor: '',
      teacherEducation: '',
      teacherExperience: '',
      teacherSelfDescription: ''
    }
  },
  onShow() {
    this.load()
  },
  async load() {
    try {
      const user = await request({ url: '/api/user/me' })
      this.setData({ user })
      if (user.userType === 1) {
        const teacher = await request({ url: '/api/teacher/profile/me' })
        if (teacher) {
          this.setData({ teacher: { ...this.data.teacher, ...teacher } })
        }
      }
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  onTeacherInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`teacher.${key}`]: e.detail.value })
  },
  async saveTeacher() {
    try {
      await request({ url: '/api/teacher/profile', method: 'POST', data: this.data.teacher })
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.load()
    } catch (e) {
      wx.showToast({ title: e.message, icon: 'none' })
    }
  },
  logout() {
    const app = getApp()
    app.globalData.token = ''
    app.globalData.userType = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userType')
    wx.redirectTo({ url: '/pages/login/index' })
  }
})
