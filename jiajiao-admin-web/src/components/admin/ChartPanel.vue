<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

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
let echartsModule = null
const chartLoadError = ref('')

async function loadEcharts() {
  if (echartsModule) return echartsModule
  const mod = await import('../../utils/echarts-lite')
  echartsModule = mod
  return echartsModule
}

async function ensureChart() {
  if (props.loading || props.empty) return
  await nextTick()
  if (!chartEl.value) return
  try {
    const echarts = await loadEcharts()
    if (!chartEl.value) return
    if (!chart) chart = echarts.init(chartEl.value)
    chart.setOption(props.option || {}, true)
    chartLoadError.value = ''
  } catch (e) {
    chartLoadError.value = '图表组件加载失败，请刷新重试'
  }
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
}, { deep: true, flush: 'post' })

watch(() => [props.loading, props.empty], () => {
  ensureChart()
}, { flush: 'post' })

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
      <div class="chart-title-wrap">
        <strong class="chart-title">{{ title }}</strong>
        <p v-if="subtitle" class="chart-subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div class="panel-body">
      <div v-if="loading" class="skeleton chart-skeleton" />
      <div v-else-if="chartLoadError" class="chart-empty chart-error" :style="{ height: `${height}px` }">{{ chartLoadError }}</div>
      <div v-else-if="empty" class="chart-empty" :style="{ height: `${height}px` }">{{ emptyText }}</div>
      <div v-else ref="chartEl" :style="{ height: `${height}px` }" />
    </div>
  </div>
</template>

<style scoped>
.chart-panel {
  height: 100%;
}
.chart-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.chart-title {
  color: var(--text-main);
}
.chart-subtitle {
  margin: 0;
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
  background: #f8fbff;
  border: 1px dashed var(--line-strong);
  border-radius: 8px;
}
.chart-error {
  color: var(--danger);
}
</style>
