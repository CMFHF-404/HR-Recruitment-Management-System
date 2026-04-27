import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import AppShell from './views/AppShell.vue'
import DashboardView from './views/DashboardView.vue'
import PositionsView from './views/PositionsView.vue'
import CandidatesView from './views/CandidatesView.vue'
import WorkflowView from './views/WorkflowView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AppShell,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView },
        { path: 'positions', component: PositionsView },
        { path: 'candidates', component: CandidatesView },
        { path: 'workflow', component: WorkflowView },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('hrms_token')
  if (to.path !== '/login' && !token) return '/login'
  if (to.path === '/login' && token) return '/dashboard'
  return true
})

export default router
