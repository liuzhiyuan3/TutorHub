<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '确认继续吗？' },
  confirmText: { type: String, default: '确认' },
  danger: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <div v-if="visible" class="modal-mask" @click.self="emit('cancel')">
    <div class="modal-card">
      <h3 class="dialog-title">{{ title }}</h3>
      <p class="dialog-message">{{ message }}</p>
      <div class="dialog-actions">
        <button class="btn ghost" @click="emit('cancel')">取消</button>
        <button :class="['btn', danger ? 'danger' : '']" @click="emit('confirm')">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-title {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--text-main);
}
.dialog-message {
  margin: 0 0 16px;
  color: var(--text-sub);
  line-height: 1.5;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 2px;
}
</style>
