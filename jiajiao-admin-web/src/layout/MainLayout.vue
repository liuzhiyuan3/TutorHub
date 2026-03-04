<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const menus = [
  { path: '/', label: '控制台' },
  { path: '/users', label: '用户管理' },
  { path: '/teacher-audit', label: '教员审核' },
  { path: '/requirements', label: '需求管理' },
  { path: '/orders', label: '订单管理' },
  { path: '/subjects', label: '学科管理' },
  { path: '/schools', label: '学校管理' },
  { path: '/regions', label: '区域管理' }
]

function logout() {
  localStorage.removeItem('admin_token')
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">家教平台管理端</div>
      <router-link v-for="m in menus" :key="m.path" class="menu-item" :to="m.path">{{ m.label }}</router-link>
    </aside>
    <main class="main">
      <header class="header card">
        <div>MVP 后台管理</div>
        <button class="btn secondary" @click="logout">退出登录</button>
      </header>
      <section class="content card">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 220px;
  background: linear-gradient(160deg, #0f172a, #1e3a8a);
  color: #fff;
  padding: 20px 14px;
}
.brand {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 18px;
}
.menu-item {
  display: block;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  color: #dbeafe;
}
.menu-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}
.main {
  flex: 1;
  padding: 18px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
}
.content {
  margin-top: 14px;
  padding: 16px;
}
</style>
