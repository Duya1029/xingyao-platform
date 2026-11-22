<template>
  <div class="dashboard">
    <div class="hero">
      <div>
        <h2>{{ greeting }}，{{ store.realName }}</h2>
        <p>今天可以优先处理待审核报工与在制任务，数据来自 MES 实时汇总。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/mes/task')">查看排产任务</el-button>
        <el-button @click="router.push('/import/center')">批量导入基础数据</el-button>
      </div>
    </div>

    <div class="stat-grid">
      <div v-for="item in stats" :key="item.label" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-desc">{{ item.desc }}</div>
      </div>
    </div>

    <div class="page-card">
      <div class="page-toolbar">
        <div>
          <h3 class="page-title">最近排产任务</h3>
          <div class="page-sub">显示最近 5 条任务，可进入排产任务页处理指派与报工</div>
        </div>
        <div class="spacer" />
        <el-button link type="primary" @click="router.push('/mes/task')">全部任务</el-button>
      </div>
      <el-table :data="tasks" size="default" stripe>
        <el-table-column prop="taskNo" label="任务号" width="150" />
        <el-table-column prop="productName" label="产品" min-width="140" />
        <el-table-column prop="assignType" label="指派方式" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.assignType === 'PERSON' ? 'success' : 'info'">
              {{ row.assignType === 'PERSON' ? '个人' : '工作站' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="指派人/站" width="140" />
        <el-table-column prop="planQty" label="排产" width="90" />
        <el-table-column prop="finishedQty" label="已生产" width="90" />
        <el-table-column label="进度" min-width="140">
          <template #default="{ row }">
            <el-progress :percentage="percent(row)" :stroke-width="8" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview } from '../../api'
import { useUserStore } from '../../store/user'

const router = useRouter()
const store = useUserStore()
const data = ref({})
const tasks = ref([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const stats = computed(() => [
  { label: '排产任务', value: data.value.taskTotal ?? 0, desc: '全部任务数' },
  { label: '进行中', value: data.value.taskRunning ?? 0, desc: '已开工未完工' },
  { label: '待审核报工', value: data.value.feedbackPending ?? 0, desc: '需要车间主管处理' },
  { label: '物料档案', value: data.value.itemTotal ?? 0, desc: 'WMS 在册物料' }
])

const percent = (row) => {
  const plan = Number(row.planQty || 0)
  const finished = Number(row.finishedQty || 0)
  if (!plan) return 0
  return Math.min(100, Math.round((finished / plan) * 100))
}

onMounted(async () => {
  const res = await getOverview()
  data.value = res
  tasks.value = res.recentTasks || []
})
</script>

<style scoped>
.hero {
  background: linear-gradient(120deg, #1e3a8a, #2563eb 60%, #0ea5e9);
  color: #fff;
  border-radius: 12px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}
.hero h2 {
  margin: 0 0 6px;
  font-size: 20px;
}
.hero p {
  margin: 0;
  color: #d6e4ff;
  font-size: 13px;
}
.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px;
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.06);
}
.stat-label {
  color: #6b7280;
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin: 6px 0 2px;
  color: #111827;
}
.stat-desc {
  color: #98a2b3;
  font-size: 12px;
}
</style>
