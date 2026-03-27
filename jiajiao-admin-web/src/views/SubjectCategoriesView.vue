<script setup>
import { onMounted, ref } from 'vue'
import { deleteSubjectCategory, pageSubjectCategories, saveSubjectCategory } from '../api'
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
    categoryName: '',
    categoryCode: '',
    categorySort: 0,
    categoryStatus: 1
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
  if (!form.value.categoryName?.trim()) return '分类名称不能为空'
  if (!form.value.categoryCode?.trim()) return '分类编码不能为空'
  if (!/^[A-Z0-9_]+$/.test(form.value.categoryCode)) return '分类编码仅支持大写字母、数字、下划线'
  if (Number(form.value.categorySort) < 0) return '排序值不能小于0'
  if (![0, 1].includes(Number(form.value.categoryStatus))) return '状态值仅支持0或1'
  return ''
}

async function load() {
  loading.value = true
  try {
    const data = await pageSubjectCategories({ pageNo: 1, pageSize: 200 })
    const records = data.records || []
    list.value = keyword.value
      ? records.filter((i) => (i.categoryName || '').includes(keyword.value) || (i.categoryCode || '').includes(keyword.value))
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
    await saveSubjectCategory({
      ...form.value,
      categorySort: Number(form.value.categorySort),
      categoryStatus: Number(form.value.categoryStatus)
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
  confirmText.value = `确认删除分类“${item.categoryName}”？`
  confirmAction.value = async () => {
    await deleteSubjectCategory(item.id)
    await load()
  }
  confirmVisible.value = true
}

async function doConfirm() {
  if (confirmAction.value) {
    await confirmAction.value()
  }
  confirmVisible.value = false
}

onMounted(load)
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">学科分类管理</h3>
    <p class="section-subtitle">维护前端侧边筛选大类（小学/初中/高中/兴趣等）</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <div class="toolbar">
          <input v-model="keyword" class="input col-span-3" placeholder="分类名称/编码筛选" />
          <button class="btn ghost col-span-1" @click="load">筛选</button>
          <button class="btn col-span-1" @click="openCreate">新增</button>
        </div>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>分类名称</th><th>分类编码</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="5"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.categoryName }}</td>
              <td>{{ item.categoryCode }}</td>
              <td>{{ item.categorySort ?? 0 }}</td>
              <td>{{ Number(item.categoryStatus) === 1 ? '启用' : '禁用' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="5"><div class="empty-state">暂无分类数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑分类' : '新增分类'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>分类名称</label>
          <input v-model="form.categoryName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>分类编码</label>
          <input v-model="form.categoryCode" class="input" placeholder="例如：PRIMARY" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>排序（数字）</label>
          <input v-model.number="form.categorySort" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.categoryStatus" class="select">
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



