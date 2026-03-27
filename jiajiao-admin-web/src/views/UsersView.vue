<script setup>
import { onMounted, ref } from 'vue'
import { deleteUser, getUserProfile, pageUsers, updateUserStatus } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import DrawerPanel from '../components/admin/DrawerPanel.vue'
import StatusTag from '../components/admin/StatusTag.vue'

const keyword = ref('')
const list = ref([])
const loading = ref(false)
const error = ref('')
const feedback = ref({ type: '', text: '' })
const confirmVisible = ref(false)
const confirmText = ref('')
const confirmDanger = ref(false)
const pendingAction = ref(null)
const profileVisible = ref(false)
const profileLoading = ref(false)
const profileData = ref(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageUsers({ pageNo: 1, pageSize: 50, keyword: keyword.value || undefined })
    list.value = data.records || []
  } catch (e) {
    error.value = e.message || '用户列表加载失败'
    list.value = []
  } finally {
    loading.value = false
  }
}

function openConfirm(message, action, danger = false) {
  confirmText.value = message
  confirmDanger.value = danger
  pendingAction.value = action
  confirmVisible.value = true
}

async function doConfirm() {
  try {
    if (pendingAction.value) {
      await pendingAction.value()
      feedback.value = { type: 'success', text: '操作成功' }
      await load()
    }
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '操作失败' }
  }
  confirmVisible.value = false
  pendingAction.value = null
}

function closeConfirm() {
  confirmVisible.value = false
  pendingAction.value = null
}

function maskPhone(value) {
  if (!value) return '-'
  return String(value).replace(/^(\d{3})\d{4}(\d+)$/, '$1****$2')
}

function maskEmail(value) {
  if (!value || !String(value).includes('@')) return value || '-'
  const [name, domain] = String(value).split('@')
  if (!name) return `***@${domain}`
  return `${name.slice(0, 1)}***@${domain}`
}

async function openProfile(item) {
  profileVisible.value = true
  profileLoading.value = true
  try {
    profileData.value = await getUserProfile(item.id)
    feedback.value = { type: '', text: '' }
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '档案加载失败' }
  } finally {
    profileLoading.value = false
  }
}

function toggleStatus(item) {
  const nextStatus = item.userStatus === 1 ? 0 : 1
  openConfirm(`确认将用户“${item.userName}”设为${nextStatus === 1 ? '启用' : '禁用'}吗？`, async () => {
    await updateUserStatus(item.id, nextStatus)
  })
}

