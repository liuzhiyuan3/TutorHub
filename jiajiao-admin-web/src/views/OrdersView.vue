<script setup>
import { onMounted, ref } from 'vue'
import { pageOrders, updateOrderStatus } from '../api'

const list = ref([])

async function load() {
  const data = await pageOrders({ pageNo: 1, pageSize: 20 })
  list.value = data.records || []
}

async function updateStatus(id, status) {
  await updateOrderStatus(id, { orderStatus: status, orderRemark: '' })
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h3>订单管理</h3>
    <table class="table">
      <thead><tr><th>订单号</th><th>金额</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.orderNumber }}</td>
          <td>{{ item.orderAmount }}</td>
          <td>{{ ['待确认', '进行中', '已完成', '已取消'][item.orderStatus || 0] }}</td>
          <td>
            <button class="btn" @click="updateStatus(item.id, 1)">进行中</button>
            <button class="btn secondary" style="margin-left:8px" @click="updateStatus(item.id, 2)">完成</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
