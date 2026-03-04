<script setup>
import { onMounted, ref } from 'vue'
import { pageSubjects, saveSubject } from '../api'

const list = ref([])
const form = ref({ subjectName: '', subjectCode: '', subjectCategory: '', subjectSort: 0, subjectStatus: 1, subjectDeleteStatus: 0 })

async function load() {
  const data = await pageSubjects({ pageNo: 1, pageSize: 50 })
  list.value = data.records || []
}

async function submit() {
  await saveSubject(form.value)
  form.value = { subjectName: '', subjectCode: '', subjectCategory: '', subjectSort: 0, subjectStatus: 1, subjectDeleteStatus: 0 }
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h3>学科管理</h3>
    <div class="toolbar">
      <input v-model="form.subjectName" class="input" placeholder="学科名称" />
      <input v-model="form.subjectCode" class="input" placeholder="学科编码" />
      <input v-model="form.subjectCategory" class="input" placeholder="学科分类" />
      <button class="btn" @click="submit">新增</button>
    </div>
    <table class="table">
      <thead><tr><th>名称</th><th>编码</th><th>分类</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.subjectName }}</td>
          <td>{{ item.subjectCode }}</td>
          <td>{{ item.subjectCategory }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.toolbar { display: grid; grid-template-columns: 1fr 1fr 1fr auto; gap: 10px; margin-bottom: 14px; }
</style>