function remove(item) {
  openConfirm(`确认删除用户“${item.userName}”吗？该操作不可恢复。`, async () => {
    await deleteUser(item.id)
  }, true)
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">用户管理</h3>
    <p class="section-subtitle">支持账号检索、用户状态启停及删除操作</p>
    <div class="panel panel-mt-14">
      <div class="panel-head">
        <strong>筛选条件</strong>
      </div>
      <div class="panel-body">
        <div class="toolbar">
          <input
            v-model="keyword"
            class="input col-span-4"
            placeholder="姓名/账号/手机号"
            @keyup.enter="load"
          />
          <button class="btn col-span-1" :disabled="loading" @click="load">查询</button>
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
        <strong>用户列表</strong>
        <span class="table-meta">共 {{ list.length }} 条</span>
      </div>
      <div class="panel-body panel-body-pt-4">
        <table class="table">
          <thead><tr><th>姓名</th><th>账号</th><th>手机号</th><th>类型</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.userName }}</td>
              <td>{{ item.userAccount }}</td>
              <td>{{ item.userPhone }}</td>
              <td>
                <StatusTag :type="item.userType === 0 ? 'info' : 'pending'" :text="item.userType === 0 ? '家长' : '教员'" />
              </td>
              <td>
                <StatusTag :type="item.userStatus === 1 ? 'success' : 'error'" :text="item.userStatus === 1 ? '启用' : '禁用'" />
              </td>
              <td class="action-group">
                <button class="btn ghost" @click="openProfile(item)">查看档案</button>
                <button class="btn ghost" @click="toggleStatus(item)">{{ item.userStatus === 1 ? '禁用' : '启用' }}</button>
                <button class="btn danger" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length">
              <td colspan="6"><div class="empty-state">暂无用户数据</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <ConfirmDialog
      :visible="confirmVisible"
      title="操作确认"
      :message="confirmText"
      :danger="confirmDanger"
      @confirm="doConfirm"
      @cancel="closeConfirm"
    />
    <DrawerPanel :visible="profileVisible" title="用户档案" @close="profileVisible = false">
      <div v-if="profileLoading" class="skeleton h-60" />
      <div v-else-if="profileData" class="detail-grid">
        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">基础信息</h4>
            <div><strong>姓名：</strong>{{ profileData.user?.userName || '-' }}</div>
            <div><strong>账号：</strong>{{ profileData.user?.userAccount || '-' }}</div>
            <div><strong>手机号：</strong>{{ maskPhone(profileData.user?.userPhone) }}</div>
            <div><strong>邮箱：</strong>{{ maskEmail(profileData.user?.userEmail) }}</div>
            <div><strong>类型：</strong>{{ profileData.user?.userType === 1 ? '教员' : '家长' }}</div>
          </div>
        </div>

        <div class="panel" v-if="profileData.teacherInfo">
          <div class="panel-body">
            <h4 class="section-title">教员档案</h4>
            <div><strong>身份：</strong>{{ profileData.teacherInfo.teacherIdentity || '-' }}</div>
            <div><strong>学校：</strong>{{ profileData.teacherInfo.teacherSchool || '-' }}</div>
            <div><strong>专业：</strong>{{ profileData.teacherInfo.teacherMajor || '-' }}</div>
            <div><strong>教龄：</strong>{{ profileData.teacherInfo.teacherTeachingYears ?? 0 }} 年</div>
            <div><strong>自我评价：</strong>{{ profileData.teacherInfo.teacherSelfDescription || '-' }}</div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">家长需求（最近20条）</h4>
            <div v-if="!(profileData.requirements || []).length" class="empty-state">暂无需求记录</div>
            <table v-else class="table">
              <thead><tr><th>标题</th><th>年级</th><th>状态</th><th>薪资</th></tr></thead>
              <tbody>
                <tr v-for="req in profileData.requirements" :key="req.id">
                  <td>{{ req.requirementTitle }}</td>
                  <td>{{ req.requirementGrade }}</td>
                  <td>{{ ['待接单', '已接单', '已完成', '已取消'][req.requirementStatus || 0] }}</td>
                  <td>{{ req.requirementSalary }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">家长订单（最近20条）</h4>
            <div v-if="!(profileData.parentOrders || []).length" class="empty-state">暂无订单记录</div>
            <table v-else class="table">
              <thead><tr><th>订单号</th><th>状态</th><th>金额</th></tr></thead>
              <tbody>
                <tr v-for="od in profileData.parentOrders" :key="od.id">
                  <td>{{ od.orderNumber }}</td>
                  <td>{{ ['待确认', '进行中', '已完成', '已取消'][od.orderStatus || 0] }}</td>
                  <td>{{ od.orderAmount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">预约记录（最近20条）</h4>
            <div v-if="!(profileData.appointments || []).length" class="empty-state">暂无预约记录</div>
            <table v-else class="table">
              <thead><tr><th>科目</th><th>年级</th><th>地址</th><th>状态</th></tr></thead>
              <tbody>
                <tr v-for="app in profileData.appointments" :key="app.id">
                  <td>{{ app.appointmentSubject }}</td>
                  <td>{{ app.appointmentGrade }}</td>
                  <td>{{ app.appointmentAddress }}</td>
                  <td>{{ ['待确认', '已确认', '已取消'][app.appointmentStatus || 0] }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </DrawerPanel>
  </div>
</template>

<style scoped>
.detail-grid {
  display: grid;
  gap: 12px;
}
.table-meta {
  color: var(--text-sub);
  font-size: 13px;
}
</style>



