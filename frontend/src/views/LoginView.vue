<template>
  <main class="login-page">
    <section class="login-copy">
      <div class="brand-mark">HR</div>
      <h1>小型企业招聘管理信息系统</h1>
      <p>覆盖岗位发布、候选人登记、简历筛选、面试安排、录用登记和招聘统计，帮助 HR 把招聘流程管理清楚。</p>
      <div class="flow-strip">
        <span>岗位</span>
        <span>候选人</span>
        <span>筛选</span>
        <span>面试</span>
        <span>录用</span>
      </div>
    </section>

    <el-form ref="formRef" class="login-card" :model="form" :rules="rules" @keyup.enter="submit">
      <h2>管理员登录</h2>
      <p>默认账号：admin / admin123</p>
      <el-form-item prop="username">
        <el-input v-model="form.username" size="large" placeholder="用户名" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" size="large" placeholder="密码" type="password" show-password :prefix-icon="Lock" />
      </el-form-item>
      <el-button type="primary" size="large" :loading="loading" @click="submit">登录系统</el-button>
    </el-form>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import { api } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await api.post('/auth/login', form)
    localStorage.setItem('hrms_token', data.token)
    localStorage.setItem('hrms_user', JSON.stringify({ username: data.username, name: data.name }))
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  align-items: center;
  gap: 48px;
  padding: 7vw;
  background:
    linear-gradient(135deg, rgba(13, 148, 136, 0.12), transparent 34%),
    linear-gradient(180deg, #f8fbff 0%, #eef4f8 100%);
  overflow-x: hidden;
}

.brand-mark {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #ffffff;
  background: #0f766e;
  font-weight: 800;
}

.login-copy {
  min-width: 0;
  max-width: 100%;
}

.login-copy h1 {
  max-width: 680px;
  margin: 24px 0 16px;
  font-size: clamp(34px, 5vw, 60px);
  line-height: 1.08;
  letter-spacing: 0;
  color: #172033;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.login-copy p {
  max-width: 620px;
  color: #475569;
  font-size: 18px;
  line-height: 1.8;
  overflow-wrap: anywhere;
}

.flow-strip {
  margin-top: 34px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  max-width: 100%;
}

.flow-strip span {
  padding: 9px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  color: #334155;
}

.login-card {
  background: #ffffff;
  border: 1px solid #dbe4ee;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
  min-width: 0;
}

.login-card h2 {
  margin: 0;
  font-size: 24px;
}

.login-card p {
  margin: 8px 0 24px;
  color: #64748b;
}

.login-card .el-button {
  width: 100%;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 32px 20px;
  }

  .login-copy h1 {
    max-width: 100%;
    font-size: 30px;
    line-height: 1.18;
    word-break: break-all;
    overflow-wrap: anywhere;
  }

  .login-copy p {
    max-width: 100%;
    font-size: 16px;
  }

  .login-card {
    width: 100%;
    padding: 28px 24px;
  }
}
</style>
