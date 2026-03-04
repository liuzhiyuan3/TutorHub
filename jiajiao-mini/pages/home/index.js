Page({
  onShow() {
    const app = getApp()
    if (!app.globalData.token) {
      wx.redirectTo({ url: '/pages/login/index' })
    }
  },
  goTeachers() {
    wx.navigateTo({ url: '/pages/teachers/index' })
  },
  goRequirementCreate() {
    wx.navigateTo({ url: '/pages/requirement-create/index' })
  },
  goRequirements() {
    wx.switchTab({ url: '/pages/requirements/index' })
  },
  goOrders() {
    wx.switchTab({ url: '/pages/orders/index' })
  }
})
