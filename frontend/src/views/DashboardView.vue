<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计首页</h1>
        <p class="page-subtitle">快速查看岗位、候选人和录用进展，支持答辩时展示招聘数据统计。</p>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>

    <div class="stat-grid">
      <div v-for="item in cards" :key="item.label" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </div>
    </div>

    <div class="panel">
      <div class="page-header compact">
        <div>
          <h2>各岗位招聘漏斗</h2>
          <p>候选人数、面试人数与录用人数对比</p>
        </div>
      </div>
      <div v-if="!positionStats.length" class="empty-hint">暂无岗位维度数据，请先维护岗位与候选人流程。</div>
      <div v-else ref="chartRef" class="chart"></div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { api } from '../api'

const overview = ref({})
const positionStats = ref([])
const chartRef = ref()
let chart
let mounted = false
const resizeChart = () => chart?.resize()

const cards = computed(() => [
  { label: '岗位数量', value: overview.value.positionCount ?? 0 },
  { label: '候选人数', value: overview.value.candidateCount ?? 0 },
  { label: '待筛选', value: overview.value.pendingScreeningCount ?? 0 },
  { label: '面试人数', value: overview.value.interviewCount ?? 0 },
  { label: '录用人数', value: overview.value.offeredCount ?? 0 },
])

async function load() {
  overview.value = await api.get('/statistics/overview')
  positionStats.value = await api.get('/statistics/positions')
  await nextTick()
  if (!mounted) return
  if (!positionStats.value.length) {
    chart?.dispose()
    chart = null
    return
  }
  if (!chartRef.value) return
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  const names = positionStats.value.map((item) => item.positionName)
  if (!names.length) {
    chart?.dispose()
    chart = null
    return
  }
  chart ||= echarts.init(chartRef.value)
  const rotate = names.length > 4 ? 28 : 0
  const bottom = rotate ? 56 : 36
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 40, right: 20, bottom, top: 48 },
    xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '候选人数', type: 'bar', data: positionStats.value.map((item) => item.candidateCount), itemStyle: { color: '#0f766e' } },
      { name: '面试人数', type: 'bar', data: positionStats.value.map((item) => item.interviewCount), itemStyle: { color: '#2563eb' } },
      { name: '录用人数', type: 'bar', data: positionStats.value.map((item) => item.offeredCount), itemStyle: { color: '#f59e0b' } },
    ],
  })
}

onMounted(() => {
  mounted = true
  load()
  window.addEventListener('resize', resizeChart)
})
onUnmounted(() => {
  mounted = false
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.compact h2 {
  margin: 0;
  font-size: 18px;
}

.compact p {
  margin: 4px 0 0;
  color: #64748b;
}
</style>
