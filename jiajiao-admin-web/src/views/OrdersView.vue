<script setup>
import { onMounted, ref } from 'vue'
import { auditOrder, getTeacherProfile, getUserProfile, pageOrders, updateOrderStatus } from '../api'
import DrawerPanel from '../components/admin/DrawerPanel.vue'
import StatusTag from '../components/admin/StatusTag.vue'

const list = ref([])
const status = ref('')
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const detailVisible = ref(false)
const current = ref(null)
const loading = ref(false)
const error = ref('')
const feedback = ref({ type: '', text: '' })
const profileLoading = ref(false)
const parentProfile = ref(null)
const teacherProfile = ref(null)

function canTransitOrder(currentStatus, nextStatus) {
  const current = Number(currentStatus)
  const next = Number(nextStatus)
  if (Number.isNaN(current) || Number.isNaN(next) || current === next) return false
  if (current === 0) return next === 1 || next === 3
  if (current === 1) return next === 2 || next === 3
  return false
}

function canUpdateStatus(item, targetStatus) {
  return canTransitOrder(item?.orderStatus, targetStatus)
}

function canAudit(item) {
  return Number(item?.orderAuditStatus ?? 0) === 0
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageOrders({ pageNo: pageNo.value, pageSize: pageSize.value, status: status.value || undefined })
    list.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (e) {
    error.value = e.message || '订单列表加载失败'
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  load()
}

function totalPages() {
  return Math.max(1, Math.ceil(total.value / pageSize.value))
}

function canPrevPage() {
  return pageNo.value > 1
}

function canNextPage() {
  return pageNo.value < totalPages()
}

function goPrevPage() {
  if (!canPrevPage() || loading.value) return
  pageNo.value -= 1
  load()
}

function goNextPage() {
  if (!canNextPage() || loading.value) return
  pageNo.value += 1
  load()
}

function emptyTip() {
  if (!status.value) return '当前暂无订单数据'
  const map = { 0: '暂无待确认订单', 1: '暂无进行中订单', 2: '暂无已完成订单', 3: '暂无已取消订单' }
  return map[Number(status.value)] || '当前筛选条件下暂无订单'
}

async function updateStatus(id, status) {
  try {
    await updateOrderStatus(id, { orderStatus: status, orderRemark: '' })
    feedback.value = { type: 'success', text: '订单状态更新成功' }
    await load()
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '订单状态更新失败' }
  }
}

async function doAudit(id, auditStatus) {
  try {
    await auditOrder(id, { auditStatus, reason: auditStatus === 2 ? '审核不通过' : '' })
    feedback.value = { type: 'success', text: auditStatus === 1 ? '订单审核通过' : '订单审核拒绝' }
    await load()
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '订单审核失败' }
  }
}

