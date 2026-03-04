<script setup>
import { onMounted, ref } from 'vue'
import { pageRegions, saveRegion } from '../api'

const list = ref([])
const form = ref({
  regionName: '',
  regionCode: '',
  regionCity: '北京',
  regionProvince: '北京',
  regionSort: 0,
  regionStatus: 1,
  regionDeleteStatus: 0
})

async function load() {
  const data = await pageRegions({ pageNo: 1, pageSize: 50 })
  list.value = data.records || []
}

async function submit() {
  await saveRegion(form.value)
  form.value.regionName = ''
  form.value.regionCode = ''
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h3>区域管理</h3>
    <div class="toolbar">
      <input v-model="form.regionName" class="input" placeholder="区域名称" />
      <input v-model="form.regionCode" class="input" placeholder="区域编码" />
      <button class="btn" @click="submit">新增</button>
    </div>
    <table class="table">
      <thead><tr><th>区域</th><th>编码</th><th>城市</th><th>省份</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.regionName }}</td>
          <td>{{ item.regionCode }}</td>
          <td>{{ item.regionCity }}</td>
          <td>{{ item.regionProvince }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.toolbar { display: grid; grid-template-columns: 1fr 1fr auto; gap: 10px; margin-bottom: 14px; }
</style>
