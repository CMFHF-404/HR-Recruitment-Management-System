<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">招聘流程</h1>
        <p class="page-subtitle">集中维护简历筛选、面试安排与最终录用结果。</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="简历筛选" name="screening">
          <el-table v-loading="loading" :data="screenings.content" stripe>
            <el-table-column label="候选人" min-width="120"><template #default="{ row }">{{ row.candidate.name }}</template></el-table-column>
            <el-table-column label="应聘岗位" min-width="150"><template #default="{ row }">{{ row.candidate.position?.name }}</template></el-table-column>
            <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ screeningStatusText[row.status] }}</el-tag></template></el-table-column>
            <el-table-column label="主管确认" width="130"><template #default="{ row }"><el-tag :type="statusType[row.managerStatus]">{{ managerReviewStatusText[row.managerStatus] }}</el-tag></template></el-table-column>
            <el-table-column prop="comment" label="筛选意见" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="editScreening(row)">处理</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="面试安排" name="interview">
          <el-table v-loading="loading" :data="interviews.content" stripe>
            <el-table-column label="候选人" min-width="120"><template #default="{ row }">{{ row.candidate.name }}</template></el-table-column>
            <el-table-column label="岗位" min-width="150"><template #default="{ row }">{{ row.candidate.position?.name }}</template></el-table-column>
            <el-table-column prop="interviewTime" label="面试时间" min-width="170" />
            <el-table-column prop="location" label="地点" width="130" />
            <el-table-column prop="interviewer" label="面试官" width="120" />
            <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ interviewStatusText[row.status] }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="editInterview(row)">安排/记录</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="录用结果" name="offer">
          <el-table v-loading="loading" :data="offers.content" stripe>
            <el-table-column label="候选人" min-width="120"><template #default="{ row }">{{ row.candidate.name }}</template></el-table-column>
            <el-table-column label="岗位" min-width="150"><template #default="{ row }">{{ row.candidate.position?.name }}</template></el-table-column>
            <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ offerStatusText[row.status] }}</el-tag></template></el-table-column>
            <el-table-column prop="salaryNote" label="薪资说明" min-width="150" />
            <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="editOffer(row)">登记</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="screeningDialog" title="简历筛选" width="500px">
      <el-form :model="screeningForm" label-width="90px">
        <el-form-item label="筛选状态">
          <el-select v-model="screeningForm.status" style="width: 100%">
            <el-option label="待筛选" value="PENDING" />
            <el-option label="通过" value="PASSED" />
            <el-option label="未通过" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="筛选意见"><el-input v-model="screeningForm.comment" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="screeningDialog = false">取消</el-button><el-button type="primary" @click="saveScreening">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="interviewDialog" title="面试安排" width="540px">
      <el-form :model="interviewForm" label-width="90px">
        <el-form-item label="面试时间"><el-date-picker v-model="interviewForm.interviewTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="面试地点"><el-input v-model="interviewForm.location" /></el-form-item>
        <el-form-item label="面试官"><el-input v-model="interviewForm.interviewer" /></el-form-item>
        <el-form-item label="面试状态">
          <el-select v-model="interviewForm.status" style="width: 100%">
            <el-option label="未安排" value="NOT_SCHEDULED" />
            <el-option label="已安排" value="SCHEDULED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="面试评价"><el-input v-model="interviewForm.evaluation" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="interviewDialog = false">取消</el-button><el-button type="primary" @click="saveInterview">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="offerDialog" title="录用结果" width="500px">
      <el-form :model="offerForm" label-width="90px">
        <el-form-item label="录用状态">
          <el-select v-model="offerForm.status" style="width: 100%">
            <el-option label="待定" value="PENDING" />
            <el-option label="录用" value="OFFERED" />
            <el-option label="未录用" value="REJECTED" />
            <el-option label="放弃入职" value="ABANDONED" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资说明"><el-input v-model="offerForm.salaryNote" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="offerForm.remark" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="offerDialog = false">取消</el-button><el-button type="primary" @click="saveOffer">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { api, interviewStatusText, managerReviewStatusText, offerStatusText, screeningStatusText, statusType } from '../api'

const activeTab = ref('screening')
const loading = ref(false)
const screenings = ref({ content: [] })
const interviews = ref({ content: [] })
const offers = ref({ content: [] })
const selectedCandidateId = ref(null)
const screeningDialog = ref(false)
const interviewDialog = ref(false)
const offerDialog = ref(false)
const screeningForm = reactive({ status: 'PENDING', comment: '' })
const interviewForm = reactive({ interviewTime: '', location: '', interviewer: '', status: 'NOT_SCHEDULED', evaluation: '' })
const offerForm = reactive({ status: 'PENDING', salaryNote: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const [screeningData, interviewData, offerData] = await Promise.all([
      api.get('/screenings', { params: { size: 100 } }),
      api.get('/interviews', { params: { size: 100 } }),
      api.get('/offers', { params: { size: 100 } }),
    ])
    screenings.value = screeningData
    interviews.value = interviewData
    offers.value = offerData
  } finally {
    loading.value = false
  }
}

function editScreening(row) {
  selectedCandidateId.value = row.candidate.id
  Object.assign(screeningForm, { status: row.status, comment: row.comment || '' })
  screeningDialog.value = true
}

async function saveScreening() {
  await api.put(`/screenings/${selectedCandidateId.value}`, screeningForm)
  ElMessage.success('筛选状态已保存')
  screeningDialog.value = false
  load()
}

function editInterview(row) {
  selectedCandidateId.value = row.candidate.id
  Object.assign(interviewForm, {
    interviewTime: row.interviewTime || '',
    location: row.location || '',
    interviewer: row.interviewer || '',
    status: row.status,
    evaluation: row.evaluation || '',
  })
  interviewDialog.value = true
}

async function saveInterview() {
  await api.put(`/interviews/${selectedCandidateId.value}`, interviewForm)
  ElMessage.success('面试信息已保存')
  interviewDialog.value = false
  load()
}

function editOffer(row) {
  selectedCandidateId.value = row.candidate.id
  Object.assign(offerForm, { status: row.status, salaryNote: row.salaryNote || '', remark: row.remark || '' })
  offerDialog.value = true
}

async function saveOffer() {
  await api.put(`/offers/${selectedCandidateId.value}`, offerForm)
  ElMessage.success('录用结果已保存')
  offerDialog.value = false
  load()
}

onMounted(load)
</script>
