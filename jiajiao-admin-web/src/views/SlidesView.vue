<script setup>
import { onMounted, ref } from 'vue'
import { deleteSlide, pageSlides, saveSlide, uploadImage } from '../api'
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
  slidePicture: '',
  slideLink: '',
  slideNote: '',
  slidePriority: 0,
  slideStatus: 1,
  slideModule: 0,
  slideDeleteStatus: 0
})
const confirmVisible = ref(false)
const confirmAction = ref(null)
const confirmText = ref('')
const uploading = ref(false)
const fileInputRef = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await pageSlides({ pageNo: 1, pageSize: 100 })
    list.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  formError.value = ''
  form.value = {
    id: '',
    slidePicture: '',
    slideLink: '',
    slideNote: '',
    slidePriority: 0,
    slideStatus: 1,
    slideModule: 0,
    slideDeleteStatus: 0
  }
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
    const data = await uploadImage(file, 'admin-slide')
    form.value.slidePicture = data.url
  } catch (e) {
    formError.value = e.message || '上传失败'
  } finally {
    uploading.value = false
  }
}

async function submit() {
  if (!form.value.slidePicture?.trim()) {
    formError.value = '轮播图图片地址不能为空'
    return
  }
  if (Number(form.value.slidePriority) < 0) {
    formError.value = '优先级不能小于0'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await saveSlide({
      ...form.value,
      slidePriority: Number(form.value.slidePriority),
      slideStatus: Number(form.value.slideStatus),
      slideModule: Number(form.value.slideModule)
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
  confirmText.value = `确认删除轮播图“${item.slideNote || item.id}”？`
  confirmAction.value = async () => {
    await deleteSlide(item.id)
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
    <h3 class="section-title">轮播图管理</h3>
    <p class="section-subtitle">新增与编辑使用独立弹窗，字段与 slide 表一致</p>
    <div class="panel panel-mt-12">
      <div class="panel-body">
        <button class="btn" @click="openCreate">新增轮播图</button>
      </div>
    </div>
    <div class="panel panel-mt-12">
      <div class="panel-body panel-body-pt-0">
        <table class="table">
          <thead><tr><th>备注</th><th>图片预览</th><th>图片URL</th><th>跳转链接</th><th>状态</th><th>优先级</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="7"><span class="skeleton" /></td></tr>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.slideNote || '-' }}</td>
              <td>
                <img
                  v-if="item.slidePicture"
                  :src="item.slidePicture"
                  alt="slide"
                  class="img-thumb-sm"
                />
                <span v-else>-</span>
              </td>
              <td>{{ item.slidePicture }}</td>
              <td>{{ item.slideLink || '-' }}</td>
              <td><StatusTag :type="item.slideStatus === 1 ? 'success' : 'error'" :text="item.slideStatus === 1 ? '启用' : '禁用'" /></td>
              <td>{{ item.slidePriority ?? 0 }}</td>
              <td>
                <button class="btn ghost" @click="edit(item)">编辑</button>
                <button class="btn danger ml-8" @click="remove(item)">删除</button>
              </td>
            </tr>
            <tr v-if="!loading && !list.length"><td colspan="7"><div class="empty-state">暂无轮播图数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <FormModal :visible="modalVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" :loading="saving" @confirm="submit" @cancel="modalVisible = false">
      <div class="form-grid">
        <div class="form-item span-2">
          <label class="form-label"><span class="required">*</span>图片地址</label>
          <input v-model="form.slidePicture" class="input" />
          <div class="upload-row">
            <button class="btn ghost" type="button" :disabled="uploading" @click="openUploadPicker">
              {{ uploading ? '上传中...' : '选择图片上传' }}
            </button>
            <img
              v-if="form.slidePicture"
              :src="form.slidePicture"
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
          <input v-model="form.slideLink" class="input" />
        </div>
        <div class="form-item span-2">
          <label class="form-label">备注</label>
          <input v-model="form.slideNote" class="input" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>优先级（数字）</label>
          <input v-model.number="form.slidePriority" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>模块（数字）</label>
          <input v-model.number="form.slideModule" class="input" type="number" min="0" step="1" />
        </div>
        <div class="form-item">
          <label class="form-label"><span class="required">*</span>状态</label>
          <select v-model.number="form.slideStatus" class="select">
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




