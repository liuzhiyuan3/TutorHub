<script setup>
import { onMounted, ref } from 'vue'
import { deleteAdvertising, pageAdvertisings, saveAdvertising, uploadImage } from '../api'
import ConfirmDialog from '../components/admin/ConfirmDialog.vue'
import StatusTag from '../components/admin/StatusTag.vue'
import FormModal from '../components/admin/FormModal.vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const modalVisible = ref(false)
const formError = ref('')
const form = ref({
  id: '',
  advertisingSource: '',
  advertisingTitle: '',
  advertisingLink: '',
  advertisingPicture: '',
  advertisingStatus: 1,
  advertisingExpireTime: '',
  advertisingDeleteStatus: 0
})
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')
const uploading = ref(false)
const fileInputRef = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await pageAdvertisings({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = {
    id: '',
    advertisingSource: '',
    advertisingTitle: '',
    advertisingLink: '',
    advertisingPicture: '',
    advertisingStatus: 1,
    advertisingExpireTime: '',
    advertisingDeleteStatus: 0
  }
  formError.value = ''
  modalVisible.value = true
}

function openUploadPicker() {
  if (uploading.value) return
  fileInputRef.value?.click()
}

async function onPickFile(event) {
  const file = event.target?.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!/^image\/(jpeg|png|webp)$/i.test(file.type)) {
    formError.value = '仅支持 jpg/png/webp 图片'
    return
  }
  uploading.value = true
  formError.value = ''
  try {
    const data = await uploadImage(file, 'admin-advertising')
    form.value.advertisingPicture = data.url
  } catch (e) {
    formError.value = e.message || '上传失败'
  } finally {
    uploading.value = false
  }
}

async function submit() {
  if (!form.value.advertisingTitle?.trim()) {
    formError.value = '广告标题不能为空'
    return
  }
  if (!form.value.advertisingPicture?.trim()) {
    formError.value = '广告图片不能为空'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await saveAdvertising({
      ...form.value,
      advertisingStatus: Number(form.value.advertisingStatus),
      advertisingExpireTime: form.value.advertisingExpireTime || null
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
  form.value.advertisingExpireTime = form.value.advertisingExpireTime ? String(form.value.advertisingExpireTime).slice(0, 16) : ''
  formError.value = ''
  modalVisible.value = true
}

function remove(item) {
  confirmText.value = `确认删除广告“${item.advertisingTitle}”？`
  confirmAction.value = async () => {
    await deleteAdvertising(item.id)
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
    <h3 class="section-title">广告管理</h3>
    <p class="section-subtitle">独立新增弹窗，字段与 advertising 表对齐</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <button class="btn" @click="openCreate">新增广告</button>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>标题</th><th>来源</th><th>图片预览</th><th>图片URL</th><th>状态</th><th>到期时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.advertisingTitle }}</td>
              <td>{{ item.advertisingSource || '-' }}</td>
              <td>
                <img
                  v-if="item.advertisingPicture"
                  :src="item.advertisingPicture"
                  alt="advertising"
                  class="img-thumb-sm"
                />
                <span v-else>-</span>
              </td>
              <td>{{ item.advertisingPicture }}</td>
              <td><StatusTag :type="item.advertisingStatus === 1 ? 'success' : 'error'" :text="item.advertisingStatus === 1 ? '启用' : '禁用'" /></td>
              <td>{{ item.advertisingExpireTime || '-' }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="7"><div class="empty-state">暂无广告数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑广告' : '新增广告'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>广告标题</label>
          <input v-model="form.advertisingTitle" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label">广告来源</label>
          <input v-model="form.advertisingSource" class="input" />
        </div>
        <div class="form-item span-2">
          <label class="form-label"><span class="required">*</span>广告图片</label>
          <input v-model="form.advertisingPicture" class="input" />
          <div class="upload-row">
            <button class="btn ghost" type="button" :disabled="uploading" @click="openUploadPicker">
              {{ uploading ? '上传中...' : '选择图片上传' }}
            </button>
            <img
              v-if="form.advertisingPicture"
              :src="form.advertisingPicture"
              alt="preview"
              class="img-thumb-md"
            />
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            class="hidden-input"
            @change="onPickFile"
          />
        </div>
        <div class="form-item span-2">
          <label class="form-label">跳转链接</label>
          <input v-model="form.advertisingLink" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.advertisingStatus" class="select">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">到期时间</label>
          <input v-model="form.advertisingExpireTime" class="input" type="datetime-local" />
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




