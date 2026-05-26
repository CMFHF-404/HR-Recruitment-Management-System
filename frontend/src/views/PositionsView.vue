<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">岗位管理</h1>
        <p class="page-subtitle">维护招聘岗位信息，岗位已有候选人时不能删除，可关闭岗位。</p>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="岗位/部门关键词" :prefix-icon="Search" @keyup.enter="load" />
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增岗位</el-button>
      </div>
    </div>

    <div class="panel">
      <div class="table-scroll">
        <el-table v-loading="loading" :data="page.content" stripe>
          <template #empty>
            <el-empty description="暂无岗位，请先新增岗位" />
          </template>
          <el-table-column prop="name" label="岗位名称" min-width="150" />
        <el-table-column prop="department" label="所属部门" width="130" />
        <el-table-column prop="headcount" label="招聘人数" width="100" />
        <el-table-column prop="publishDate" label="发布时间" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ positionStatusText[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requirements" label="岗位要求" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" :disabled="row.status === 'CLOSED'" @click="closePosition(row)">关闭</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        </el-table>
      </div>
      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="page.totalElements"
        :page-size="query.size"
        :current-page="query.page + 1"
        @current-change="changePage"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑岗位' : '新增岗位'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="岗位名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属部门" prop="department"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="招聘人数" prop="headcount"><el-input-number v-model="form.headcount" :min="1" /></el-form-item>
        <el-form-item label="发布时间" prop="publishDate"><el-date-picker v-model="form.publishDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="岗位状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="招聘中" value="OPEN" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位要求" prop="requirements"><el-input v-model="form.requirements" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { api, positionStatusText, statusType } from '../api'

const loading = ref(false)
const keyword = ref('')
const page = ref({ content: [], totalElements: 0 })
const query = reactive({ page: 0, size: 10 })
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const form = reactive({ name: '', department: '', headcount: 1, requirements: '', publishDate: '', status: 'OPEN' })
const rules = {
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
  headcount: [{ required: true, message: '请输入招聘人数', trigger: 'change' }],
  requirements: [{ required: true, message: '请输入岗位要求', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    page.value = await api.get('/positions', { params: { keyword: keyword.value, page: query.page, size: query.size } })
  } finally {
    loading.value = false
  }
}

function changePage(pageNo) {
  query.page = pageNo - 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', department: '', headcount: 1, requirements: '', publishDate: '', status: 'OPEN' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  if (editingId.value) await api.put(`/positions/${editingId.value}`, form)
  else await api.post('/positions', form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function closePosition(row) {
  await api.patch(`/positions/${row.id}/close`)
  ElMessage.success('岗位已关闭')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除岗位“${row.name}”？`, '删除确认', { type: 'warning' })
  await api.delete(`/positions/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
