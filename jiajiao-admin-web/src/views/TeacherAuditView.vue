<script setup>
import { onMounted, ref } from 'vue'
import { auditTeacher, getTeacherProfile, pageTeachers } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import DrawerPanel from '../components/admin/DrawerPanel.vue'
import StatusTag from '../components/admin/StatusTag.vue'

const list = ref([])
const loading = ref(false)
const error = ref('')
const feedback = ref({ type: '', text: '' })
const detailVisible = ref(false)
const current = ref(null)
const detailLoading = ref(false)
const detailData = ref(null)
const rejectVisible = ref(false)
const rejectReason = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await pageTeachers({ pageNo: 1, pageSize: 50 })
    list.value = data.records || []
  } catch (e) {
    error.value = e.message || '教员列表加载失败'
    list.value = []
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    await auditTeacher(id, { auditStatus: 1, reason: '' })
    feedback.value = { type: 'success', text: '审核通过成功' }
    await load()
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '审核操作失败' }
  }
}

function openReject(item) {
  current.value = item
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!current.value) return
  try {
    await auditTeacher(current.value.id, { auditStatus: 2, reason: rejectReason.value || '资料不符合要求' })
    feedback.value = { type: 'success', text: '已拒绝该教员审核' }
    rejectVisible.value = false
    await load()
  } catch (e) {
    feedback.value = { type: 'error', text: e.message || '审核操作失败' }
  }
}

function openDetail(item) {
  current.value = item
  detailVisible.value = true
  detailData.value = null
  detailLoading.value = true
  getTeacherProfile(item.id)
    .then((data) => {
      detailData.value = data
    })
    .finally(() => {
      detailLoading.value = false
    })
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">教员审核</h3>
    <p class="section-subtitle">审核教员资料并保留拒绝原因，支持详情抽屉查看</p>
    <div v-if="error" class="page-feedback error">
      加载失败：{{ error }}
      <button class="btn ghost ml-8" @click="load">重试</button>
    </div>
    <div v-if="feedback.text" :class="['page-feedback', feedback.type === 'error' ? 'error' : '']">
      {{ feedback.text }}
    </div>
    <div class="panel panel-mt-14">
      <div class="panel-head">
        <strong>教员审核列表</strong>
        <span class="table-meta">共 {{ list.length }} 条</span>
      </div>
      <div class="panel-body panel-body-pt-4">
        <table class="table">
          <thead><tr><th>身份</th><th>学校</th><th>专业</th><th>教龄</th><th>审核状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.teacherIdentity }}</td>
              <td>{{ item.teacherSchool || '-' }}</td>
              <td>{{ item.teacherMajor || '-' }}</td>
              <td>{{ item.teacherTeachingYears }}</td>
              <td>
                <StatusTag
                  :type="item.teacherAuditStatus === 1 ? 'success' : item.teacherAuditStatus === 2 ? 'error' : 'pending'"
                  :text="['待审', '通过', '拒绝'][item.teacherAuditStatus || 0]"
                />
              </td>
              <td class="action-group">
                <button class="btn ghost" @click="openDetail(item)">详情</button>
                <button class="btn" :disabled="loading" @click="approve(item.id)">通过</button>
                <button class="btn secondary" :disabled="loading" @click="openReject(item)">拒绝</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length">
              <td colspan="6"><div class="empty-state">暂无待审核教员</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <DrawerPanel :visible="detailVisible" title="教员详情" @close="detailVisible = false">
      <div v-if="detailLoading" class="skeleton h-60" />
      <div v-else-if="detailData" class="detail-grid">
        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">基础信息</h4>
            <div><strong>姓名：</strong>{{ detailData.user?.userName || '-' }}</div>
            <div><strong>手机号：</strong>{{ detailData.user?.userPhone || '-' }}</div>
            <div><strong>身份：</strong>{{ detailData.teacherInfo?.teacherIdentity || '-' }}</div>
            <div><strong>学校：</strong>{{ detailData.teacherInfo?.teacherSchool || '-' }}</div>
            <div><strong>专业：</strong>{{ detailData.teacherInfo?.teacherMajor || '-' }}</div>
            <div><strong>学历：</strong>{{ detailData.teacherInfo?.teacherEducation || '-' }}</div>
            <div><strong>教龄：</strong>{{ detailData.teacherInfo?.teacherTeachingYears || 0 }} 年</div>
            <div><strong>授课方式：</strong>{{ ['线上', '线下', '线上线下'][detailData.teacherInfo?.teacherTutoringMethod || 0] }}</div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">个人简介</h4>
            <div><strong>自我评价：</strong>{{ detailData.teacherInfo?.teacherSelfDescription || '-' }}</div>
            <div><strong>教学经历：</strong>{{ detailData.teacherInfo?.teacherExperience || '-' }}</div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">授课标签</h4>
            <div><strong>科目：</strong>{{ (detailData.subjectNames || []).join('、') || '-' }}</div>
            <div><strong>区域：</strong>{{ (detailData.regionNames || []).join('、') || '-' }}</div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-body">
            <h4 class="section-title">成功记录（最近20条）</h4>
            <div v-if="!(detailData.successRecords || []).length" class="empty-state">暂无成功记录</div>
            <table v-else class="table">
              <thead><tr><th>年级科目</th><th>描述</th></tr></thead>
              <tbody>
                <tr v-for="sr in detailData.successRecords" :key="sr.id">
                  <td>{{ sr.successGrade }}</td>
                  <td>{{ sr.successDescription || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </DrawerPanel>
    <ConfirmDialog
      :visible="rejectVisible"
      title="拒绝审核"
      :message="`拒绝原因：${rejectReason || '未填写（将使用默认原因）'}`"
      confirm-text="确认拒绝"
      :danger="true"
      @confirm="doReject"
      @cancel="rejectVisible = false"
    />
    <div v-if="rejectVisible" class="reject-box card">
      <p class="hint-text">请填写拒绝原因（可选）</p>
      <textarea v-model="rejectReason" class="textarea" rows="3" placeholder="例如：证件信息不完整、教学经历不足" />
    </div>
  </div>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.reject-box {
  margin-top: 12px;
  padding: 12px;
}
.table-meta {
  color: var(--text-sub);
  font-size: 13px;
}
</style>



