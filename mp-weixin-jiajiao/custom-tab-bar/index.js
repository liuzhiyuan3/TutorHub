Component({
  data: {
    selected: 0,
    color: '#6f839f',
    selectedColor: '#2f7cf6',
    list: [
      { pagePath: 'pages/home/index', text: '首页', icon: 'i-home' },
      { pagePath: 'pages/requirements/index', text: '需求', icon: 'i-requirement' },
      { pagePath: 'pages/orders/index', text: '订单', icon: 'i-order' },
      { pagePath: 'pages/profile/index', text: '我的', icon: 'i-profile' }
    ]
  },
  lifetimes: {
    attached() {
      this.syncSelected()
    }
  },
  pageLifetimes: {
    show() {
      this.syncSelected()
    }
  },
  methods: {
    syncSelected() {
      const pages = getCurrentPages()
      if (!pages.length) return
      const route = pages[pages.length - 1].route
      const selected = this.data.list.findIndex((item) => item.pagePath === route)
      if (selected >= 0 && selected !== this.data.selected) {
        this.setData({ selected })
      }
    },
    onSwitchTab(e) {
      const index = Number(e.currentTarget.dataset.index)
      if (Number.isNaN(index)) return
      const item = this.data.list[index]
      if (!item || !item.pagePath) return
      wx.switchTab({ url: '/' + item.pagePath })
    }
  }
})
