<script setup>
import { onMounted, ref } from 'vue'
import { auditRequirement, getTeacherProfile, getUserProfile, pageOrders, pageRequirements } from '../api'
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
const relationLoading = ref(false)
const parentProfile = ref(null)
const teacherProfile = ref(null)

function canAudit(item) {
  return Number(item?.requirementAuditStatus ?? 0) === 0
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageRequirements({ pageNo: pageNo.value, pageSize: pageSize.value, status: status.value || undefined })
    list.value = data.records || []
    total.value = Number(data.total || 0)
  } catch (e) {
    error.value = e.message || '需求列表加载失败'
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
  if (!status.value) return '当前暂无需求数据'
  const map = { 0: '暂无待接单需求', 1: '暂无已接单需求', 2: '暂无已完成需求', 3: '暂无已取消需求' }
  return map[Number(status.value)] || '当前筛选条件下暂无需求'
}

async function doAudit(item, auditStatus) {
  try {
    await auditRequirement(item.id, { auditStatus, reason: auditStatus === 2 ? '审核不通过' : '' })
    feedback.value = { type: 'success', text: auditStatus === 1 ? '审核通过成功' : '审核拒绝成功' }
    await load()
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '审核操作失败' }
  }
}

async function showDetail(item) {
  current.value = item
  detailVisible.value = true
  relationLoading.value = true
  parentProfile.value = null
  teacherProfile.value = null
  try {
    parentProfile.value = await getUserProfile(item.parentId)
    const orderPage = await pageOrders({ pageNo: 1, pageSize: 200 })
    const matchedOrder = (orderPage.records || []).find((od) => od.requirementId === item.id)
    if (matchedOrder?.teacherId) {
      teacherProfile.value = await getTeacherProfile(matchedOrder.teacherId)
    }
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '详情数据加载失败' }
  } finally {
    relationLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <div class="page-heading">
      <h3 class="section-title">需求管理</h3>
      <p class="section-subtitle">按状态筛选并审核需求，优先处理可成交意向单</p>
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
            <option value="0">待接单</option>
            <option value="1">已接单</option>
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
        <strong>需求列表</strong>
        <span class="table-meta">共 {{ total }} 条</span>
      </div>
      <div class="panel-body panel-body-pt-4">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>标题</th><th>年级</th><th>薪资</th><th>状态</th><th>审核</th><th>地址</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-if="loading"><td colspan="7"><span class="skeleton" /></td></tr>
              <tr v-for="item in list" :key="item.id">
                <td>{{ item.requirementTitle }}</td>
                <td>{{ item.requirementGrade }}</td>
                <td>{{ item.requirementSalary }}</td>
                <td>
                  <StatusTag
                    :type="item.requirementStatus === 2 ? 'success' : item.requirementStatus === 3 ? 'error' : 'pending'"
                    :text="['待接单', '已接单', '已完成', '已取消'][item.requirementStatus || 0]"
                  />
                </td>
                <td>
                  <StatusTag
                    :type="item.requirementAuditStatus === 1 ? 'success' : item.requirementAuditStatus === 2 ? 'error' : 'pending'"
                    :text="['待审', '通过', '拒绝'][item.requirementAuditStatus || 0]"
                  />
                </td>
                <td>{{ item.requirementAddress }}</td>
                <td class="action-group action-group--table">
                  <button class="btn ghost sm" @click="showDetail(item)">详情</button>
                  <button class="btn sm" :disabled="!canAudit(item)" @click="doAudit(item, 1)">通过</button>
                  <button class="btn secondary sm" :disabled="!canAudit(item)" @click="doAudit(item, 2)">拒绝</button>
                </td>
              </tr>
              <tr v-if="!loading && !list.length && !error">
                <td colspan="7"><div class="empty-state">{{ emptyTip() }}</div></td>
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
    <DrawerPanel :visible="detailVisible" title="需求详情" @close="detailVisible = false">
      <div v-if="current" class="detail-grid">
        <div><strong>标题：</strong>{{ current.requirementTitle }}</div>
        <div><strong>描述：</strong>{{ current.requirementDescription }}</div>
        <div><strong>年级：</strong>{{ current.requirementGrade }}</div>
        <div><strong>薪资：</strong>{{ current.requirementSalary }}</div>
        <div><strong>地址：</strong>{{ current.requirementAddress }}</div>
        <div><strong>频率：</strong>{{ current.requirementFrequency || '-' }}</div>
        <div><strong>其他要求：</strong>{{ current.requirementOther || '-' }}</div>
        <div v-if="relationLoading" class="skeleton h-60" />
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
              <h4 class="section-title">匹配教员摘要</h4>
              <div v-if="teacherProfile">
                <div><strong>姓名：</strong>{{ teacherProfile.user?.userName || '-' }}</div>
                <div><strong>学校：</strong>{{ teacherProfile.teacherInfo?.teacherSchool || '-' }}</div>
                <div><strong>专业：</strong>{{ teacherProfile.teacherInfo?.teacherMajor || '-' }}</div>
                <div><strong>教龄：</strong>{{ teacherProfile.teacherInfo?.teacherTeachingYears || 0 }} 年</div>
              </div>
              <div v-else class="empty-state">暂无已关联教员</div>
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



