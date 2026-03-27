<script setup>
import { onMounted, ref } from 'vue'
import { deleteMenu, pageMenus, saveMenu } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const formError = ref('')
const form = ref({ id: '', menuName: '', menuParent: '', menuPriority: 0, menuLink: '', menuIcon: '', menuType: 1, menuDeleteStatus: 0 })
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')

async function load() {
  loading.value = true
  try {
    const data = await pageMenus({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  formError.value = ''
  form.value = { id: '', menuName: '', menuParent: '', menuPriority: 0, menuLink: '', menuIcon: '', menuType: 1, menuDeleteStatus: 0 }
  modalVisible.value = true
}

async function submit() {
  if (!form.value.menuName?.trim()) {
    formError.value = '菜单名称不能为空'
    return
  }
  if (Number(form.value.menuPriority) < 0) {
    formError.value = '菜单排序不能小于0'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await saveMenu({
      ...form.value,
      menuPriority: Number(form.value.menuPriority),
      menuType: Number(form.value.menuType)
    })
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
  confirmText.value = `确认删除菜单“${item.menuName}”？`
  confirmAction.value = async () => {
    await deleteMenu(item.id)
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
    <h3 class="section-title">菜单管理</h3>
    <p class="section-subtitle">独立新增按钮 + 弹窗编辑，菜单字段与数据库保持一致</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <button class="btn" @click="openCreate">新增菜单</button>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>名称</th><th>类型</th><th>父菜单</th><th>链接</th><th>排序</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="6"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.menuName }}</td>
              <td>{{ ['目录', '菜单', '按钮'][item.menuType || 0] }}</td>
              <td>{{ item.menuParent || '-' }}</td>
              <td>{{ item.menuLink || '-' }}</td>
              <td>{{ item.menuPriority ?? 0 }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="6"><div class="empty-state">暂无菜单数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑菜单' : '新增菜单'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>菜单名称</label>
          <input v-model="form.menuName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label">父菜单ID</label>
          <input v-model="form.menuParent" class="input" placeholder="可空" />
        </div>
        <div class="form-item">
          <label class="form-label">路由链接</label>
          <input v-model="form.menuLink" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label">图标</label>
          <input v-model="form.menuIcon" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>菜单类型</label>
          <select v-model.number="form.menuType" class="select">
            <option :value="0">目录</option>
            <option :value="1">菜单</option>
            <option :value="2">按钮</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>排序（数字）</label>
          <input v-model.number="form.menuPriority" class="input" type="number" min="0" step="1" />
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




