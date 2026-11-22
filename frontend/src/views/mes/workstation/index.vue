<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-input v-model="keyword" placeholder="工作站名称" clearable style="width: 200px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增工作站</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="code" label="工作站编码" width="150" />
      <el-table-column prop="name" label="工作站名称" min-width="180" />
      <el-table-column prop="workshopName" label="所属车间" width="150" />
      <el-table-column prop="memberCount" label="成员数" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <el-button link type="primary" @click="openMembers(row)">成员</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑工作站' : '新增工作站'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="如 ST05" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="所属车间">
          <el-select v-model="form.workshopId" style="width: 100%" placeholder="选择车间">
            <el-option v-for="w in workshops" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
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

    <el-dialog v-model="memberVisible" title="工作站成员" width="460px">
      <p class="muted">成员决定 JB-5「本订单关联人员」的判定范围（工作站成员可报工）。</p>
      <el-select v-model="memberIds" multiple filterable style="width: 100%" placeholder="选择成员">
        <el-option v-for="u in users" :key="u.id" :label="`${u.realName}（${u.username}）`" :value="u.id" />
      </el-select>
      <template #footer>
        <el-button @click="memberVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMembers">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWorkstations,
  getWorkshops,
  createWorkstation,
  updateWorkstation,
  deleteWorkstation,
  getStationMembers,
  saveStationMembers,
  allUsers
} from '../../../api'

const rows = ref([])
const workshops = ref([])
const users = ref([])
const loading = ref(false)
const keyword = ref('')
const visible = ref(false)
const memberVisible = ref(false)
const memberIds = ref([])
const currentStation = ref(null)
const formRef = ref()
const form = reactive({ id: null, code: '', name: '', workshopId: null, status: 1 })
const rules = {
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await getWorkstations({ keyword: keyword.value || undefined })
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, code: '', name: '', workshopId: null, status: 1 })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  visible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateWorkstation(form.id, form)
  } else {
    await createWorkstation(form)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除工作站「${row.name}」？`, '提示', { type: 'warning' })
  await deleteWorkstation(row.id)
  ElMessage.success('已删除')
  load()
}

const openMembers = async (row) => {
  currentStation.value = row
  const list = await getStationMembers(row.id)
  memberIds.value = list.map((u) => u.id)
  memberVisible.value = true
}

const saveMembers = async () => {
  await saveStationMembers(currentStation.value.id, memberIds.value)
  ElMessage.success('成员已更新')
  memberVisible.value = false
  load()
}

onMounted(async () => {
  workshops.value = await getWorkshops()
  users.value = await allUsers()
  load()
})
</script>

<style scoped>
.muted {
  color: #8a94a6;
  font-size: 12px;
  margin-top: 0;
}
</style>
