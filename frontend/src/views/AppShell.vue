<template>
  <el-container class="shell">
    <el-aside v-if="!isMobile" class="sidebar" width="232px">
      <div class="brand">
        <span>HR</span>
        <strong>招聘管理</strong>
      </div>
      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon><span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-start">
          <el-button v-if="isMobile" class="menu-trigger" text :icon="Menu" aria-label="打开菜单" @click="mobileMenuOpen = true" />
          <div class="topbar-titles">
            <el-breadcrumb v-if="breadcrumbItems.length" separator="/">
              <el-breadcrumb-item v-for="(bc, i) in breadcrumbItems" :key="i">{{ bc }}</el-breadcrumb-item>
            </el-breadcrumb>
            <strong>{{ title }}</strong>
            <span>{{ roleText[currentRole] || '本地课程设计演示系统' }}</span>
          </div>
        </div>
        <el-dropdown>
          <el-button :icon="User"> {{ user.name || user.username }} / {{ roleText[currentRole] || '用户' }} </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <el-drawer v-if="isMobile" v-model="mobileMenuOpen" direction="ltr" size="min(280px, 88vw)" title="" :show-close="true" class="mobile-drawer">
      <div class="brand drawer-brand">
        <span>HR</span>
        <strong>招聘管理</strong>
      </div>
      <el-menu router :default-active="$route.path" class="menu drawer-menu" @select="onMenuSelectMobile">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon><span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Briefcase, CircleCheck, DataAnalysis, Menu, Operation, User, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const user = JSON.parse(localStorage.getItem('hrms_user') || '{}')
const roleText = {
  HR: 'HR',
  MANAGER: '部门主管',
}
const currentRole = user.role || 'HR'
const allMenus = [
  { path: '/dashboard', label: '统计首页', icon: DataAnalysis, roles: ['HR'] },
  { path: '/positions', label: '岗位管理', icon: Briefcase, roles: ['MANAGER'] },
  { path: '/candidates', label: '候选人管理', icon: UserFilled, roles: ['HR'] },
  { path: '/workflow', label: '招聘流程', icon: Operation, roles: ['HR'] },
  { path: '/manager-reviews', label: '主管确认', icon: CircleCheck, roles: ['MANAGER'] },
]
const menus = computed(() => allMenus.filter((item) => item.roles.includes(currentRole)))
const titles = {
  '/dashboard': '统计首页',
  '/positions': '岗位管理',
  '/candidates': '候选人管理',
  '/workflow': '招聘流程',
  '/manager-reviews': '主管确认',
}
const title = computed(() => titles[route.path] || '招聘管理')
const breadcrumbItems = computed(() => ['工作台', title.value])

const isMobile = ref(false)
const mobileMenuOpen = ref(false)
let mq

function syncMobile() {
  isMobile.value = mq.matches
  if (!isMobile.value) mobileMenuOpen.value = false
}

function onMenuSelectMobile() {
  mobileMenuOpen.value = false
}

onMounted(() => {
  mq = window.matchMedia('(max-width: 760px)')
  syncMobile()
  mq.addEventListener('change', syncMobile)
})

onUnmounted(() => {
  mq?.removeEventListener('change', syncMobile)
})

function logout() {
  localStorage.removeItem('hrms_token')
  localStorage.removeItem('hrms_user')
  router.push('/login')
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
}

.sidebar {
  background: var(--hr-sidebar-bg, #102a43);
  color: #ffffff;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
}

.drawer-brand {
  height: auto;
  padding: 0 4px 16px;
  border-bottom: 1px solid var(--hr-border, #e2e8f0);
  margin-bottom: 8px;
}

.brand span {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--hr-color-primary-light, #14b8a6);
  font-weight: 800;
}

.brand strong {
  font-size: 18px;
}

.menu {
  border-right: 0;
  background: transparent;
}

.drawer-menu {
  border-right: none;
}

.menu :deep(.el-menu-item) {
  color: var(--hr-sidebar-text, #d9e2ec);
}

.menu :deep(.el-menu-item.is-active),
.menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background: rgba(20, 184, 166, 0.22);
}

.drawer-menu :deep(.el-menu-item) {
  color: var(--hr-text, #1e293b);
}

.drawer-menu :deep(.el-menu-item.is-active),
.drawer-menu :deep(.el-menu-item:hover) {
  color: var(--hr-color-primary, #0f766e);
  background: rgba(15, 118, 110, 0.1);
}

.topbar {
  height: auto;
  min-height: 64px;
  flex-wrap: wrap;
  gap: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--hr-border, #e2e8f0);
  background: var(--hr-surface, #ffffff);
  padding: 12px 20px;
}

.topbar-start {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.menu-trigger {
  flex-shrink: 0;
  margin-top: 2px;
}

.topbar-titles {
  min-width: 0;
}

.topbar-titles :deep(.el-breadcrumb) {
  margin-bottom: 4px;
  font-size: 12px;
}

.topbar-titles :deep(.el-breadcrumb__inner) {
  color: var(--hr-text-muted, #64748b);
}

.topbar strong {
  display: block;
}

.topbar span {
  color: var(--hr-text-muted, #64748b);
  font-size: 13px;
}

.main {
  background: var(--hr-page-bg, #f4f7fb);
  padding: 24px;
}

.mobile-drawer :deep(.el-drawer__body) {
  padding-top: 12px;
}

@media (max-width: 760px) {
  .main {
    padding: 16px;
  }
}
</style>
