<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">候选人管理</h1>
        <p class="page-subtitle">登记候选人资料，并可查看从筛选到录用的完整招聘进度。</p>
      </div>
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="姓名/电话/邮箱" :prefix-icon="Search" @keyup.enter="load" />
        <el-select v-model="positionId" clearable placeholder="应聘岗位" style="width: 170px">
          <el-option v-for="item in positions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button :icon="Search" @click="load">查询</el-button>
        <el-button :icon="Download" @click="exportCsv">导出 CSV</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增候选人</el-button>
      </div>
    </div>

    <div class="panel">
      <div class="table-scroll">
        <el-table v-loading="loading" :data="page.content" stripe>
          <template #empty>
            <el-empty description="暂无候选人，可点击新增录入" />
          </template>
          <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="education" label="学历" width="100" />
        <el-table-column prop="school" label="毕业院校" min-width="150" />
        <el-table-column label="应聘岗位" min-width="150">
          <template #default="{ row }">{{ row.position?.name }}</template>
        </el-table-column>
        <el-table-column label="简历附件" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.resumeOriginalFileName || '未上传' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showProgress(row)">进度</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑候选人' : '新增候选人'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学历" prop="education"><el-input v-model="form.education" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="毕业院校" prop="school"><el-input v-model="form.school" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="应聘岗位" prop="positionId">
          <el-select v-model="form.positionId" style="width: 100%" filterable>
            <el-option v-for="item in positions" :key="item.id" :label="`${item.name} / ${item.department}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="简历附件">
          <el-upload
            v-model:file-list="resumeFiles"
            accept=".pdf,.docx,.txt"
            :auto-upload="false"
            :limit="1"
            :on-change="selectResumeFile"
            :on-remove="removeResumeFile"
          >
            <el-button :icon="Upload">选择 PDF/DOCX/TXT</el-button>
            <template #tip>
              <span class="upload-tip">
                {{ form.resumeOriginalFileName ? `当前：${form.resumeOriginalFileName}` : '保存候选人后将自动上传并进行 AI 匹配分析' }}
              </span>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="候选人招聘进度" size="420px">
      <template v-if="progress">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="候选人">{{ progress.candidate.name }}</el-descriptions-item>
          <el-descriptions-item label="应聘岗位">{{ progress.candidate.position?.name }}</el-descriptions-item>
          <el-descriptions-item label="简历筛选">
            <el-tag :type="statusType[progress.screening.status]">{{ screeningStatusText[progress.screening.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="筛选意见">{{ progress.screening.comment || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="AI 匹配度">{{ formatAiScore(progress.screening.aiMatchScore) }}</el-descriptions-item>
          <el-descriptions-item label="AI 快评">{{ progress.screening.aiQuickReview || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="主管确认">
            <el-tag :type="statusType[progress.screening.managerStatus]">{{ managerReviewStatusText[progress.screening.managerStatus] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="主管意见">{{ progress.screening.managerComment || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="面试状态">
            <el-tag :type="statusType[progress.interview.status]">{{ interviewStatusText[progress.interview.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="面试时间">{{ progress.interview.interviewTime || '未安排' }}</el-descriptions-item>
          <el-descriptions-item label="面试评价">{{ progress.interview.evaluation || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="录用结果">
            <el-tag :type="statusType[progress.offer.status]">{{ offerStatusText[progress.offer.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="录用备注">{{ progress.offer.remark || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Search, Upload } from '@element-plus/icons-vue'
import { api, interviewStatusText, managerReviewStatusText, offerStatusText, screeningStatusText, statusType } from '../api'

const loading = ref(false)
const keyword = ref('')
const positionId = ref()
const positions = ref([])
const page = ref({ content: [], totalElements: 0 })
const query = reactive({ page: 0, size: 10 })
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const editingId = ref(null)
const progress = ref(null)
const formRef = ref()
const resumeFiles = ref([])
const selectedResumeFile = ref(null)
const form = reactive({ name: '', gender: '男', phone: '', email: '', education: '', school: '', positionId: null, note: '', resumeOriginalFileName: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确邮箱', trigger: 'blur' }],
  education: [{ required: true, message: '请输入学历', trigger: 'blur' }],
  school: [{ required: true, message: '请输入毕业院校', trigger: 'blur' }],
  positionId: [{ required: true, message: '请选择应聘岗位', trigger: 'change' }],
}

async function loadPositions() {
  const data = await api.get('/positions', { params: { size: 100 } })
  positions.value = data.content
}

async function load() {
  loading.value = true
  try {
    page.value = await api.get('/candidates', { params: { keyword: keyword.value, positionId: positionId.value, page: query.page, size: query.size } })
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
  Object.assign(form, { name: '', gender: '男', phone: '', email: '', education: '', school: '', positionId: positions.value[0]?.id || null, note: '', resumeOriginalFileName: '' })
  clearSelectedResume()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row, positionId: row.position?.id })
  clearSelectedResume()
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  const saved = editingId.value
    ? await api.put(`/candidates/${editingId.value}`, form)
    : await api.post('/candidates', form)
  if (selectedResumeFile.value) {
    const uploadResult = await uploadResume(saved.id)
    if (uploadResult.analysisSucceeded) {
      ElMessage.success('候选人已保存，AI 匹配分析已完成')
    } else {
      ElMessage.warning(uploadResult.analysisMessage || '简历已上传，AI 分析失败，可稍后重试')
    }
  } else {
    ElMessage.success('保存成功')
  }
  dialogVisible.value = false
  load()
}

async function showProgress(row) {
  progress.value = await api.get(`/candidates/${row.id}/progress`)
  drawerVisible.value = true
}

async function exportCsv() {
  const blob = await api.get('/candidates/export', {
    params: { keyword: keyword.value, positionId: positionId.value },
    responseType: 'blob',
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'candidates.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('CSV 已导出')
}

function selectResumeFile(uploadFile) {
  selectedResumeFile.value = uploadFile.raw
  resumeFiles.value = [uploadFile]
}

function removeResumeFile() {
  clearSelectedResume()
}

function clearSelectedResume() {
  selectedResumeFile.value = null
  resumeFiles.value = []
}

async function uploadResume(candidateId) {
  const body = new FormData()
  body.append('file', selectedResumeFile.value)
  return api.post(`/candidates/${candidateId}/resume`, body)
}

function formatAiScore(score) {
  return score === null || score === undefined ? '暂无' : `${score}%`
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除候选人“${row.name}”及其流程记录？`, '删除确认', { type: 'warning' })
  await api.delete(`/candidates/${row.id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  await loadPositions()
  await load()
})
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.upload-tip {
  display: block;
  margin-top: 6px;
  color: #64748b;
  line-height: 1.5;
}
</style>
