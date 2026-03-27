<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { pageMenus } from '../api'
import { adminProfile, clearAdminSession } from '../stores/adminSession'
import { resolveMenuIcon } from '../utils/menuIconResolver'

const router = useRouter()
const route = useRoute()
const menuIconsByPath = ref({})

const groups = [
  {
    title: '业务管理',
    items: [
      { path: '/', label: '控制台', iconKey: 'LayoutDashboard' },
      { path: '/users', label: '用户管理', iconKey: 'Users' },
      { path: '/teacher-audit', label: '教员审核', iconKey: 'ShieldCheck' },
      { path: '/requirements', label: '需求管理', iconKey: 'ClipboardList' },
      { path: '/orders', label: '订单管理', iconKey: 'FolderKanban' }
    ]
  },
  {
    title: '内容管理',
    items: [
      { path: '/subject-categories', label: '学科分类', iconKey: 'BookOpen' },
      { path: '/subjects', label: '学科管理', iconKey: 'BookOpen' },
      { path: '/schools', label: '学校管理', iconKey: 'GraduationCap' },
      { path: '/regions', label: '区域管理', iconKey: 'MapPinned' },
      { path: '/slides', label: '轮播图管理', iconKey: 'MonitorCog' },
      { path: '/advertisings', label: '广告管理', iconKey: 'MonitorCog' }
    ]
  },
  {
    title: '系统管理',
    items: [
      { path: '/roles', label: '角色管理', iconKey: 'ShieldCheck' },
      { path: '/menus', label: '菜单管理', iconKey: 'SquareMenu' },
      { path: '/role-menus', label: '角色菜单', iconKey: 'SquareMenu' },
      { path: '/dictionaries', label: '字典管理', iconKey: 'SlidersHorizontal' },
      { path: '/stats', label: '统计看板', iconKey: 'ChartNoAxesCombined' }
    ]
  }
]

const normalizedGroups = computed(() => {
  return groups.map((group) => {
    return {
      ...group,
      items: group.items.map((item) => {
        const serverIcon = menuIconsByPath.value[item.path]
        return { ...item, iconName: serverIcon || item.iconKey }
      })
    }
  })
})

const pageTitle = computed(() => route.meta?.title || '控制台')
const breadcrumb = computed(() => `首页 / ${route.meta?.title || '控制台'}`)
const adminName = computed(() => adminProfile.value?.name || adminProfile.value?.account || '管理员')

function iconOf(name) {
  return resolveMenuIcon(name, 'Circle')
}

async function loadMenuIconMapping() {
  try {
    const response = await pageMenus({ pageNo: 1, pageSize: 200 })
    const records = response?.records || []
    const map = {}
    records.forEach((item) => {
      const link = (item.menuLink || '').trim()
      const icon = (item.menuIcon || '').trim()
      if (!link || !icon) return
      const normalizedPath = link.startsWith('/') ? link : `/${link}`
      map[normalizedPath] = icon
    })
    menuIconsByPath.value = map
  } catch (_) {
    menuIconsByPath.value = {}
  }
}

function logout() {
  clearAdminSession()
  router.replace('/login')
}

onMounted(loadMenuIconMapping)
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand-wrap">
        <div class="brand-mark">TC</div>
        <div>
          <div class="brand">家教平台管理端</div>
          <div class="brand-sub">Teacher Console</div>
        </div>
      </div>
      <section v-for="group in normalizedGroups" :key="group.title" class="menu-group">
        <p class="group-title">{{ group.title }}</p>
        <router-link v-for="m in group.items" :key="m.path" class="menu-item" :to="m.path">
          <component :is="iconOf(m.iconName)" class="menu-icon" />
          <span>{{ m.label }}</span>
        </router-link>
      </section>
    </aside>
    <main class="main">
      <header class="header card">
        <div>
          <div class="header-breadcrumb">{{ breadcrumb }}</div>
          <div class="header-title">{{ pageTitle }}</div>
          <div class="header-sub">家教平台管理后台 · 业务与运营中心</div>
        </div>
        <div class="header-actions">
          <span class="admin-chip">{{ adminName }}</span>
          <button class="btn secondary" @click="logout">退出登录</button>
        </div>
      </header>
      <section class="content">
        <div class="content-shell card">
          <router-view />
        </div>
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
  width: 256px;
  background: linear-gradient(170deg, #1e3a8a, #1d4ed8 56%, #1554c7);
  color: #fff;
  padding: 18px 12px 14px;
  overflow: auto;
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.12);
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 4px 8px 14px;
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.8px;
}

.brand {
  font-size: 18px;
  font-weight: 800;
  line-height: 1.1;
}

.brand-sub {
  margin-top: 3px;
  font-size: 11px;
  color: rgba(219, 234, 254, 0.86);
}

.menu-group {
  margin-bottom: 14px;
}

.group-title {
  margin: 0 0 6px;
  padding-left: 10px;
  color: #dbeafe;
  font-size: 12px;
  letter-spacing: 0.6px;
  opacity: 0.9;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  margin-bottom: 6px;
  color: #e7f1ff;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.menu-icon {
  width: 16px;
  height: 16px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.14);
}

.menu-item.router-link-active {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.44);
  color: #fff;
}

.main {
  flex: 1;
  padding: 18px 18px 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  min-height: 82px;
  border-radius: 18px;
}

.header-breadcrumb {
  color: var(--text-light);
  font-size: 12px;
  margin-bottom: 2px;
}

.header-title {
  font-size: 20px;
  font-weight: 800;
}

.header-sub {
  margin-top: 2px;
  color: var(--text-sub);
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.admin-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #f6faff;
  color: #30527c;
  font-size: 12px;
  font-weight: 700;
}

.content {
  margin-top: 12px;
}

.content-shell {
  border-radius: 20px;
  overflow: hidden;
  min-height: calc(100vh - 126px);
}

@media (max-width: 1024px) {
  .sidebar {
    width: 220px;
  }
  .main {
    padding: 14px;
  }
}

@media (max-width: 860px) {
  .layout {
    display: block;
  }
  .sidebar {
    width: 100%;
    max-height: 240px;
  }
  .main {
    padding: 12px;
  }
  .content-shell {
    min-height: auto;
  }
}
</style>
