<script setup>
import { computed, onMounted, ref } from 'vue'
import { homeFilters, pageOrders, pageRequirements, pageTeachers, statsBusiness, statsOverview, statsTrend } from '../api'
import ChartPanel from '../components/admin/ChartPanel.vue'

const rangeDays = ref(7)
const granularity = ref('day')
const compareMode = ref(false)
const loading = ref(false)
const error = ref('')

const cards = ref([
  { key: 'userTotal', title: '总用户数', value: 0 },
  { key: 'teacherTotal', title: '教员总数', value: 0 },
  { key: 'requirementTotal', title: '需求总数', value: 0 },
  { key: 'orderTotal', title: '订单总数', value: 0 }
])

const trendRows = ref([])
const regionRows = ref([])
const statusRows = ref([])
const rankRows = ref([])
const bizCards = ref([
  { key: 'teacherPendingAuditTotal', title: '待审核教员', value: 0 },
  { key: 'requirementPendingAuditTotal', title: '待审核需求', value: 0 },
  { key: 'slideEnabledTotal', title: '启用轮播图', value: 0 },
  { key: 'advertisingEnabledTotal', title: '启用广告位', value: 0 },
  { key: 'dictionaryTotal', title: '字典条目数', value: 0 },
  { key: 'roleMenuTotal', title: '角色菜单绑定', value: 0 }
])

const metricOptions = [
  { key: 'orderCount', label: '新增订单', color: '#3b82f6', area: 'rgba(59,130,246,0.18)' },
  { key: 'requirementCount', label: '新增需求', color: '#10b981', area: 'rgba(16,185,129,0.14)' }
]
const selectedMetrics = ref(['orderCount', 'requirementCount'])
const visibleCharts = ref({
  trend: true,
  status: true,
  region: true,
  rank: true
})

const granularityLabel = computed(() => {
  if (granularity.value === 'month') return '月趋势'
  if (granularity.value === 'week') return '周趋势'
  return '日趋势'
})

const selectedMetricSeries = computed(() => {
  const rows = trendRows.value || []
  const series = []
  metricOptions.forEach((metric) => {
    if (!selectedMetrics.value.includes(metric.key)) return
    series.push({
      name: metric.label,
      type: 'line',
      smooth: true,
      data: rows.map((item) => Number(item[metric.key] || 0)),
      lineStyle: { color: metric.color, width: 3 },
      itemStyle: { color: metric.color },
      areaStyle: { color: metric.area }
    })
    if (compareMode.value) {
      const compareRows = rows.map((item, index) => (index === 0 ? null : Number(rows[index - 1][metric.key] || 0)))
      series.push({
        name: `${metric.label}-环比基线`,
        type: 'line',
        smooth: true,
        data: compareRows,
        lineStyle: { color: metric.color, type: 'dashed', width: 2 },
        itemStyle: { color: metric.color, opacity: 0.6 },
        symbol: 'none'
      })
    }
  })
  return series
})

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: '#4a6488' } },
  grid: { left: 16, right: 20, top: 40, bottom: 22, containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendRows.value.map((item) => item.date),
    axisLine: { lineStyle: { color: '#cfdff5' } },
    axisLabel: { color: '#6782a7' }
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#e8f0fb' } },
    axisLabel: { color: '#6782a7' }
  },
  series: [
    ...selectedMetricSeries.value
  ]
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#4a6488' } },
  series: [
    {
      name: '订单状态',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { color: '#375174', formatter: '{b}: {d}%' },
      data: statusRows.value
    }
  ]
}))

const regionOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 16, right: 20, top: 20, bottom: 20, containLabel: true },
  xAxis: {
    type: 'category',
    data: regionRows.value.map((item) => item.name),
    axisLine: { lineStyle: { color: '#cfdff5' } },
    axisLabel: { color: '#6782a7', interval: 0, rotate: 18 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#e8f0fb' } },
    axisLabel: { color: '#6782a7' }
  },
  series: [{
    type: 'bar',
    data: regionRows.value.map((item) => item.value),
    barWidth: 18,
    itemStyle: {
      borderRadius: [8, 8, 0, 0],
      color: '#60a5fa'
    }
  }]
}))

const rankOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 90, right: 16, top: 12, bottom: 12, containLabel: true },
  xAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#e8f0fb' } },
    axisLabel: { color: '#6782a7' }
  },
  yAxis: {
    type: 'category',
    inverse: true,
    data: rankRows.value.map((item) => item.name),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#4a6488' }
  },
  series: [{
    type: 'bar',
    data: rankRows.value.map((item) => item.value),
    barWidth: 14,
    itemStyle: { borderRadius: [0, 8, 8, 0], color: '#34d399' }
  }]
}))

