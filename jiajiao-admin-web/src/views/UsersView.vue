<script setup>
import { onMounted, ref } from 'vue'
import { pageUsers } from '../api'

const keyword = ref('')
const list = ref([])

async function load() {
  const data = await pageUsers({ pageNo: 1, pageSize: 20, keyword: keyword.value || undefined })
  list.value = data.records || []
}

onMounted(load)
</script>

<template>
  <div>
    <h3>用户管理</h3>
    <div class="toolbar">
      <input v-model="keyword" class="input" placeholder="姓名/账号/手机号" />
      <button class="btn" @click="load">查询</button>
    </div>
    <table class="table">
      <thead><tr><th>姓名</th><th>账号</th><th>手机号</th><th>类型</th><th>状态</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.userName }}</td>
          <td>{{ item.userAccount }}</td>
          <td>{{ item.userPhone }}</td>
          <td>{{ item.userType === 0 ? '家长' : '教员' }}</td>
          <td>{{ item.userStatus === 1 ? '启用' : '禁用' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
</style>
