<script setup>
import { onMounted, ref } from 'vue'
import { auditRequirement, getTeacherProfile, getUserProfile, pageOrders, pageRequirements } from '../api'
import DrawerPanel from '../components/admin/DrawerPanel.vue'
import StatusTag from '../components/admin/StatusTag.vue'

const list = ref([])
const status = ref('')
const detailVisible = ref(false)
const current = ref(null)
const loading = ref(false)
const error = ref('')
const feedback = ref({ type: '', text: '' })
const relationLoading = ref(false)
const parentProfile = ref(null)
const teacherProfile = ref(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageRequirements({ pageNo: 1, pageSize: 50, status: status.value || undefined })
    list.value = data.records || []
  } catch (e) {
    error.value = e.message || '需求列表加载失败'
    list.value = []
  } finally {
    loading.value = false
  }
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
    <h3 class="section-title">需求管理</h3>
    <p class="section-subtitle">按状态筛选需求并执行审核流转</p>
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
          <button class="btn col-span-1" @click="load">筛选</button>
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
        <span class="table-meta">共 {{ list.length }} 条</span>
      </div>
      <div class="panel-body panel-body-pt-4">
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
              <td class="action-group">
                <button class="btn ghost" @click="showDetail(item)">详情</button>
                <button class="btn" @click="doAudit(item, 1)">通过</button>
                <button class="btn secondary" @click="doAudit(item, 2)">拒绝</button>
              </td>
            </tr>
            <tr v-if="!list.length">
              <td colspan="7"><div class="empty-state">暂无需求数据</div></td>
            </tr>
          </tbody>
        </table>
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
.detail-grid {
  display: grid;
  gap: 10px;
}
.table-meta {
  color: var(--text-sub);
  font-size: 13px;
}
</style>



