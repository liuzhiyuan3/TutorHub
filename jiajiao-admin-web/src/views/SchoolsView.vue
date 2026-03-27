<script setup>
import { onMounted, ref } from 'vue'
import { deleteSchool, pageSchools, saveSchool } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const formError = ref('')
const form = ref(createDefaultForm())
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')

function createDefaultForm() {
  return {
    id: '',
    schoolName: '',
    schoolCode: '',
    schoolType: 0,
    schoolProvince: '北京',
    schoolCity: '北京',
    schoolDistrict: '海淀区',
    schoolAddress: '',
    schoolLongitude: null,
    schoolLatitude: null,
    schoolStatus: 1,
    schoolDeleteStatus: 0
  }
}

function openCreate() {
  formError.value = ''
  form.value = createDefaultForm()
  modalVisible.value = true
}

function edit(item) {
  formError.value = ''
  form.value = {
    ...createDefaultForm(),
    ...item
  }
  modalVisible.value = true
}

function validateForm() {
  if (!form.value.schoolName?.trim()) return '学校名称不能为空'
  if (!form.value.schoolCode?.trim()) return '学校编码不能为空'
  if (!/^[A-Z0-9_]+$/.test(form.value.schoolCode)) return '学校编码仅支持大写字母、数字、下划线'
  if (Number(form.value.schoolType) < 0) return '学校类型不能小于0'
  if (![0, 1].includes(Number(form.value.schoolStatus))) return '状态值仅支持0或1'
  if (form.value.schoolLongitude !== null && form.value.schoolLongitude !== '' && Number.isNaN(Number(form.value.schoolLongitude))) {
    return '经度必须是数字'
  }
  if (form.value.schoolLatitude !== null && form.value.schoolLatitude !== '' && Number.isNaN(Number(form.value.schoolLatitude))) {
    return '纬度必须是数字'
  }
  return ''
}

async function load() {
  loading.value = true
  try {
    const data = await pageSchools({ pageNo: 1, pageSize: 50 })
    const records = data.records || []
    list.value = keyword.value
      ? records.filter((i) => (i.schoolName || '').includes(keyword.value) || (i.schoolCode || '').includes(keyword.value))
      : records
  } finally {
    loading.value = false
  }
}

async function submit() {
  formError.value = validateForm()
  if (formError.value) return
  saving.value = true
  try {
    await saveSchool({
      ...form.value,
      schoolType: Number(form.value.schoolType),
      schoolStatus: Number(form.value.schoolStatus),
      schoolLongitude: form.value.schoolLongitude === '' ? null : form.value.schoolLongitude,
      schoolLatitude: form.value.schoolLatitude === '' ? null : form.value.schoolLatitude
    })
    modalVisible.value = false
    await load()
  } catch (e) {
    formError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function remove(item) {
  confirmText.value = `确认删除学校“${item.schoolName}”？`
  confirmAction.value = async () => {
    await deleteSchool(item.id)
    await load()
  }
  confirmVisible.value = true
}

async function doConfirm() {
  if (confirmAction.value) await confirmAction.value()
  confirmVisible.value = false
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">学校管理</h3>
    <p class="section-subtitle">独立新增弹窗，字段与 school 表保持一致</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <div class="toolbar">
          <input v-model="keyword" class="input col-span-3" placeholder="学校名称/编码筛选" />
          <button class="btn ghost col-span-1" @click="load">筛选</button>
          <button class="btn col-span-1" @click="openCreate">新增</button>
        </div>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>名称</th><th>编码</th><th>城市</th><th>地址</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.schoolName }}</td>
              <td>{{ item.schoolCode }}</td>
              <td>{{ item.schoolCity }}</td>
              <td>{{ item.schoolAddress || '-' }}</td>
              <td>{{ Number(item.schoolStatus) === 1 ? '启用' : '禁用' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="6"><div class="empty-state">暂无学校数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑学校' : '新增学校'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>学校名称</label>
          <input v-model="form.schoolName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>学校编码</label>
          <input v-model="form.schoolCode" class="input" placeholder="例如：PKU_MAIN" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>学校类型（数字）</label>
          <input v-model.number="form.schoolType" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>省份</label>
          <input v-model="form.schoolProvince" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>城市</label>
          <input v-model="form.schoolCity" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>区县</label>
          <input v-model="form.schoolDistrict" class="input" />
        </div>
        <div class="form-item span-2">
          <label class="form-label">学校地址</label>
          <input v-model="form.schoolAddress" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label">经度（数字）</label>
          <input v-model="form.schoolLongitude" class="input" type="number" step="0.000001" />
        </div>
        <div class="form-item">
          <label class="form-label">纬度（数字）</label>
          <input v-model="form.schoolLatitude" class="input" type="number" step="0.000001" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.schoolStatus" class="select">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </div>
      </div>
      <p v-if="formError" class="form-error">{{ formError }}</p>
    </FormModal>
    <ConfirmDialog
      :visible="confirmVisible"
      title="删除确认"
      :message="confirmText"
      :danger="true"
      @confirm="doConfirm"
      @cancel="confirmVisible = false"
    />
  </div>
</template>



