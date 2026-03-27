<script setup>
import { onMounted, ref } from 'vue'
import { deleteSubject, pageSubjectCategories, pageSubjects, saveSubject } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const categoryList = ref([])
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
    subjectName: '',
    subjectCode: '',
    subjectCategoryId: '',
    subjectCategory: '',
    subjectDescription: '',
    subjectSort: 0,
    subjectStatus: 1,
    subjectDeleteStatus: 0
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
  if (!form.value.subjectName?.trim()) return '学科名称不能为空'
  if (!form.value.subjectCode?.trim()) return '学科编码不能为空'
  if (!/^[A-Z0-9_]+$/.test(form.value.subjectCode)) return '学科编码仅支持大写字母、数字、下划线'
  if (Number(form.value.subjectSort) < 0) return '排序值不能小于0'
  if (![0, 1].includes(Number(form.value.subjectStatus))) return '状态值仅支持0或1'
  return ''
}

async function load() {
  loading.value = true
  try {
    const data = await pageSubjects({ pageNo: 1, pageSize: 50 })
    const records = data.records || []
    list.value = keyword.value
      ? records.filter((i) => (i.subjectName || '').includes(keyword.value) || (i.subjectCode || '').includes(keyword.value))
      : records
  } finally {
    loading.value = false
  }
}

async function loadCategoryList() {
  const data = await pageSubjectCategories({ pageNo: 1, pageSize: 200 })
  categoryList.value = data.records || []
}

async function submit() {
  formError.value = validateForm()
  if (formError.value) return
  saving.value = true
  try {
    await saveSubject({
      ...form.value,
      subjectSort: Number(form.value.subjectSort),
      subjectStatus: Number(form.value.subjectStatus)
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
  confirmText.value = `确认删除学科“${item.subjectName}”？`
  confirmAction.value = async () => {
    await deleteSubject(item.id)
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

onMounted(async () => {
  await loadCategoryList()
  await load()
})
</script>

<template>
  <div class="page-block">
    <h3 class="section-title">学科管理</h3>
    <p class="section-subtitle">新增与编辑使用独立弹窗，字段与数据库一致</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <div class="toolbar">
          <input v-model="keyword" class="input col-span-3" placeholder="学科名称/编码筛选" />
          <button class="btn ghost col-span-1" @click="load">筛选</button>
          <button class="btn col-span-1" @click="openCreate">新增</button>
        </div>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>名称</th><th>编码</th><th>分类</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.subjectName }}</td>
              <td>{{ item.subjectCode }}</td>
              <td>{{ item.subjectCategory || '-' }}</td>
              <td>{{ item.subjectSort ?? 0 }}</td>
              <td>{{ Number(item.subjectStatus) === 1 ? '启用' : '禁用' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="6"><div class="empty-state">暂无学科数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑学科' : '新增学科'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>学科名称</label>
          <input v-model="form.subjectName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>学科编码</label>
          <input v-model="form.subjectCode" class="input" placeholder="例如：MATH_HIGH" />
        </div>
        <div class="form-item">
          <label class="form-label">所属分类</label>
          <select v-model="form.subjectCategoryId" class="select">
            <option value="">未指定</option>
            <option v-for="item in categoryList" :key="item.id" :value="item.id">{{ item.categoryName }}</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>排序（数字）</label>
          <input v-model.number="form.subjectSort" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item span-2">
          <label class="form-label">学科描述</label>
          <textarea v-model="form.subjectDescription" class="textarea" rows="3" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.subjectStatus" class="select">
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



