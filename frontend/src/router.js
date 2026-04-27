import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import AppShell from './views/AppShell.vue'
import DashboardView from './views/DashboardView.vue'
import PositionsView from './views/PositionsView.vue'
import CandidatesView from './views/CandidatesView.vue'
import WorkflowView from './views/WorkflowView.vue'
import ManagerReviewsView from './views/ManagerReviewsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AppShell,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView, meta: { roles: ['HR'] } },
        { path: 'positions', component: PositionsView, meta: { roles: ['MANAGER'] } },
        { path: 'candidates', component: CandidatesView, meta: { roles: ['HR'] } },
        { path: 'workflow', component: WorkflowView, meta: { roles: ['HR'] } },
        { path: 'manager-reviews', component: ManagerReviewsView, meta: { roles: ['MANAGER'] } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('hrms_token')
  const user = JSON.parse(localStorage.getItem('hrms_user') || '{}')
  const currentRole = user.role || 'HR'
  if (to.path !== '/login' && !token) return '/login'
  if (to.path === '/login' && token) return currentRole === 'MANAGER' ? '/positions' : '/dashboard'
  if (to.meta.roles && !to.meta.roles.includes(currentRole)) {
    return currentRole === 'MANAGER' ? '/positions' : '/dashboard'
  }
  return true
})

export default router
