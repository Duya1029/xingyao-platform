<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-radio-group v-model="query.status" @change="reload">
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">已通过</el-radio-button>
        <el-radio-button label="REJECTED">已驳回</el-radio-button>
        <el-radio-button label="">全部</el-radio-button>
      </el-radio-group>
      <el-input v-model="query.keyword" placeholder="报工单号" clearable style="width: 180px" @keyup.enter="reload" />
      <el-button type="primary" @click="reload">查询</el-button>
      <div class="spacer" />
      <span class="tip">JB-3：审核通过后自动回写排产已生产数量；JB-4：累计不得超过排产数量。</span>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="feedbackNo" label="报工单号" width="170" />
      <el-table-column prop="taskNo" label="任务号" width="150" />
      <el-table-column prop="userName" label="报工人" width="100" />
      <el-table-column prop="qty" label="报工数量" width="100" />
      <el-table-column label="排产 / 已生产" width="140">
        <template #default="{ row }">{{ row.planQty }} / {{ row.finishedQty }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="报工时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="审核意见" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="success" @click="audit(row, true)">通过</el-button>
            <el-button link type="danger" @click="audit(row, false)">驳回</el-button>
          </template>
          <span v-else class="muted">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageFeedback, approveFeedback, rejectFeedback } from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, status: 'PENDING', keyword: '' })

const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s)
const tagType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await pageFeedback({ ...query, status: query.status || undefined })
    rows.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

const reload = () => {
  query.pageNum = 1
  load()
}

const audit = async (row, pass) => {
  const { value } = await ElMessageBox.prompt(
    pass ? '确认通过该报工？通过后将计入已生产数量。' : '请填写驳回原因',
    pass ? '审核通过' : '驳回报工',
    {
      inputPlaceholder: pass ? '审核意见（选填）' : '驳回原因',
      inputValidator: (v) => (pass || (v && v.trim()) ? true : '驳回原因不能为空'),
      type: pass ? 'success' : 'warning'
    }
  )
  if (pass) {
    await approveFeedback(row.id, value || '')
    ElMessage.success('已通过并回写排产数量')
  } else {
    await rejectFeedback(row.id, value)
    ElMessage.success('已驳回')
  }
  load()
}

onMounted(load)
</script>

<style scoped>
.tip {
  color: #8a94a6;
  font-size: 12px;
}
.muted {
  color: #9ca3af;
  font-size: 12px;
}
</style>
