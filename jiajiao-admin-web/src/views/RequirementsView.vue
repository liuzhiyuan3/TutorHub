<script setup>
import { onMounted, ref } from 'vue'
import { pageRequirements } from '../api'

const list = ref([])

async function load() {
  const data = await pageRequirements({ pageNo: 1, pageSize: 20 })
  list.value = data.records || []
}

onMounted(load)
</script>

<template>
  <div>
    <h3>需求管理</h3>
    <table class="table">
      <thead><tr><th>标题</th><th>年级</th><th>薪资</th><th>状态</th><th>地址</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.requirementTitle }}</td>
          <td>{{ item.requirementGrade }}</td>
          <td>{{ item.requirementSalary }}</td>
          <td>{{ ['待接单', '已接单', '已完成', '已取消'][item.requirementStatus || 0] }}</td>
          <td>{{ item.requirementAddress }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