const lineEmpty = computed(() => trendRows.value.length === 0 || selectedMetricSeries.value.length === 0)
const statusEmpty = computed(() => statusRows.value.every((item) => Number(item.value || 0) === 0))
const regionEmpty = computed(() => regionRows.value.length === 0)
const rankEmpty = computed(() => rankRows.value.length === 0)
const totalOrders = computed(() => Number(cards.value.find((item) => item.key === 'orderTotal')?.value || 0))
const totalRequirements = computed(() => Number(cards.value.find((item) => item.key === 'requirementTotal')?.value || 0))
const completedOrders = computed(() => Number(statusRows.value.find((item) => item.name === '已完成')?.value || 0))
const completeRate = computed(() => {
  if (!totalOrders.value) return 0
  return Math.round((completedOrders.value / totalOrders.value) * 100)
})
const pendingReviewTotal = computed(() => {
  const teacherPending = Number(bizCards.value.find((item) => item.key === 'teacherPendingAuditTotal')?.value || 0)
  const requirementPending = Number(bizCards.value.find((item) => item.key === 'requirementPendingAuditTotal')?.value || 0)
  return teacherPending + requirementPending
})

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildStatusDistribution(orderRecords) {
  const map = [
    { key: 0, name: '待确认', color: '#f59e0b' },
    { key: 1, name: '进行中', color: '#3b82f6' },
    { key: 2, name: '已完成', color: '#10b981' },
    { key: 3, name: '已取消', color: '#ef4444' }
  ]
  return map.map((row) => ({
    name: row.name,
    value: orderRecords.filter((item) => Number(item.orderStatus) === row.key).length,
    itemStyle: { color: row.color }
  }))
}

