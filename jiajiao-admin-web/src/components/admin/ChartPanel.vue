<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  option: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  height: { type: Number, default: 320 },
  empty: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据' }
})

const chartEl = ref(null)
let chart = null
let resizeObserver = null

function ensureChart() {
  if (!chartEl.value) return
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption(props.option || {}, true)
}

function resize() {
  if (chart) chart.resize()
}

onMounted(() => {
  ensureChart()
  resizeObserver = new ResizeObserver(() => resize())
  if (chartEl.value) resizeObserver.observe(chartEl.value)
  window.addEventListener('resize', resize)
})

watch(() => props.option, () => {
  ensureChart()
}, { deep: true })

onBeforeUnmount(() => {
  if (resizeObserver && chartEl.value) {
    resizeObserver.unobserve(chartEl.value)
  }
  resizeObserver = null
  window.removeEventListener('resize', resize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<template>
  <div class="panel chart-panel">
    <div class="panel-head">
      <div>
        <strong>{{ title }}</strong>
        <p v-if="subtitle" class="chart-subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div class="panel-body">
      <div v-if="loading" class="skeleton chart-skeleton" />
      <div v-else-if="empty" class="chart-empty" :style="{ height: `${height}px` }">{{ emptyText }}</div>
      <div v-else ref="chartEl" :style="{ height: `${height}px` }" />
    </div>
  </div>
</template>

<style scoped>
.chart-panel {
  height: 100%;
}
.chart-subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--text-sub);
}
.chart-skeleton {
  height: 280px;
}
.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  background: linear-gradient(180deg, #fbfdff, #f5f9ff);
  border: 1px dashed var(--line-strong);
  border-radius: 10px;
}
</style>
