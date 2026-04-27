<template>
  <el-container class="shell">
    <el-aside class="sidebar" width="232px">
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
        <div>
          <strong>{{ title }}</strong>
          <span>{{ roleText[currentRole] || '本地课程设计演示系统' }}</span>
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
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Briefcase, DataAnalysis, Operation, User, UserFilled } from '@element-plus/icons-vue'

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
  { path: '/manager-reviews', label: '主管确认', icon: Operation, roles: ['MANAGER'] },
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
  background: #102a43;
  color: #ffffff;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
}

.brand span {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #14b8a6;
  font-weight: 800;
}

.brand strong {
  font-size: 18px;
}

.menu {
  border-right: 0;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #d9e2ec;
}

.menu :deep(.el-menu-item.is-active),
.menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background: rgba(20, 184, 166, 0.22);
}

.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
}

.topbar strong {
  display: block;
}

.topbar span {
  color: #64748b;
  font-size: 13px;
}

.main {
  background: #f4f7fb;
  padding: 24px;
}

@media (max-width: 760px) {
  .shell {
    display: block;
  }

  .sidebar {
    width: 100% !important;
  }

  .brand {
    height: 58px;
  }

  .main {
    padding: 16px;
  }
}
</style>
