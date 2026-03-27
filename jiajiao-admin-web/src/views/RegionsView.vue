<script setup>
import { onMounted, ref } from 'vue'
import { deleteRegion, pageRegions, saveRegion } from '../api'
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
    regionName: '',
    regionCode: '',
    regionCity: '北京',
    regionProvince: '北京',
    regionSort: 0,
    regionStatus: 1,
    regionDeleteStatus: 0
  }
}

function openCreate() {
  formError.value = ''
  form.value = createDefaultForm()
  modalVisible.value = true
}

function edit(item) {
  formError.value = ''
  form.value = { ...createDefaultForm(), ...item }
  modalVisible.value = true
}

function validateForm() {
  if (!form.value.regionName?.trim()) return '区域名称不能为空'
  if (!form.value.regionCode?.trim()) return '区域编码不能为空'
  if (!/^[A-Z0-9_]+$/.test(form.value.regionCode)) return '区域编码仅支持大写字母、数字、下划线'
  if (Number(form.value.regionSort) < 0) return '排序值不能小于0'
  if (![0, 1].includes(Number(form.value.regionStatus))) return '状态值仅支持0或1'
  return ''
}

async function load() {
  loading.value = true
  try {
    const data = await pageRegions({ pageNo: 1, pageSize: 50 })
    const records = data.records || []
    list.value = keyword.value
      ? records.filter((i) => (i.regionName || '').includes(keyword.value) || (i.regionCode || '').includes(keyword.value))
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
    await saveRegion({
      ...form.value,
      regionSort: Number(form.value.regionSort),
      regionStatus: Number(form.value.regionStatus)
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
  confirmText.value = `确认删除区域“${item.regionName}”？`
  confirmAction.value = async () => {
    await deleteRegion(item.id)
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
    <h3 class="section-title">区域管理</h3>
    <p class="section-subtitle">区域新增与编辑使用弹窗，支持数字字段限制</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <div class="toolbar">
          <input v-model="keyword" class="input col-span-3" placeholder="区域名称/编码筛选" />
          <button class="btn ghost col-span-1" @click="load">筛选</button>
          <button class="btn col-span-1" @click="openCreate">新增</button>
        </div>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>区域</th><th>编码</th><th>城市</th><th>省份</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.regionName }}</td>
              <td>{{ item.regionCode }}</td>
              <td>{{ item.regionCity }}</td>
              <td>{{ item.regionProvince }}</td>
              <td>{{ item.regionSort ?? 0 }}</td>
              <td>{{ Number(item.regionStatus) === 1 ? '启用' : '禁用' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="7"><div class="empty-state">暂无区域数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑区域' : '新增区域'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>区域名称</label>
          <input v-model="form.regionName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>区域编码</label>
          <input v-model="form.regionCode" class="input" placeholder="例如：HD_DISTRICT" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>城市</label>
          <input v-model="form.regionCity" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>省份</label>
          <input v-model="form.regionProvince" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>排序（数字）</label>
          <input v-model.number="form.regionSort" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.regionStatus" class="select">
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



