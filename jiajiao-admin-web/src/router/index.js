import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layout/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import UsersView from '../views/UsersView.vue'
import TeacherAuditView from '../views/TeacherAuditView.vue'
import RequirementsView from '../views/RequirementsView.vue'
import OrdersView from '../views/OrdersView.vue'
import SubjectsView from '../views/SubjectsView.vue'
import SchoolsView from '../views/SchoolsView.vue'
import RegionsView from '../views/RegionsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', component: DashboardView },
        { path: 'users', component: UsersView },
        { path: 'teacher-audit', component: TeacherAuditView },
        { path: 'requirements', component: RequirementsView },
        { path: 'orders', component: OrdersView },
        { path: 'subjects', component: SubjectsView },
        { path: 'schools', component: SchoolsView },
        { path: 'regions', component: RegionsView }
      ]
    }
  ]
})

router.beforeEach((to, _, next) => {
  if (to.path === '/login') {
    next()
    return
  }
  const token = localStorage.getItem('admin_token')
  if (!token) {
    next('/login')
    return
  }
  next()
})

export default router
