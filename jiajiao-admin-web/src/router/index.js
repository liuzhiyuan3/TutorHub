import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { bootstrapAdminSession, clearAdminSession } from '../stores/adminSession'

const LoginView = () => import('../views/LoginView.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const UsersView = () => import('../views/UsersView.vue')
const TeacherAuditView = () => import('../views/TeacherAuditView.vue')
const RequirementsView = () => import('../views/RequirementsView.vue')
const OrdersView = () => import('../views/OrdersView.vue')
const SubjectsView = () => import('../views/SubjectsView.vue')
const SubjectCategoriesView = () => import('../views/SubjectCategoriesView.vue')
const SchoolsView = () => import('../views/SchoolsView.vue')
const RegionsView = () => import('../views/RegionsView.vue')
const RolesView = () => import('../views/RolesView.vue')
const MenusView = () => import('../views/MenusView.vue')
const RoleMenusView = () => import('../views/RoleMenusView.vue')
const DictionariesView = () => import('../views/DictionariesView.vue')
const SlidesView = () => import('../views/SlidesView.vue')
const AdvertisingsView = () => import('../views/AdvertisingsView.vue')
const StatsView = () => import('../views/StatsView.vue')

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