function buildSubjectRank(requirements, subjectOptions) {
  const nameMap = {}
  ;(subjectOptions || []).forEach((item) => {
    nameMap[item.id] = item.name
  })
  const counter = {}
  ;(requirements || []).forEach((item) => {
    if (!item.subjectId) return
    counter[item.subjectId] = (counter[item.subjectId] || 0) + 1
  })
  return Object.entries(counter)
    .map(([subjectId, count]) => ({ name: nameMap[subjectId] || '未命名学科', value: count }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)
}

function normalizeTrendRows(trend) {
  const orderTrendMap = {}
  const requirementTrendMap = {}
  ;(trend?.orderTrend || []).forEach((item) => {
    orderTrendMap[item.date] = Number(item.value || 0)
  })
  ;(trend?.requirementTrend || []).forEach((item) => {
    requirementTrendMap[item.date] = Number(item.value || 0)
  })
  const labels = Array.from(new Set([
    ...Object.keys(orderTrendMap),
    ...Object.keys(requirementTrendMap)
  ])).sort()
  return labels.map((label) => ({
    date: label,
    orderCount: Number(orderTrendMap[label] || 0),
    requirementCount: Number(requirementTrendMap[label] || 0)
  }))
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - (rangeDays.value - 1))
    const startDateText = formatLocalDate(startDate)
    const endDateText = formatLocalDate(endDate)

    const [overview, business, trend, teacherPage, requirementPage, orderPage, filters] = await Promise.all([
      statsOverview(),
      statsBusiness(),
      statsTrend({ startDate: startDateText, endDate: endDateText, granularity: granularity.value }),
      pageTeachers({ pageNo: 1, pageSize: 200 }),
      pageRequirements({ pageNo: 1, pageSize: 500 }),
      pageOrders({ pageNo: 1, pageSize: 500 }),
      homeFilters()
    ])

    cards.value = cards.value.map((card) => {
      if (card.key === 'teacherTotal') {
        return { ...card, value: Number(teacherPage.total || teacherPage.records?.length || 0) }
      }
      return { ...card, value: Number(overview[card.key] || 0) }
    })

    trendRows.value = normalizeTrendRows(trend)

    regionRows.value = (trend?.regionDistribution || []).map((item) => ({
      name: item.regionName || '未命名',
      value: Number(item.requirementCount || 0)
    }))
    statusRows.value = buildStatusDistribution(orderPage.records || [])
    rankRows.value = buildSubjectRank(requirementPage.records || [], filters?.subjects || [])
    bizCards.value = bizCards.value.map((item) => ({
      ...item,
      value: Number((business && business[item.key]) || 0)
    }))
  } catch (e) {
    error.value = e.message || '看板数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">运营总览</h3>
    <p class="section-subtitle">管理端决策入口：先看核心指标，再看趋势和分布</p>

    <div class="overview-strip panel-mt-14">
      <div class="overview-item">
        <span class="overview-label">需求转订单规模</span>
        <strong class="overview-value">{{ totalRequirements }} / {{ totalOrders }}</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">订单完成率</span>
        <strong class="overview-value">{{ completeRate }}%</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">待处理审核量</span>
        <strong class="overview-value">{{ pendingReviewTotal }}</strong>
      </div>
    </div>

    <div class="panel panel-mt-12">
      <div class="panel-head">
        <strong>总览筛选</strong>
        <span class="table-meta">默认展示最近 {{ rangeDays }} 天（{{ granularityLabel }}）</span>
      </div>
      <div class="panel-body">
        <div class="dashboard-toolbar dashboard-toolbar-primary">
          <select v-model.number="rangeDays" class="select">
            <option :value="7">近7天</option>
            <option :value="15">近15天</option>
            <option :value="30">近30天</option>
          </select>
          <select v-model="granularity" class="select">
            <option value="day">日趋势</option>
            <option value="week">周趋势</option>
            <option value="month">月趋势</option>
          </select>
          <button class="btn" :disabled="loading" @click="load">刷新看板</button>
        </div>
        <div class="dashboard-toolbar dashboard-toolbar-secondary">
          <div class="control-group">
            <span class="control-label">趋势指标（常用）</span>
            <label v-for="item in metricOptions" :key="item.key" class="control-item">
              <input v-model="selectedMetrics" type="checkbox" :value="item.key" />
              <span>{{ item.label }}</span>
            </label>
          </div>
          <div class="control-group">
            <span class="control-label">高级显示</span>
            <label class="control-item">
              <input v-model="compareMode" type="checkbox" />
              <span>显示环比基线</span>
            </label>
            <label class="control-item"><input v-model="visibleCharts.trend" type="checkbox" /> <span>业务趋势</span></label>
            <label class="control-item"><input v-model="visibleCharts.status" type="checkbox" /> <span>订单状态</span></label>
            <label class="control-item"><input v-model="visibleCharts.region" type="checkbox" /> <span>区域分布</span></label>
            <label class="control-item"><input v-model="visibleCharts.rank" type="checkbox" /> <span>学科排行</span></label>
          </div>
        </div>
      </div>
    </div>

    <div v-if="error" class="page-feedback error">
      加载失败：{{ error }}
      <button class="btn ghost ml-8" @click="load">重试</button>
    </div>

    <div class="metric-grid panel-mt-14">
      <div v-for="item in cards" :key="item.key" class="metric-card">
        <div class="metric-title">{{ item.title }}</div>
        <div class="metric-value">{{ loading ? '...' : item.value }}</div>
      </div>
    </div>

    <div class="panel panel-mt-12">
      <div class="panel-head">
        <strong>管理端统计</strong>
      </div>
      <div class="panel-body">
        <div class="metric-grid metric-grid-compact">
          <div v-for="item in bizCards" :key="item.key" class="metric-card metric-card-compact">
            <div class="metric-title">{{ item.title }}</div>
            <div class="metric-value metric-value-compact">{{ loading ? '...' : item.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-grid-main">
      <ChartPanel
        v-if="visibleCharts.trend"
        title="业务趋势"
        :subtitle="`核心趋势（${granularityLabel}）`"
        :loading="loading"
        :option="lineOption"
        :height="320"
        :empty="lineEmpty"
        empty-text="暂无趋势数据"
      />
    </div>

    <div class="charts-grid-secondary">
      <ChartPanel
        v-if="visibleCharts.status"
        title="订单状态分布"
        subtitle="待确认 / 进行中 / 完成 / 取消"
        :loading="loading"
        :option="statusOption"
        :height="320"
        :empty="statusEmpty"
        empty-text="暂无订单状态数据"
      />
      <ChartPanel
        v-if="visibleCharts.region"
        title="区域需求分布"
        subtitle="按区域统计需求数量"
        :loading="loading"
        :option="regionOption"
        :height="300"
        :empty="regionEmpty"
        empty-text="暂无区域需求数据"
      />
      <ChartPanel
        v-if="visibleCharts.rank"
        title="学科需求排行"
        subtitle="Top 8 热门学科需求"
        :loading="loading"
        :option="rankOption"
        :height="300"
        :empty="rankEmpty"
        empty-text="暂无学科排行数据"
      />
    </div>
  </div>
</template>

<style scoped>
.overview-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.overview-item {
  background: linear-gradient(180deg, #f9fcff, #f2f8ff);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.overview-label {
  color: var(--text-sub);
  font-size: 12px;
}
.overview-value {
  font-size: 24px;
  color: var(--text-main);
  line-height: 1.1;
}
.dashboard-toolbar {
  display: grid;
  gap: 10px;
}
.dashboard-toolbar-primary {
  grid-template-columns: 1fr 1fr auto;
}
.dashboard-toolbar-secondary {
  margin-top: 10px;
  grid-template-columns: 1.4fr 1.8fr;
}
.control-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.control-label {
  font-size: 12px;
  color: var(--text-sub);
}
.control-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-main);
}
.charts-grid-main {
  margin-top: 14px;
}
.charts-grid-secondary {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
@media (max-width: 1200px) {
  .overview-strip,
  .dashboard-toolbar-primary,
  .dashboard-toolbar-secondary {
    grid-template-columns: 1fr;
  }
  .charts-grid-secondary {
    grid-template-columns: 1fr;
  }
}
.metric-grid-compact {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}
.metric-card-compact {
  padding: 12px;
}
.metric-value-compact {
  font-size: 21px;
  margin-top: 6px;
}
@media (max-width: 1400px) {
  .metric-grid-compact {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 900px) {
  .metric-grid-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>



