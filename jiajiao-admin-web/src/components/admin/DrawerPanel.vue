<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '详情' },
  width: { type: String, default: '480px' }
})

const emit = defineEmits(['close'])
</script>

<template>
  <div v-if="visible" class="drawer-mask" @click.self="emit('close')">
    <aside class="drawer" :style="{ width }">
      <header class="drawer-header">
        <strong>{{ title }}</strong>
        <button class="btn ghost" @click="emit('close')">关闭</button>
      </header>
      <section class="drawer-body">
        <slot />
      </section>
    </aside>
  </div>
</template>

<style scoped>
.drawer-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(19, 38, 66, 0.32);
  display: flex;
  justify-content: flex-end;
  backdrop-filter: blur(4px);
}
.drawer {
  height: 100%;
  background: #ffffff;
  border-left: 1px solid var(--line);
  box-shadow: -10px 0 28px rgba(26, 46, 78, 0.14);
}
.drawer-header {
  height: 58px;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: linear-gradient(180deg, #fbfdff, #f5f9ff);
}
.drawer-body {
  padding: 16px;
  overflow: auto;
  height: calc(100% - 58px);
}
</style>
