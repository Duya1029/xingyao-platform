<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-input v-model="query.keyword" placeholder="路线名称" clearable style="width: 200px" @keyup.enter="load" />
      <el-select v-model="query.categoryId" placeholder="工艺分类" clearable style="width: 180px" @change="load">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增工艺路线</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="code" label="路线编码" width="150" />
      <el-table-column prop="name" label="路线名称" min-width="180" />
      <el-table-column prop="categoryName" label="所属分类" width="150" />
      <el-table-column prop="remark" label="工序说明" min-width="200" show-overflow-tooltip />
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

    <el-dialog v-model="visible" :title="form.id ? '编辑工艺路线' : '新增工艺路线'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="路线编码" prop="code">
          <el-input v-model="form.code" placeholder="如 RT-1005" />
        </el-form-item>
        <el-form-item label="路线名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="工艺分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工序说明">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="如 下料-车削-铣削-检验" />
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
import { getRoutes, createRoute, updateRoute, deleteRoute, getRouteCategories } from '../../../api'

const rows = ref([])
const categories = ref([])
const loading = ref(false)
const visible = ref(false)
const formRef = ref()
const query = reactive({ keyword: '', categoryId: null })
const form = reactive({ id: null, code: '', name: '', categoryId: null, remark: '', status: 1 })
const rules = {
  code: [{ required: true, message: '请输入路线编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入路线名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择工艺分类', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await getRoutes({ keyword: query.keyword || undefined, categoryId: query.categoryId ?? undefined })
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, code: '', name: '', categoryId: null, remark: '', status: 1 })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateRoute(form.id, form)
  } else {
    await createRoute(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除路线「${row.name}」？`, '提示', { type: 'warning' })
  await deleteRoute(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  categories.value = await getRouteCategories()
  load()
})
</script>
