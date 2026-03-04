<script setup>
import { onMounted, ref } from 'vue'
import { pageSchools, saveSchool } from '../api'

const list = ref([])
const form = ref({
  schoolName: '',
  schoolCode: '',
  schoolType: 0,
  schoolProvince: '北京',
  schoolCity: '北京',
  schoolDistrict: '海淀区',
  schoolAddress: '',
  schoolStatus: 1,
  schoolDeleteStatus: 0
})

async function load() {
  const data = await pageSchools({ pageNo: 1, pageSize: 50 })
  list.value = data.records || []
}

async function submit() {
  await saveSchool(form.value)
  form.value.schoolName = ''
  form.value.schoolCode = ''
  form.value.schoolAddress = ''
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h3>学校管理</h3>
    <div class="toolbar">
      <input v-model="form.schoolName" class="input" placeholder="学校名称" />
      <input v-model="form.schoolCode" class="input" placeholder="学校编码" />
      <input v-model="form.schoolAddress" class="input" placeholder="学校地址" />
      <button class="btn" @click="submit">新增</button>
    </div>
    <table class="table">
      <thead><tr><th>名称</th><th>编码</th><th>城市</th><th>地址</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.schoolName }}</td>
          <td>{{ item.schoolCode }}</td>
          <td>{{ item.schoolCity }}</td>
          <td>{{ item.schoolAddress }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.toolbar { display: grid; grid-template-columns: 1fr 1fr 1.2fr auto; gap: 10px; margin-bottom: 14px; }
</style>
