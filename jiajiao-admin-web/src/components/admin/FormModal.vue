<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '新增' },
  confirmText: { type: String, default: '保存' },
  loading: { type: Boolean, default: false },
  width: { type: String, default: '760px' }
})

const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <div v-if="visible" class="modal-mask" @click.self="emit('cancel')">
    <div class="modal-card" :style="{ width }">
      <h3 class="form-modal-title">{{ title }}</h3>
      <div class="form-modal-body">
        <slot />
      </div>
      <div class="form-modal-footer">
        <button class="btn ghost" :disabled="loading" @click="emit('cancel')">取消</button>
        <button class="btn" :disabled="loading" @click="emit('confirm')">{{ loading ? '保存中...' : confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-modal-title {
  margin: 0 0 14px;
}
.form-modal-body {
  max-height: min(58vh, 560px);
  overflow: auto;
  padding-right: 4px;
}
.form-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
</style>
