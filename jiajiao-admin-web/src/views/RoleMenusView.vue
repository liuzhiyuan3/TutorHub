<script setup>
import { onMounted, ref } from 'vue'
import { deleteRoleMenu, pageRoleMenus, saveRoleMenu } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const formError = ref('')
const form = ref({ roleId: '', menuId: '' })
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')

async function load() {
  loading.value = true
  try {
    const data = await pageRoleMenus({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.value.roleId?.trim()) {
    formError.value = '角色ID不能为空'
    return
  }
  if (!form.value.menuId?.trim()) {
    formError.value = '菜单ID不能为空'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await saveRoleMenu(form.value)
    form.value = { roleId: '', menuId: '' }
    await load()
  } catch (e) {
    formError.value = e.message || '新增绑定失败'
  } finally {
    saving.value = false
  }
}

function remove(item) {
  confirmText.value = `确认删除关系“${item.id}”？`
  confirmAction.value = async () => {
    await deleteRoleMenu(item.id)
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
    <h3 class="section-title">角色菜单绑定</h3>
    <p class="section-subtitle">统一输入区与表格排版，长ID按等宽字体显示避免错位</p>

    <div class="panel panel-mt-12">
      <div class="panel-body">
        <div class="toolbar role-menu-grid">
          <input v-model="form.roleId" class="input" placeholder="角色ID，例如 r_super_admin_..." />
          <input v-model="form.menuId" class="input" placeholder="菜单ID，例如 m_user_..." />
          <button class="btn" :disabled="saving" @click="submit">{{ saving ? '提交中...' : '新增绑定' }}</button>
        </div>
        <p v-if="formError" class="form-error mt-8">{{ formError }}</p>
      </div>
    </div>

    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table role-menu-table">
          <thead><tr><th class="col-w-92">ID</th><th>角色ID</th><th>菜单ID</th><th class="col-w-94">操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="4"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td><span class="mono-id">{{ item.id }}</span></td>
              <td><span class="mono-id">{{ item.roleId }}</span></td>
              <td><span class="mono-id">{{ item.menuId }}</span></td>
              <td>
                <button class="btn danger" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="4"><div class="empty-state">暂无角色菜单绑定数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
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

<style scoped>
.role-menu-grid {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 12px;
  margin-bottom: 0;
}
.role-menu-table {
  table-layout: fixed;
}
.mono-id {
  font-family: "JetBrains Mono", "Consolas", monospace;
  font-size: 13px;
  color: #2f4563;
  word-break: break-all;
}
@media (max-width: 980px) {
  .role-menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>