async function showDetail(item) {
  current.value = item
  detailVisible.value = true
  profileLoading.value = true
  parentProfile.value = null
  teacherProfile.value = null
  try {
    parentProfile.value = await getUserProfile(item.parentId)
    teacherProfile.value = await getTeacherProfile(item.teacherId)
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '详情数据加载失败' }
  } finally {
    profileLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <div class="page-heading">
      <h3 class="section-title">订单管理</h3>
      <p class="section-subtitle">管理订单状态流转与审核，聚焦高优先级订单处理</p>
      <div class="hint-chip-row">
        <span class="hint-chip">总数 {{ total }}</span>
        <span class="hint-chip">当前页 {{ pageNo }}/{{ totalPages() }}</span>
      </div>
    </div>
    <div class="panel panel-mt-14">
      <div class="panel-head">
        <strong>筛选条件</strong>
      </div>
      <div class="panel-body">
        <div class="toolbar">
          <select v-model="status" class="select col-span-2">
            <option value="">全部状态</option>
            <option value="0">待确认</option>
            <option value="1">进行中</option>
            <option value="2">已完成</option>
            <option value="3">已取消</option>
          </select>
          <button class="btn col-span-1" @click="handleSearch">筛选</button>
        </div>
      </div>
    </div>
    <div v-if="error" class="page-feedback error">
      加载失败：{{ error }}
      <button class="btn ghost ml-8" @click="load">重试</button>
    </div>
    <div v-if="feedback.text" :class="['page-feedback', feedback.type === 'error' ? 'error' : '']">
      {{ feedback.text }}
    </div>
    <div class="panel panel-mt-14">
      <div class="panel-head">
        <strong>订单列表</strong>
        <span class="table-meta">共 {{ total }} 条</span>
      </div>
      <div class="panel-body panel-body-pt-4">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>订单号</th><th>金额</th><th>状态</th><th>审核</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-if="loading"><td colspan="5"><span class="skeleton" /></td></tr>
              <tr v-for="item in list" :key="item.id">
                <td>{{ item.orderNumber }}</td>
                <td>{{ item.orderAmount }}</td>
                <td>
                  <StatusTag
                    :type="item.orderStatus === 2 ? 'success' : item.orderStatus === 3 ? 'error' : 'pending'"
                    :text="['待确认', '进行中', '已完成', '已取消'][item.orderStatus || 0]"
                  />
                </td>
                <td>
                  <StatusTag
                    :type="item.orderAuditStatus === 1 ? 'success' : item.orderAuditStatus === 2 ? 'error' : 'pending'"
                    :text="['待审', '通过', '拒绝'][item.orderAuditStatus || 0]"
                  />
                </td>
                <td class="action-group action-group--table">
                  <button class="btn ghost sm" @click="showDetail(item)">详情</button>
                  <button class="btn sm" :disabled="!canUpdateStatus(item, 1)" @click="updateStatus(item.id, 1)">进行中</button>
                  <button class="btn secondary sm" :disabled="!canUpdateStatus(item, 2)" @click="updateStatus(item.id, 2)">完成</button>
                  <button class="btn secondary sm" :disabled="!canUpdateStatus(item, 3)" @click="updateStatus(item.id, 3)">取消</button>
                  <button class="btn sm" :disabled="!canAudit(item)" @click="doAudit(item.id, 1)">审核通过</button>
                  <button class="btn secondary sm" :disabled="!canAudit(item)" @click="doAudit(item.id, 2)">审核拒绝</button>
                </td>
              </tr>
              <tr v-if="!loading && !list.length && !error">
                <td colspan="5"><div class="empty-state">{{ emptyTip() }}</div></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pager-row">
          <span class="table-meta">第 {{ pageNo }} / {{ totalPages() }} 页</span>
          <div class="pager-actions">
            <button class="btn ghost" :disabled="!canPrevPage() || loading" @click="goPrevPage">上一页</button>
            <button class="btn ghost" :disabled="!canNextPage() || loading" @click="goNextPage">下一页</button>
          </div>
        </div>
      </div>
    </div>
    <DrawerPanel :visible="detailVisible" title="订单详情" @close="detailVisible = false">
      <div v-if="current" class="detail-grid">
        <div><strong>订单号：</strong>{{ current.orderNumber }}</div>
        <div><strong>金额：</strong>{{ current.orderAmount }}</div>
        <div><strong>需求ID：</strong>{{ current.requirementId }}</div>
        <div><strong>家长ID：</strong>{{ current.parentId }}</div>
        <div><strong>教员ID：</strong>{{ current.teacherId }}</div>
        <div><strong>备注：</strong>{{ current.orderRemark || '-' }}</div>
        <div v-if="profileLoading" class="skeleton h-60" />
        <template v-else>
          <div class="panel">
            <div class="panel-body">
              <h4 class="section-title">家长简介摘要</h4>
              <div><strong>姓名：</strong>{{ parentProfile?.user?.userName || '-' }}</div>
              <div><strong>手机号：</strong>{{ parentProfile?.user?.userPhone || '-' }}</div>
              <div><strong>历史需求数：</strong>{{ (parentProfile?.requirements || []).length }}</div>
              <div><strong>历史订单数：</strong>{{ (parentProfile?.parentOrders || []).length }}</div>
            </div>
          </div>
          <div class="panel">
            <div class="panel-body">
              <h4 class="section-title">教员简介摘要</h4>
              <div><strong>姓名：</strong>{{ teacherProfile?.user?.userName || '-' }}</div>
              <div><strong>学校：</strong>{{ teacherProfile?.teacherInfo?.teacherSchool || '-' }}</div>
              <div><strong>专业：</strong>{{ teacherProfile?.teacherInfo?.teacherMajor || '-' }}</div>
              <div><strong>教龄：</strong>{{ teacherProfile?.teacherInfo?.teacherTeachingYears || 0 }} 年</div>
            </div>
          </div>
        </template>
      </div>
    </DrawerPanel>
  </div>
</template>

<style scoped>
.page-heading {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.hint-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.hint-chip {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--line);
  background: var(--bg-soft);
  color: var(--text-sub);
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
}
.detail-grid {
  display: grid;
  gap: 10px;
}
.pager-row {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager-actions {
  display: flex;
  gap: 8px;
}
.action-group--table {
  gap: 6px;
}
</style>



