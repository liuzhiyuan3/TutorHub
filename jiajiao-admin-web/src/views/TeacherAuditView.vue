<script setup>
import { onMounted, ref } from 'vue'
import { auditTeacher, pageTeachers } from '../api'

const list = ref([])

async function load() {
  const data = await pageTeachers({ pageNo: 1, pageSize: 20 })
  list.value = data.records || []
}

async function doAudit(id, status) {
  await auditTeacher(id, status)
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h3>教员审核</h3>
    <table class="table">
      <thead><tr><th>身份</th><th>学校</th><th>专业</th><th>教龄</th><th>审核状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.teacherIdentity }}</td>
          <td>{{ item.teacherSchool || '-' }}</td>
          <td>{{ item.teacherMajor || '-' }}</td>
          <td>{{ item.teacherTeachingYears }}</td>
          <td>{{ ['待审', '通过', '拒绝'][item.teacherAuditStatus || 0] }}</td>
          <td>
            <button class="btn" @click="doAudit(item.id, 1)">通过</button>
            <button class="btn secondary" style="margin-left:8px" @click="doAudit(item.id, 2)">拒绝</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
