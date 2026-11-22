<template>
  <div class="page-card">
    <div class="page-toolbar">
      <span class="page-title">登录日志</span>
      <div class="spacer" />
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" width="150" />
      <el-table-column prop="ip" label="IP" width="160" />
      <el-table-column label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
            {{ row.success === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="说明" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="180" />
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
import { pageLoginLogs } from '../../../api'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const res = await pageLoginLogs(query)
    rows.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
