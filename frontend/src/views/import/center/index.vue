<template>
  <div class="page-card">
    <div class="page-toolbar">
      <el-select v-model="bizType" placeholder="选择导入类型" style="width: 220px">
        <el-option v-for="t in types" :key="t.bizType" :label="t.label" :value="t.bizType" />
      </el-select>
      <el-button :disabled="!bizType" @click="downloadTemplate">下载模板</el-button>
      <el-divider direction="vertical" />
      <el-upload
        :auto-upload="false"
        :limit="1"
        :on-change="onFileChange"
        :on-remove="() => (file = null)"
        accept=".xlsx,.xls"
        :show-file-list="true"
        style="display: inline-block"
      >
        <el-button>选择 Excel 文件</el-button>
      </el-upload>
      <el-button type="primary" :disabled="!bizType || !file" :loading="uploading" @click="doImport">
        开始导入
      </el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px"
      title="导入采用行级校验：通过的行直接入库，失败的行记录到失败明细，可在批次列表查看。" />

    <div class="page-toolbar" style="margin-bottom: 10px">
      <span class="page-title">导入批次</span>
      <div class="spacer" />
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="batchNo" label="批次号" width="180" />
      <el-table-column prop="bizType" label="类型" width="170" />
      <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="totalRows" label="总行数" width="90" />
      <el-table-column prop="successRows" label="成功" width="80" />
      <el-table-column prop="failRows" label="失败" width="80" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createUser" label="操作人" width="120" />
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="!row.failRows" @click="openErrors(row)">失败明细</el-button>
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

    <el-dialog v-model="errorVisible" :title="`失败明细 - ${currentBatch?.batchNo || ''}`" width="860px">
      <el-table :data="errors" stripe max-height="420">
        <el-table-column prop="rowNo" label="行号" width="80" />
        <el-table-column prop="columnName" label="列" width="120" />
        <el-table-column prop="message" label="错误信息" min-width="240" />
        <el-table-column prop="rawData" label="原始数据" min-width="260" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getImportTypes, uploadImport, pageImportBatches, getImportErrors } from '../../../api'
import { DEMO, downloadDemoTemplate } from '../../../mock/adapter'

const types = ref([])
const bizType = ref('')
const file = ref(null)
const uploading = ref(false)
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const errorVisible = ref(false)
const errors = ref([])
const currentBatch = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10 })

const statusText = (s) => ({ DONE: '全部成功', PARTIAL: '部分成功', FAILED: '全部失败', RUNNING: '处理中' }[s] || s)
const statusType = (s) => ({ DONE: 'success', PARTIAL: 'warning', FAILED: 'danger', RUNNING: 'info' }[s] || 'info')

const onFileChange = (uploadFile) => {
  file.value = uploadFile.raw
}

const downloadTemplate = async () => {
  if (DEMO) {
    downloadDemoTemplate(bizType.value)
    return
  }
  const resp = await fetch(`/api/import/template/${bizType.value}`, {
    headers: { Authorization: `Bearer ${localStorage.getItem('xy_token') || ''}` }
  })
  if (!resp.ok) {
    ElMessage.error('模板下载失败')
    return
  }
  const blob = await resp.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${bizType.value}_模板.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}

const doImport = async () => {
  const formData = new FormData()
  formData.append('bizType', bizType.value)
  formData.append('file', file.value)
  uploading.value = true
  try {
    const res = await uploadImport(formData)
    ElMessage.success(`导入完成：共 ${res.total} 行，成功 ${res.success} 行，失败 ${res.fail} 行`)
    query.pageNum = 1
    load()
  } finally {
    uploading.value = false
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageImportBatches(query)
    rows.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

const openErrors = async (row) => {
  currentBatch.value = row
  errors.value = await getImportErrors(row.id)
  errorVisible.value = true
}

onMounted(async () => {
  types.value = await getImportTypes()
  bizType.value = types.value[0]?.bizType || ''
  load()
})
</script>
