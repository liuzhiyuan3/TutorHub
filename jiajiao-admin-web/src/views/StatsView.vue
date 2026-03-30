<script setup>
import { onMounted, ref } from 'vue'
import { statsBusiness, statsOverview, statsTrend } from '../api'

const rangeDays = ref(7)
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
      statsTrend({ startDate: startDateText, endDate: endDateText })
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
    <h3 class="section-title">统计看板</h3>
    <p class="section-subtitle">按时间范围查看总览、趋势与区域分布</p>
    <div class="panel panel-mt-12">
      <div class="panel-head">
        <strong>筛选条件</strong>
        <span class="table-meta">当前 {{ rangeDays }} 天</span>
      </div>
      <div class="panel-body">
        <div class="toolbar toolbar-no-bottom">
          <select v-model.number="rangeDays" class="select col-span-2">
            <option :value="7">近7天</option>
            <option :value="15">近15天</option>
            <option :value="30">近30天</option>
          </select>
          <button class="btn col-span-1" :disabled="loading" @click="load">刷新</button>
        </div>
      </div>
    </div>

    <div class="cards panel-mt-12">
      <div class="card stat-card">
        <p>总用户数</p>
        <h2>{{ loading ? '...' : overview.userTotal ?? 0 }}</h2>
      </div>
      <div class="card stat-card">
        <p>总教员数</p>
        <h2>{{ loading ? '...' : overview.teacherTotal ?? 0 }}</h2>
      </div>
      <div class="card stat-card">
        <p>总需求数</p>
        <h2>{{ loading ? '...' : overview.requirementTotal ?? 0 }}</h2>
      </div>
      <div class="card stat-card">
        <p>总订单数</p>
        <h2>{{ loading ? '...' : overview.orderTotal ?? 0 }}</h2>
      </div>
    </div>
    <div class="cards cards-compact">
      <div class="card stat-card compact">
        <p>待审核教员</p>
        <h3>{{ loading ? '...' : business.teacherPendingAuditTotal ?? 0 }}</h3>
      </div>
      <div class="card stat-card compact">
        <p>待审核需求</p>
        <h3>{{ loading ? '...' : business.requirementPendingAuditTotal ?? 0 }}</h3>
      </div>
      <div class="card stat-card compact">
        <p>启用轮播图</p>
        <h3>{{ loading ? '...' : business.slideEnabledTotal ?? 0 }}</h3>
      </div>
      <div class="card stat-card compact">
        <p>启用广告位</p>
        <h3>{{ loading ? '...' : business.advertisingEnabledTotal ?? 0 }}</h3>
      </div>
    </div>

    <div v-if="error" class="card panel error-tip">
      <h4>统计数据加载失败</h4>
      <p class="text-error">{{ error }}</p>
      <button class="btn" @click="load">重试</button>
    </div>

    <div class="card panel">
      <h4>趋势数据</h4>
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
      <h4>区域分布</h4>
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
</template>

<style scoped>
.cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.cards-compact {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.stat-card {
  padding: 14px;
}
.stat-card p {
  margin: 0;
  color: #64748b;
}
.stat-card h2 {
  margin: 8px 0 0;
}
.stat-card.compact h3 {
  margin: 8px 0 0;
  font-size: 24px;
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
  .cards,
  .cards-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 760px) {
  .cards,
  .cards-compact {
    grid-template-columns: 1fr;
  }
}
</style>



