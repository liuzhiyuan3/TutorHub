<script setup>
import { onMounted, ref } from 'vue'
import { deleteRole, pageRoles, saveRole } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const formError = ref('')
const form = ref({ id: '', roleName: '', roleCode: '', roleDescription: '', roleDeleteStatus: 0 })
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')

async function load() {
  loading.value = true
  try {
    const data = await pageRoles({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { id: '', roleName: '', roleCode: '', roleDescription: '', roleDeleteStatus: 0 }
  formError.value = ''
  modalVisible.value = true
}

async function submit() {
  if (!form.value.roleName?.trim()) {
    formError.value = '角色名称不能为空'
    return
  }
  if (!form.value.roleCode?.trim()) {
    formError.value = '角色编码不能为空'
    return
  }
  if (!/^[A-Z0-9_]+$/.test(form.value.roleCode)) {
    formError.value = '角色编码仅支持大写字母、数字、下划线'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await saveRole(form.value)
    modalVisible.value = false
    await load()
  } catch (e) {
    formError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function edit(item) {
  form.value = { ...item }
  formError.value = ''
  modalVisible.value = true
}

function remove(item) {
  confirmText.value = `确认删除角色“${item.roleName}”？`
  confirmAction.value = async () => {
    await deleteRole(item.id)
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
    <h3 class="section-title">角色管理</h3>
    <p class="section-subtitle">新增按钮独立展示，弹窗内字段与 role 表对齐</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <button class="btn" @click="openCreate">新增角色</button>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>名称</th><th>编码</th><th>描述</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="4"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.roleName }}</td>
              <td>{{ item.roleCode }}</td>
              <td>{{ item.roleDescription || '-' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="4"><div class="empty-state">暂无角色数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑角色' : '新增角色'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>角色名称</label>
          <input v-model="form.roleName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>角色编码</label>
          <input v-model="form.roleCode" class="input" />
        </div>
        <div class="form-item span-2">
          <label class="form-label">角色描述</label>
          <input v-model="form.roleDescription" class="input" />
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




