Component({
  properties: {
    hasMore: {
      type: Boolean,
      value: false
    },
    loading: {
      type: Boolean,
      value: false
    }
  },
  methods: {
    onLoadMore() {
      if (this.data.loading || !this.data.hasMore) return
      this.triggerEvent("loadmore")
    }
  }
})
