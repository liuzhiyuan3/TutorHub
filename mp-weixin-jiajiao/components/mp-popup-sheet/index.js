Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    title: {
      type: String,
      value: "操作"
    }
  },
  methods: {
    close() {
      this.triggerEvent("close")
    },
    noop() {}
  }
})
