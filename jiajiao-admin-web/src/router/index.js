import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layout/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import UsersView from '../views/UsersView.vue'
import TeacherAuditView from '../views/TeacherAuditView.vue'
import RequirementsView from '../views/RequirementsView.vue'
import OrdersView from '../views/OrdersView.vue'
import SubjectsView from '../views/SubjectsView.vue'
import SubjectCategoriesView from '../views/SubjectCategoriesView.vue'
import SchoolsView from '../views/SchoolsView.vue'
import RegionsView from '../views/RegionsView.vue'
import RolesView from '../views/RolesView.vue'
import MenusView from '../views/MenusView.vue'
import RoleMenusView from '../views/RoleMenusView.vue'
import DictionariesView from '../views/DictionariesView.vue'
import SlidesView from '../views/SlidesView.vue'
import AdvertisingsView from '../views/AdvertisingsView.vue'
import StatsView from '../views/StatsView.vue'
import { bootstrapAdminSession, clearAdminSession } from '../stores/adminSession'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { title: '登录' } },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', component: DashboardView, meta: { title: '控制台' } },
        { path: 'users', component: UsersView, meta: { title: '用户管理' } },
        { path: 'teacher-audit', component: TeacherAuditView, meta: { title: '教员审核' } },
        { path: 'requirements', component: RequirementsView, meta: { title: '需求管理' } },
        { path: 'orders', component: OrdersView, meta: { title: '订单管理' } },
        { path: 'subjects', component: SubjectsView, meta: { title: '学科管理' } },
        { path: 'subject-categories', component: SubjectCategoriesView, meta: { title: '学科分类' } },
        { path: 'schools', component: SchoolsView, meta: { title: '学校管理' } },
        { path: 'regions', component: RegionsView, meta: { title: '区域管理' } },
        { path: 'roles', component: RolesView, meta: { title: '角色管理' } },
        { path: 'menus', component: MenusView, meta: { title: '菜单管理' } },
        { path: 'role-menus', component: RoleMenusView, meta: { title: '角色菜单' } },
        { path: 'dictionaries', component: DictionariesView, meta: { title: '字典管理' } },
        { path: 'slides', component: SlidesView, meta: { title: '轮播图管理' } },
        { path: 'advertisings', component: AdvertisingsView, meta: { title: '广告管理' } },
        { path: 'stats', component: StatsView, meta: { title: '统计看板' } }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const token = localStorage.getItem('admin_token')

  if (to.path === '/login') {
    if (!token) return true
    try {
      await bootstrapAdminSession()
      return '/'
    } catch (_) {
      clearAdminSession()
      return true
    }
  }

  if (!token) {
    return '/login'
  }

  try {
    await bootstrapAdminSession()
    return true
  } catch (_) {
    clearAdminSession()
    return '/login'
  }
})

export default router
