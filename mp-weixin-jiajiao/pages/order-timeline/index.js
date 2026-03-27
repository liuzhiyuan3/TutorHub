const { request } = require('../../utils/request')
const { ensureLogin } = require('../../utils/auth-guard')

Page({
  data: {
    id: '',
    loading: false,
    error: '',
    list: [],
    authSheetVisible: false,
    authSheetRole: ''
  },
  onLoad(options) {
    const id = (options && options.id) || ''
    this.setData({ id })
  },
  onShow() {
    if (!ensureLogin('查看订单轨迹需要登录，是否前往登录？')) return
    this.load()
  },
  async load() {
    if (!this.data.id) {
      this.setData({ error: '缺少订单ID' })
      return
    }
    this.setData({ loading: true, error: '' })
    try {
      const list = await request({ url: `/api/order/${this.data.id}/timeline`, authMode: 'required' })
      this.setData({ loading: false, list: list || [] })
    } catch (e) {
      this.setData({ loading: false, error: e.message || '轨迹加载失败' })
    }
  },
  openAuthSheet(role) {
    this.setData({ authSheetVisible: true, authSheetRole: role || '' })
  },
  onAuthSheetClose() {
    this.setData({ authSheetVisible: false, authSheetRole: '' })
  },
  onAuthSheetSuccess() {
    this.setData({ authSheetVisible: false, authSheetRole: '' })
    this.load()
  }
})
