<script setup>
import { onMounted, ref } from 'vue'
import {
  deleteDictionary,
  deleteDictionaryContent,
  pageDictionaryContents,
  pageDictionaries,
  saveDictionary,
  saveDictionaryContent
} from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import DrawerPanel from '../components/admin/DrawerPanel.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const loading = ref(false)
const dictSaving = ref(false)
const itemSaving = ref(false)
const dictModalVisible = ref(false)
const itemModalVisible = ref(false)
const dictError = ref('')
const itemError = ref('')
const dictForm = ref({ id: '', dictionaryName: '', dictionaryCode: '', dictionaryDescription: '', dictionaryDeleteStatus: 0 })
const itemList = ref([])
const itemForm = ref({
  id: '',
  dictionaryId: '',
  dictionaryContentText: '',
  dictionaryContentValue: '',
  dictionaryContentSort: 0,
  dictionaryContentStatus: 1
})
const drawerVisible = ref(false)
const currentDict = ref(null)
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')

async function load() {
  loading.value = true
  try {
    const data = await pageDictionaries({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreateDict() {
  dictError.value = ''
  dictForm.value = { id: '', dictionaryName: '', dictionaryCode: '', dictionaryDescription: '', dictionaryDeleteStatus: 0 }
  dictModalVisible.value = true
}

function editDict(item) {
  dictForm.value = { ...item }
  dictError.value = ''
  dictModalVisible.value = true
}

async function submitDict() {
  if (!dictForm.value.dictionaryName?.trim()) {
    dictError.value = '字典名称不能为空'
    return
  }
  if (!dictForm.value.dictionaryCode?.trim()) {
    dictError.value = '字典编码不能为空'
    return
  }
  if (!/^[A-Z0-9_]+$/.test(dictForm.value.dictionaryCode)) {
    dictError.value = '字典编码仅支持大写字母、数字、下划线'
    return
  }
  dictSaving.value = true
  dictError.value = ''
  try {
    await saveDictionary(dictForm.value)
    dictModalVisible.value = false
    await load()
  } catch (e) {
    dictError.value = e.message || '保存失败'
  } finally {
    dictSaving.value = false
  }
}

function removeDict(item) {
  confirmText.value = `确认删除字典“${item.dictionaryName}”？`
  confirmAction.value = async () => {
    await deleteDictionary(item.id)
    await load()
  }
  confirmVisible.value = true
}

async function openItems(dict) {
  currentDict.value = dict
  drawerVisible.value = true
  const data = await pageDictionaryContents({ pageNo: 1, pageSize: 100 })
  const all = data.records || []
  itemList.value = all.filter((i) => i.dictionaryId === dict.id)
}

function openCreateItem() {
  itemError.value = ''
  itemForm.value = {
    id: '',
    dictionaryId: currentDict.value?.id || '',
    dictionaryContentText: '',
    dictionaryContentValue: '',
    dictionaryContentSort: 0,
    dictionaryContentStatus: 1
  }
  itemModalVisible.value = true
}

function editItem(item) {
  itemError.value = ''
  itemForm.value = { ...item }
  itemModalVisible.value = true
}

async function submitItem() {
  if (!itemForm.value.dictionaryContentText?.trim()) {
    itemError.value = '字典项文本不能为空'
    return
  }
  if (!itemForm.value.dictionaryContentValue?.trim()) {
    itemError.value = '字典项值不能为空'
    return
  }
  if (Number(itemForm.value.dictionaryContentSort) < 0) {
    itemError.value = '排序不能小于0'
    return
  }
  if (![0, 1].includes(Number(itemForm.value.dictionaryContentStatus))) {
    itemError.value = '状态值仅支持0或1'
    return
  }
  itemSaving.value = true
  itemError.value = ''
  try {
    await saveDictionaryContent({
      ...itemForm.value,
      dictionaryId: currentDict.value?.id || itemForm.value.dictionaryId,
      dictionaryContentSort: Number(itemForm.value.dictionaryContentSort),
      dictionaryContentStatus: Number(itemForm.value.dictionaryContentStatus)
    })
    itemModalVisible.value = false
    if (currentDict.value) await openItems(currentDict.value)
  } catch (e) {
    itemError.value = e.message || '保存失败'
  } finally {
    itemSaving.value = false
  }
}

function removeItem(item) {
  confirmText.value = `确认删除字典项“${item.dictionaryContentText}”？`
  confirmAction.value = async () => {
    await deleteDictionaryContent(item.id)
    if (currentDict.value) {
      await openItems(currentDict.value)
    }
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
    <h3 class="section-title">字典管理</h3>
    <p class="section-subtitle">新增/编辑字典与字典项均采用独立弹窗，字段与表结构一致</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <button class="btn" @click="openCreateDict">新增字典</button>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>名称</th><th>编码</th><th>描述</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="4"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.dictionaryName }}</td>
              <td>{{ item.dictionaryCode }}</td>
              <td>{{ item.dictionaryDescription || '-' }}</td>
              <td>
                <button class="btn ghost" @click="editDict(item)">编辑</button>
                <button class="btn ghost ml-8" @click="openItems(item)">字典项</button>
                <button class="btn danger ml-8" @click="removeDict(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="4"><div class="empty-state">暂无字典数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <DrawerPanel :visible="drawerVisible" :title="`字典项管理 - ${currentDict?.dictionaryName || ''}`" @close="drawerVisible = false">
      <div class="toolbar item-grid">
        <button class="btn col-span-1" @click="openCreateItem">新增字典项</button>
      </div>
      <table class="table">
        <thead><tr><th>文本</th><th>值</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in itemList" :key="item.id">
            <td>{{ item.dictionaryContentText }}</td>
            <td>{{ item.dictionaryContentValue }}</td>
            <td>{{ item.dictionaryContentSort ?? 0 }}</td>
            <td>{{ Number(item.dictionaryContentStatus) === 1 ? '启用' : '禁用' }}</td>
            <td>
              <button class="btn ghost" @click="editItem(item)">编辑</button>
              <button class="btn danger ml-8" @click="removeItem(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!itemList.length"><td colspan="5"><div class="empty-state">暂无字典项</div></td></tr>
        </tbody>
      </table>
    </DrawerPanel>
    <FormModal :visible="dictModalVisible" :title="dictForm.id ? '编辑字典' : '新增字典'" :loading="dictSaving" @confirm="submitDict" @cancel="dictModalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>字典名称</label>
          <input v-model="dictForm.dictionaryName" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>字典编码</label>
          <input v-model="dictForm.dictionaryCode" class="input" />
        </div>
        <div class="form-item span-2">
          <label class="form-label">字典描述</label>
          <input v-model="dictForm.dictionaryDescription" class="input" />
        </div>
      </div>
      <p v-if="dictError" class="form-error">{{ dictError }}</p>
    </FormModal>
    <FormModal :visible="itemModalVisible" :title="itemForm.id ? '编辑字典项' : '新增字典项'" :loading="itemSaving" @confirm="submitItem" @cancel="itemModalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>字典项文本</label>
          <input v-model="itemForm.dictionaryContentText" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>字典项值</label>
          <input v-model="itemForm.dictionaryContentValue" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>排序（数字）</label>
          <input v-model.number="itemForm.dictionaryContentSort" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="itemForm.dictionaryContentStatus" class="select">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </div>
      </div>
      <p v-if="itemError" class="form-error">{{ itemError }}</p>
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

<style scoped>
.item-grid {
  display: flex;
}
</style>



