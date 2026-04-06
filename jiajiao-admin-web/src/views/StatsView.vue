<script setup>
import { onMounted, ref } from 'vue'
import { statsBusiness, statsOverview, statsTrend } from '../api'

const rangeDays = ref(7)
const granularity = ref('day')
const overview = ref({})
const trendList = ref([])
const regionList = ref([])
const business = ref({})
const loading = ref(false)
const error = ref('')

function formatLocalDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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
    const [o, b, t] = await Promise.all([
      statsOverview(),
      statsBusiness(),
      statsTrend({ startDate: startDateText, endDate: endDateText, granularity: granularity.value })
    ])
    overview.value = o || {}
    business.value = b || {}
    trendList.value = ((t?.requirementTrend || [])).map((item) => {
      const orderRow = (t?.orderTrend || []).find((x) => x.date === item.date)
      return {
        date: item.date,
        requirementCount: item.value,
        orderCount: orderRow ? orderRow.value : 0
      }
    })
    regionList.value = t?.regionDistribution || []
  } catch (e) {
    error.value = e.message || '统计数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">统计明细分析</h3>
    <p class="section-subtitle">面向数据核对与运营分析，展示趋势明细、区域分布与管理指标</p>
    <div class="panel panel-mt-12">
      <div class="panel-head">
        <strong>分析条件</strong>
        <span class="table-meta">当前 {{ rangeDays }} 天 / {{ granularity === 'day' ? '日粒度' : granularity === 'week' ? '周粒度' : '月粒度' }}</span>
      </div>
      <div class="panel-body">
        <div class="stats-toolbar">
          <select v-model.number="rangeDays" class="select">
            <option :value="7">近7天</option>
            <option :value="15">近15天</option>
            <option :value="30">近30天</option>
          </select>
          <select v-model="granularity" class="select">
            <option value="day">按日</option>
            <option value="week">按周</option>
            <option value="month">按月</option>
          </select>
          <button class="btn" :disabled="loading" @click="load">刷新明细</button>
        </div>
      </div>
    </div>

    <div v-if="error" class="card panel error-tip">
      <h4>统计数据加载失败</h4>
      <p class="text-error">{{ error }}</p>
      <button class="btn" @click="load">重试</button>
    </div>

    <div class="panel panel-mt-12">
      <div class="panel-head">
        <strong>分析摘要</strong>
      </div>
      <div class="panel-body">
        <div class="summary-grid">
          <div class="summary-item">
            <div class="summary-label">总用户数</div>
            <div class="summary-value">{{ loading ? '...' : overview.userTotal ?? 0 }}</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">总需求数</div>
            <div class="summary-value">{{ loading ? '...' : overview.requirementTotal ?? 0 }}</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">总订单数</div>
            <div class="summary-value">{{ loading ? '...' : overview.orderTotal ?? 0 }}</div>
          </div>
          <div class="summary-item">
            <div class="summary-label">待审核总量</div>
            <div class="summary-value">{{ loading ? '...' : (Number(business.teacherPendingAuditTotal || 0) + Number(business.requirementPendingAuditTotal || 0)) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="detail-grid">
      <div class="card panel">
        <h4>趋势明细</h4>
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>日期</th><th>新增需求</th><th>新增订单</th></tr></thead>
            <tbody>
              <tr v-if="loading"><td colspan="3"><span class="skeleton" /></td></tr>
              <tr v-for="item in trendList" :key="item.date">
                <td>{{ item.date }}</td>
                <td>{{ item.requirementCount ?? 0 }}</td>
                <td>{{ item.orderCount ?? 0 }}</td>
              </tr>
              <tr v-if="!loading && !trendList.length"><td colspan="3"><div class="empty-state">暂无趋势数据</div></td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card panel">
        <h4>区域分布排行</h4>
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>区域</th><th>数量</th></tr></thead>
            <tbody>
              <tr v-if="loading"><td colspan="2"><span class="skeleton" /></td></tr>
              <tr v-for="item in regionList" :key="item.regionName">
                <td>{{ item.regionName || '-' }}</td>
                <td>{{ item.requirementCount ?? 0 }}</td>
              </tr>
              <tr v-if="!loading && !regionList.length"><td colspan="2"><div class="empty-state">暂无区域分布数据</div></td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-toolbar {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  border: 1px solid var(--line);
  background: linear-gradient(180deg, #fbfdff, #f4f8ff);
  border-radius: var(--radius);
  padding: 12px;
}

.summary-label {
  font-size: 12px;
  color: var(--text-sub);
}

.summary-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 800;
  line-height: 1.1;
}

.detail-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 12px;
}

.panel {
  padding: 14px;
  margin-bottom: 12px;
}

.panel h4 {
  margin: 0 0 10px;
}

.error-tip {
  border: 1px solid #fecaca;
}

@media (max-width: 1200px) {
  .stats-toolbar,
  .summary-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>