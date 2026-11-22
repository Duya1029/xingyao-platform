<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-input v-model="query.keyword" placeholder="任务号" clearable style="width: 180px" @keyup.enter="load" />
      <el-select v-model="query.assignType" placeholder="指派方式" clearable style="width: 140px" @change="load">
        <el-option label="个人" value="PERSON" />
        <el-option label="工作站" value="STATION" />
      </el-select>
      <el-select v-model="query.status" placeholder="任务状态" clearable style="width: 140px" @change="load">
        <el-option label="待开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已完成" :value="2" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新建排产任务</el-button>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="JB-1：指派方式为「个人」或「工作站」二选一；JB-4：累计报工达到排产数量后系统将拦截继续报工。"
      style="margin-bottom: 12px"
    />

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="taskNo" label="任务号" width="150" />
      <el-table-column prop="productName" label="产品" min-width="130" />
      <el-table-column prop="routeName" label="工艺路线" min-width="140" show-overflow-tooltip />
      <el-table-column label="指派方式" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.assignType === 'PERSON' ? 'success' : 'info'">
            {{ row.assignType === 'PERSON' ? '个人' : '工作站' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assigneeName" label="指派人/站" width="140" />
      <el-table-column label="排产 / 已生产" width="130">
        <template #default="{ row }">{{ row.planQty }} / {{ row.finishedQty }}</template>
      </el-table-column>
      <el-table-column label="已报(含待审)" width="110">
        <template #default="{ row }">{{ row.reportedQty }}</template>
      </el-table-column>
      <el-table-column label="剩余可报" width="100">
        <template #default="{ row }">
          <span :class="{ danger: Number(row.remainQty) <= 0 }">{{ row.remainQty }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 2 || Number(row.remainQty) <= 0" @click="openFeedback(row)">
            报工
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next, sizes"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑排产任务' : '新建排产任务'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="任务号" prop="taskNo">
          <el-input v-model="form.taskNo" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="生产工单" prop="workOrderId">
          <el-select v-model="form.workOrderId" style="width: 100%" placeholder="选择工单">
            <el-option
              v-for="o in options.workOrders"
              :key="o.id"
              :label="`${o.orderNo} · ${o.productName}`"
              :value="o.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工艺路线" prop="routeId">
          <el-select v-model="form.routeId" style="width: 100%" placeholder="选择工艺路线">
            <el-option v-for="r in options.routes" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="指派方式" prop="assignType">
          <el-radio-group v-model="form.assignType" @change="form.assigneeId = null">
            <el-radio-button label="PERSON">个人</el-radio-button>
            <el-radio-button label="STATION">工作站</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.assignType === 'PERSON' ? '指派人员' : '指派工作站'" prop="assigneeId">
          <el-select v-model="form.assigneeId" style="width: 100%" :placeholder="form.assignType === 'PERSON' ? '选择人员' : '选择工作站'">
            <template v-if="form.assignType === 'PERSON'">
              <el-option v-for="u in options.users" :key="u.id" :label="`${u.realName}（${u.username}）`" :value="u.id" />
            </template>
            <template v-else>
              <el-option v-for="w in options.workstations" :key="w.id" :label="w.name" :value="w.id" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="排产数量" prop="planQty">
          <el-input-number v-model="form.planQty" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="form.range"
            type="daterange"
            value-format="YYYY-MM-DDT00:00:00"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="fb.visible" title="报工" width="460px">
      <el-descriptions :column="1" size="small" border style="margin-bottom: 14px">
        <el-descriptions-item label="任务号">{{ fb.row.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ fb.row.productName }}</el-descriptions-item>
        <el-descriptions-item label="排产 / 已生产">{{ fb.row.planQty }} / {{ fb.row.finishedQty }}</el-descriptions-item>
        <el-descriptions-item label="剩余可报">{{ fb.row.remainQty }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="报工数量">
          <el-input-number v-model="fb.qty" :min="1" :max="Number(fb.row.remainQty) || 1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="fb.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fb.visible = false">取消</el-button>
        <el-button type="primary" :loading="fb.loading" @click="submitFeedback">提交报工</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOptions,
  pageTasks,
  createTask,
  updateTask,
  deleteTask,
  createFeedback
} from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const options = ref({ workOrders: [], routes: [], users: [], workstations: [] })
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', assignType: '', status: null })
const dialog = reactive({ visible: false, id: null })
const form = reactive({
  taskNo: '',
  workOrderId: null,
  routeId: null,
  assignType: 'STATION',
  assigneeId: null,
  planQty: 100,
  range: []
})
const rules = {
  workOrderId: [{ required: true, message: '请选择生产工单', trigger: 'change' }],
  routeId: [{ required: true, message: '请选择工艺路线', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择指派对象', trigger: 'change' }],
  planQty: [{ required: true, message: '请输入排产数量', trigger: 'blur' }]
}
const fb = reactive({ visible: false, row: {}, qty: 1, remark: '', loading: false })

const statusText = (s) => ({ 0: '待开始', 1: '进行中', 2: '已完成' }[s] || '-')
const statusType = (s) => ({ 0: 'info', 1: 'warning', 2: 'success' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const res = await pageTasks({ ...query, status: query.status ?? undefined })
    rows.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialog.id = null
  dialog.visible = true
  Object.assign(form, {
    taskNo: '',
    workOrderId: null,
    routeId: null,
    assignType: 'STATION',
    assigneeId: null,
    planQty: 100,
    range: []
  })
}

const openEdit = (row) => {
  dialog.id = row.id
  dialog.visible = true
  Object.assign(form, {
    taskNo: row.taskNo,
    workOrderId: row.workOrderId,
    routeId: row.routeId,
    assignType: row.assignType,
    assigneeId: row.assigneeId,
    planQty: Number(row.planQty),
    range: row.planStart && row.planEnd ? [row.planStart, row.planEnd] : []
  })
}

const submit = async () => {
  await formRef.value.validate()
  const payload = {
    taskNo: form.taskNo || null,
    workOrderId: form.workOrderId,
    routeId: form.routeId,
    assignType: form.assignType,
    assigneeId: form.assigneeId,
    workstationId: form.assignType === 'STATION' ? form.assigneeId : null,
    planQty: form.planQty,
    planStart: form.range?.[0] || null,
    planEnd: form.range?.[1] || null
  }
  if (dialog.id) {
    await updateTask(dialog.id, payload)
  } else {
    await createTask(payload)
  }
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除任务 ${row.taskNo}？`, '提示', { type: 'warning' })
  await deleteTask(row.id)
  ElMessage.success('已删除')
  load()
}

const openFeedback = (row) => {
  fb.row = row
  fb.qty = Math.min(Number(row.remainQty) || 1, 1)
  fb.remark = ''
  fb.visible = true
}

const submitFeedback = async () => {
  fb.loading = true
  try {
    await createFeedback({ taskId: fb.row.id, qty: fb.qty, remark: fb.remark })
    ElMessage.success('报工已提交，等待审核')
    fb.visible = false
    load()
  } finally {
    fb.loading = false
  }
}

onMounted(async () => {
  options.value = await getOptions()
  load()
})
</script>

<style scoped>
.danger {
  color: #dc2626;
  font-weight: 600;
}
</style>
