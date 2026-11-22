<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-select v-model="query.type" placeholder="订单类型" clearable style="width: 160px" @change="reload">
        <el-option label="销售订单" value="SALES" />
        <el-option label="采购订单" value="PURCHASE" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增订单</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 'SALES' ? 'success' : 'info'">
            {{ row.type === 'SALES' ? '销售' : '采购' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="partnerName" label="客户 / 供应商" min-width="200" />
      <el-table-column prop="amount" label="金额" width="130" />
      <el-table-column prop="orderDate" label="订单日期" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已确认' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
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
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑订单' : '新增订单'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button label="SALES">销售</el-radio-button>
            <el-radio-button label="PURCHASE">采购</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="form.orderNo" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="客户/供应商" prop="partnerName">
          <el-input v-model="form.partnerName" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订单日期">
          <el-date-picker v-model="form.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已确认</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageErpOrders, createErpOrder, updateErpOrder, deleteErpOrder } from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, type: 'SALES' })
const form = reactive({ id: null, orderNo: '', type: 'SALES', partnerName: '', amount: 0, orderDate: '', status: 0 })
const rules = {
  type: [{ required: true, message: '请选择订单类型', trigger: 'change' }],
  partnerName: [{ required: true, message: '请输入客户或供应商', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageErpOrders({ ...query, type: query.type || undefined })
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
  Object.assign(form, { id: null, orderNo: '', type: 'SALES', partnerName: '', amount: 0, orderDate: '', status: 0 })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateErpOrder(form.id, form)
  } else {
    await createErpOrder(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除订单 ${row.orderNo}？`, '提示', { type: 'warning' })
  await deleteErpOrder(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
