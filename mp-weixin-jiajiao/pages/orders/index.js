const { request } = require('../../utils/request')
const { getLoginState, getRoleMode, ensureLogin } = require('../../utils/auth-guard')

Page({
  data: {
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    list: [],
    guestMode: false,
    roleMode: 'guest',
    loading: false,
    error: '',
    statusMap: ['待确认', '进行中', '已完成', '已取消'],
    stageMap: {
      PUBLISHED_WAITING: '等待接单',
      MATCHED: '已被承接',
      IN_SERVICE: '服务中',
      DONE: '已完成',
      CLOSED: '已关闭'
    },
    authSheetVisible: false,
    authSheetRole: ''
  },

  normalizeTextStatus(text, fallback) {
    const raw = String(text || '').trim()
    if (!raw) return fallback
    const map = {
      WAITING: '待接单',
      WAITING_CONFIRM: '待确认',
      IN_PROGRESS: '进行中',
      FINISHED: '已完成',
      CANCELED: '已取消',
      IN_SERVICE: '服务中',
      DONE: '已完成',
      CLOSED: '已关闭',
      PAID: '已支付',
      UNPAID: '待支付'
    }
    return map[raw] || raw
  },

  onShow() {
    const state = getLoginState()
    const roleMode = getRoleMode()
    if (!state.loggedIn) {
      this.setData({ guestMode: true, roleMode: 'guest', list: [], loading: false, error: '' })
      return
    }
    this.setData({ guestMode: false, roleMode })
    this.load(true)
  },

  onPullDownRefresh() {
    if (this.data.guestMode) {
      wx.stopPullDownRefresh()
      return
    }
    this.load(true).finally(() => wx.stopPullDownRefresh())
  },

  normalizeOrderItem(item, isParent) {
    if (isParent) {
      const sourceType = item.sourceType || 'ORDER'
      const orderStatus = item.orderStatus === undefined || item.orderStatus === null ? null : Number(item.orderStatus)
      const entityId = sourceType === 'ORDER' ? (item.bizId || item.id || '') : (item.requirementId || item.bizId || item.id || '')
      return {
        ...item,
        id: entityId,
        sourceType,
        stageText: this.normalizeTextStatus(item.parentOrderStageText, this.data.stageMap[item.parentOrderStage] || '等待接单'),
        orderStatus,
        displayStatusText: this.normalizeTextStatus(item.statusText, orderStatus === null ? '等待接单' : (this.data.statusMap[orderStatus] || '未知')),
        canCancelRequirement: sourceType === 'REQUIREMENT' && Number(item.requirementStatus) === 0,
        canTo3: sourceType === 'ORDER' && orderStatus !== null && [0, 1].includes(orderStatus),
        canPay: sourceType === 'ORDER' && Number(item.payStatus) !== 1 && orderStatus === 0
      }
    }

    const orderStatus = Number(item.orderStatus)
    const entityId = item.id || item.bizId || ''
    return {
      ...item,
      id: entityId,
      sourceType: 'ORDER',
      stageText: this.data.statusMap[orderStatus] || '未知',
      displayStatusText: this.normalizeTextStatus(item.statusText, this.data.statusMap[orderStatus] || '未知'),
      canTo1: this.allowUpdateByRole(this.data.roleMode, orderStatus, 1),
      canTo2: this.allowUpdateByRole(this.data.roleMode, orderStatus, 2),
      canTo3: this.allowUpdateByRole(this.data.roleMode, orderStatus, 3),
      canPay: false,
      canCancelRequirement: false
    }
  },

  async load(reset = false) {
    const nextPage = reset ? 1 : this.data.pageNo
    if (this.data.loading) return
    if (!reset && !this.data.hasMore) return
    this.setData({ loading: true, error: '' })

    try {
      const isParent = this.data.roleMode === 'parent'
      const path = isParent ? '/api/order/my/pool-page' : '/api/order/my/page'
      const data = await request({
        url: `${path}?pageNo=${nextPage}&pageSize=${this.data.pageSize}&sortBy=latest`,
        authMode: 'required'
      })

      const list = (data.records || []).map((item) => this.normalizeOrderItem(item, isParent))
      const merged = reset ? list : this.data.list.concat(list)
      const hasMore = merged.length < Number(data.total || 0)
      this.setData({
        list: merged,
        loading: false,
        pageNo: nextPage + 1,
        hasMore
      })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '订单加载失败' })
    }
  },

  onLoadMore() {
    this.load(false)
  },

  canTransit(currentStatus, nextStatus) {
    const current = Number(currentStatus)
    const next = Number(nextStatus)
    const allowMap = {
      0: [1, 3],
      1: [2, 3],
      2: [],
      3: []
    }
    return (allowMap[current] || []).includes(next)
  },

  allowUpdateByRole(roleMode, currentStatus, nextStatus) {
    if (!this.canTransit(currentStatus, nextStatus)) return false
    if (roleMode === 'teacher') return nextStatus === 1 || nextStatus === 2
    if (roleMode === 'parent') return nextStatus === 3
    return false
  },

  ensureLoginForAction(message, action, payload) {
    const state = getLoginState()
    if (!state.loggedIn) this._pendingAction = { action, payload }
    if (!ensureLogin(message)) {
      if (state.loggedIn) this._pendingAction = null
      return false
    }
    this._pendingAction = null
    return true
  },

  updateStatus(e) {
    const id = e.currentTarget.dataset.id
    const status = Number(e.currentTarget.dataset.status)
    const current = Number(e.currentTarget.dataset.current)
    if (!this.ensureLoginForAction('更新订单状态需要登录，是否前往登录？', 'updateStatusByPayload', { id, status, current })) return
    this.updateStatusByPayload({ id, status, current })
  },

  updateStatusByPayload(payload) {
    const id = payload && payload.id
    const status = Number(payload && payload.status)
    const current = Number(payload && payload.current)
    if (!id) {
      wx.showToast({ title: '订单ID缺失，请刷新重试', icon: 'none' })
      return
    }
    if (!this.allowUpdateByRole(this.data.roleMode, current, status)) {
      wx.showToast({ title: '当前状态不可执行该操作', icon: 'none' })
      return
    }
    const statusText = this.data.statusMap[status] || '目标状态'
    wx.showModal({
      title: '确认更新',
      content: `确认将订单状态改为「${statusText}」吗？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({
            url: `/api/order/${id}/status`,
            method: 'PUT',
            authMode: 'required',
            data: { orderStatus: status, orderRemark: '' }
          })
          wx.showToast({ title: '更新成功', icon: 'success' })
          await this.load(true)
        } catch (err) {
          wx.showToast({ title: err.message || '更新失败', icon: 'none' })
        }
      }
    })
  },

  cancelRequirement(e) {
    const requirementId = e.currentTarget.dataset.id
    if (!requirementId) return
    wx.showModal({
      title: '取消需求',
      content: '确认取消当前待接单需求吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ url: `/api/requirement/${requirementId}/cancel`, method: 'PUT', authMode: 'required' })
          wx.showToast({ title: '已取消', icon: 'success' })
          await this.load(true)
        } catch (err) {
          wx.showToast({ title: err.message || '取消失败', icon: 'none' })
        }
      }
    })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/index?redirect=%2Fpages%2Forders%2Findex' })
  },

  goTimeline(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({ url: `/pages/order-timeline/index?id=${id}` })
  },

  goPay(e) {
    const id = e.currentTarget.dataset.id
    const orderNumber = e.currentTarget.dataset.ordernumber || ''
    const amount = e.currentTarget.dataset.amount || 0
    if (!this.ensureLoginForAction('支付需要登录，是否前往登录？', 'goPayByPayload', { id, orderNumber, amount })) return
    this.goPayByPayload({ id, orderNumber, amount })
  },

  goPayByPayload(payload) {
    const id = payload && payload.id
    const orderNumber = (payload && payload.orderNumber) || ''
    const amount = (payload && payload.amount) || 0
    if (!id) return
    wx.navigateTo({
      url: `/pages/pay/index?orderId=${encodeURIComponent(id)}&orderNumber=${encodeURIComponent(orderNumber)}&amount=${encodeURIComponent(String(amount))}`
    })
  },

  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || '' })
  },

  onAuthSheetClose() {
    this._pendingAction = null
    this.setData({ authSheetVisible: false, authSheetRole: '' })
  },

  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false, authSheetRole: '', guestMode: false, roleMode: getRoleMode() })
    this.load(true)
    if (this._pendingAction && typeof this[this._pendingAction.action] === 'function') {
      const action = this._pendingAction
      this._pendingAction = null
      this[action.action](action.payload)
    }
  }
})
