<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3 class="page-title">仓库管理</h3>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增仓库</el-button>
    </div>
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="code" label="仓库编码" width="150" />
      <el-table-column prop="name" label="仓库名称" min-width="180" />
      <el-table-column prop="address" label="地址" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑仓库' : '新增仓库'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="仓库编码" prop="code">
          <el-input v-model="form.code" placeholder="如 WH03" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getWarehouses, createWarehouse, updateWarehouse, deleteWarehouse } from '../../../api'

const rows = ref([])
const loading = ref(false)
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, code: '', name: '', address: '', status: 1 })
const rules = {
  code: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await getWarehouses()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, code: '', name: '', address: '', status: 1 })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateWarehouse(form.id, form)
  } else {
    await createWarehouse(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除仓库「${row.name}」？`, '提示', { type: 'warning' })
  await deleteWarehouse(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
