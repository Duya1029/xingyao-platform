<template>
  <div class="page-card">
    <div class="page-toolbar">
      <span class="page-title">角色管理</span>
      <div class="spacer" />
      <el-tag type="info">角色由数据库初始化，如需新增请在 sys_role 中维护</el-tag>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="roleCode" label="角色编码" width="180" />
      <el-table-column prop="roleName" label="角色名称" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="240" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getRoles } from '../../../api'

const rows = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    rows.value = await getRoles()
  } finally {
    loading.value = false
  }
})
</script>
