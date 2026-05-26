<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">主管确认</h1>
        <p class="page-subtitle">对 HR 初筛通过的候选人进行二次确认，通过后候选人才可进入面试安排。</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="panel">
      <div class="table-scroll">
        <el-table v-loading="loading" :data="reviews.content" stripe>
          <template #empty>
            <el-empty description="暂无待确认记录" />
          </template>
          <el-table-column label="候选人" min-width="120"><template #default="{ row }">{{ row.candidate.name }}</template></el-table-column>
          <el-table-column label="应聘岗位" min-width="160"><template #default="{ row }">{{ row.candidate.position?.name }}</template></el-table-column>
          <el-table-column label="部门" min-width="120"><template #default="{ row }">{{ row.candidate.position?.department }}</template></el-table-column>
          <el-table-column label="HR 初筛" width="110"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ screeningStatusText[row.status] }}</el-tag></template></el-table-column>
          <el-table-column label="主管确认" width="130"><template #default="{ row }"><el-tag :type="statusType[row.managerStatus]">{{ managerReviewStatusText[row.managerStatus] }}</el-tag></template></el-table-column>
          <el-table-column label="AI 匹配" width="110"><template #default="{ row }">{{ formatAiScore(row.aiMatchScore) }}</template></el-table-column>
          <el-table-column prop="aiQuickReview" label="AI 快评" min-width="220" show-overflow-tooltip />
          <el-table-column prop="comment" label="HR 意见" min-width="180" show-overflow-tooltip />
          <el-table-column prop="managerComment" label="主管意见" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="editReview(row)">确认</el-button></template></el-table-column>
        </el-table>
      </div>
      <el-pagination
        v-if="reviews.totalElements > 0"
        class="pager"
        layout="total, prev, pager, next"
        :total="reviews.totalElements"
        :page-size="query.size"
        :current-page="query.page + 1"
        @current-change="changePage"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="主管确认" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="AI 匹配度">{{ formatAiScore(form.aiMatchScore) }}</el-form-item>
        <el-form-item label="AI 快评"><el-input v-model="form.aiQuickReview" type="textarea" :rows="3" readonly /></el-form-item>
        <el-form-item label="确认结果" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="主管通过" value="APPROVED" />
            <el-option label="主管驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="确认意见"><el-input v-model="form.comment" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReview">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { api, managerReviewStatusText, screeningStatusText, statusType } from '../api'

const loading = ref(false)
const reviews = ref({ content: [], totalElements: 0 })
const query = reactive({ page: 0, size: 10 })
const dialogVisible = ref(false)
const selectedCandidateId = ref(null)
const formRef = ref()
const form = reactive({ status: 'APPROVED', comment: '', aiMatchScore: null, aiQuickReview: '' })
const rules = {
  status: [{ required: true, message: '请选择确认结果', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    reviews.value = await api.get('/manager-reviews', { params: { page: query.page, size: query.size } })
  } finally {
    loading.value = false
  }
}

function changePage(pageNo) {
  query.page = pageNo - 1
  load()
}

function editReview(row) {
  selectedCandidateId.value = row.candidate.id
  Object.assign(form, {
    status: row.managerStatus === 'REJECTED' ? 'REJECTED' : 'APPROVED',
    comment: row.managerComment || '',
    aiMatchScore: row.aiMatchScore ?? null,
    aiQuickReview: row.aiQuickReview || '',
  })
  dialogVisible.value = true
}

async function saveReview() {
  await formRef.value?.validate()
  await api.put(`/manager-reviews/${selectedCandidateId.value}`, {
    status: form.status,
    comment: form.comment,
  })
  ElMessage.success('主管确认状态已保存')
  dialogVisible.value = false
  load()
}

function formatAiScore(score) {
  return score === null || score === undefined ? '暂无' : `${score}%`
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
