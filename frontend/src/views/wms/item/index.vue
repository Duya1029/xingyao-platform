<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-input v-model="query.keyword" placeholder="编码 / 名称" clearable style="width: 200px" @keyup.enter="reload" />
      <el-select v-model="query.categoryId" placeholder="物料分类" clearable style="width: 170px" @change="reload">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增物料</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="code" label="物料编码" width="150" />
      <el-table-column prop="name" label="物料名称" min-width="180" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="brandName" label="品牌" width="120" />
      <el-table-column prop="spec" label="规格" min-width="140" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="stockQty" label="库存" width="100" />
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

    <el-dialog v-model="visible" :title="form.id ? '编辑物料' : '新增物料'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="物料编码" prop="code">
          <el-input v-model="form.code" placeholder="如 SKU-1004" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="form.brandId" clearable style="width: 100%">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如 件 / 根 / 个" />
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
  pageItems,
  createItem,
  updateItem,
  deleteItem,
  getItemCategories,
  getOptions
} from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const brands = ref([])
const visible = ref(false)
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null })
const form = reactive({ id: null, code: '', name: '', categoryId: null, brandId: null, spec: '', unit: '件' })
const rules = {
  code: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageItems({ ...query, categoryId: query.categoryId ?? undefined })
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
  Object.assign(form, { id: null, code: '', name: '', categoryId: null, brandId: null, spec: '', unit: '件' })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateItem(form.id, form)
  } else {
    await createItem(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除物料「${row.name}」？`, '提示', { type: 'warning' })
  await deleteItem(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const options = await getOptions()
  categories.value = await getItemCategories()
  brands.value = options.brands || []
  load()
})
</script>
