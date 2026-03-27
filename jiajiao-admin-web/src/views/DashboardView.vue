<script setup>
import { computed, onMounted, ref } from 'vue'
import { homeFilters, pageOrders, pageRequirements, pageTeachers, statsBusiness, statsOverview, statsTrend } from '../api'
import ChartPanel from '../components/admin/ChartPanel.vue'

const rangeDays = ref(7)
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
    {
      name: '新增订单',
      type: 'line',
      smooth: true,
      data: trendRows.value.map((item) => item.orderCount),
      lineStyle: { color: '#3b82f6', width: 3 },
      itemStyle: { color: '#3b82f6' },
      areaStyle: { color: 'rgba(59,130,246,0.18)' }
    },
    {
      name: '新增需求',
      type: 'line',
      smooth: true,
      data: trendRows.value.map((item) => item.requirementCount),
      lineStyle: { color: '#10b981', width: 3 },
      itemStyle: { color: '#10b981' },
      areaStyle: { color: 'rgba(16,185,129,0.14)' }
    }
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

const lineEmpty = computed(() => trendRows.value.length === 0)
const statusEmpty = computed(() => statusRows.value.every((item) => Number(item.value || 0) === 0))
const regionEmpty = computed(() => regionRows.value.length === 0)
const rankEmpty = computed(() => rankRows.value.length === 0)

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
      statsTrend({ startDate: startDateText, endDate: endDateText }),
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

    const orderTrendMap = {}
    ;(trend?.orderTrend || []).forEach((item) => {
      orderTrendMap[item.date] = Number(item.value || 0)
    })
    trendRows.value = (trend?.requirementTrend || []).map((item) => ({
      date: item.date,
      requirementCount: Number(item.value || 0),
      orderCount: Number(orderTrendMap[item.date] || 0)
    }))

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
    <h3 class="section-title">运营概览</h3>
    <p class="section-subtitle">总览卡片、趋势、分布与排行统一展示，支持按时间维度快速巡检</p>

    <div class="panel panel-mt-14">
      <div class="panel-head">
        <strong>时间范围</strong>
      </div>
      <div class="panel-body">
        <div class="toolbar toolbar-no-bottom">
          <select v-model.number="rangeDays" class="select col-span-2">
            <option :value="7">近7天</option>
            <option :value="15">近15天</option>
            <option :value="30">近30天</option>
          </select>
          <button class="btn col-span-1" :disabled="loading" @click="load">刷新看板</button>
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

    <div class="charts-grid">
      <ChartPanel
        title="业务趋势"
        subtitle="新增订单 / 新增需求（日趋势）"
        :loading="loading"
        :option="lineOption"
        :height="320"
        :empty="lineEmpty"
        empty-text="暂无趋势数据"
      />
      <ChartPanel
        title="订单状态分布"
        subtitle="待确认 / 进行中 / 完成 / 取消"
        :loading="loading"
        :option="statusOption"
        :height="320"
        :empty="statusEmpty"
        empty-text="暂无订单状态数据"
      />
      <ChartPanel
        title="区域需求分布"
        subtitle="按区域统计需求数量"
        :loading="loading"
        :option="regionOption"
        :height="300"
        :empty="regionEmpty"
        empty-text="暂无区域需求数据"
      />
      <ChartPanel
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
.charts-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}
@media (max-width: 1200px) {
  .charts-grid {
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
  font-size: 22px;
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



