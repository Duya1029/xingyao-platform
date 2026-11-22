<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-select v-model="query.type" placeholder="单据类型" clearable style="width: 150px" @change="reload">
        <el-option label="入库" value="IN" />
        <el-option label="出库" value="OUT" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新建单据</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="单据号" width="180" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 'IN' ? 'success' : 'warning'">
            {{ row.type === 'IN' ? '入库' : '出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warehouseName" label="仓库" width="150" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已过账' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="primary" @click="post(row)">过账</el-button>
          <span v-else class="muted">已完成</span>
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

    <el-dialog v-model="visible" title="新建出入库单" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="单据类型">
          <el-radio-group v-model="form.type">
            <el-radio-button label="IN">入库</el-radio-button>
            <el-radio-button label="OUT">出库</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="form.warehouseId" style="width: 100%" placeholder="选择仓库">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
        <el-form-item label="明细">
          <div class="lines">
            <div v-for="(line, index) in form.lines" :key="index" class="line">
              <el-select v-model="line.itemId" placeholder="选择物料" filterable style="flex: 1">
                <el-option v-for="i in items" :key="i.id" :label="`${i.code} ${i.name}`" :value="i.id" />
              </el-select>
              <el-input-number v-model="line.qty" :min="1" style="width: 140px" />
              <el-button link type="danger" @click="form.lines.splice(index, 1)">删除</el-button>
            </div>
            <el-button link type="primary" @click="form.lines.push({ itemId: null, qty: 1 })">+ 添加明细行</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageInouts, createInout, postInout, getWarehouses, pageItems } from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const warehouses = ref([])
const items = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, type: 'IN' })
const form = reactive({ type: 'IN', warehouseId: null, remark: '', lines: [{ itemId: null, qty: 1 }] })

const load = async () => {
  loading.value = true
  try {
    const res = await pageInouts({ ...query, type: query.type || undefined })
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

const openCreate = () => {
  form.type = 'IN'
  form.warehouseId = null
  form.remark = ''
  form.lines = [{ itemId: null, qty: 1 }]
  visible.value = true
}

const submit = async () => {
  if (!form.warehouseId) {
    ElMessage.warning('请选择仓库')
    return
  }
  const lines = form.lines.filter((l) => l.itemId)
  if (!lines.length) {
    ElMessage.warning('请至少添加一条明细')
    return
  }
  await createInout({ ...form, lines })
  ElMessage.success('单据已保存为草稿')
  visible.value = false
  load()
}

const post = async (row) => {
  await ElMessageBox.confirm(`确认过账单据 ${row.orderNo}？过账后将按类型增减库存。`, '提示', { type: 'warning' })
  await postInout(row.id)
  ElMessage.success('过账成功')
  load()
}

onMounted(async () => {
  warehouses.value = await getWarehouses()
  const res = await pageItems({ pageNum: 1, pageSize: 100 })
  items.value = res.records
  load()
})
</script>

<style scoped>
.lines {
  width: 100%;
}
.line {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
}
.muted {
  color: #9ca3af;
  font-size: 12px;
}
</style>
