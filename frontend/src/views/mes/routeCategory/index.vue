<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-input v-model="keyword" placeholder="分类名称" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="JB-2：工艺流程分类对齐物料分类（item-type）的实现方式，支持列表筛选与表单选择。"
      style="margin-bottom: 12px"
    />
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="code" label="分类编码" width="160" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column prop="routeCount" label="工艺路线数" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑工艺分类' : '新增工艺分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="如 RC09" />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如 热处理工艺" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
import {
  getRouteCategories,
  createRouteCategory,
  updateRouteCategory,
  deleteRouteCategory
} from '../../../api'

const rows = ref([])
const loading = ref(false)
const keyword = ref('')
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, code: '', name: '', sort: 1, status: 1 })
const rules = {
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await getRouteCategories({ keyword: keyword.value || undefined })
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, code: '', name: '', sort: 1, status: 1 })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateRouteCategory(form.id, form)
  } else {
    await createRouteCategory(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await deleteRouteCategory(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
