<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'info' },
  text: { type: String, default: '-' }
})

const cls = computed(() => `status-tag ${props.type || 'info'}`)
</script>

<template>
  <span :class="cls">{{ text }}</span>
</template>
