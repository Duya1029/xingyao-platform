<template>
  <div class="page-card">
    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 14px"
      title="支持 PDF / PNG / JPG / DWG / DXF，单次最多 50 份。上传后进入识别队列，可在下方查看识别结果并重试失败文件。" />

    <el-upload
      drag
      multiple
      :auto-upload="false"
      :on-change="onFileChange"
      :on-remove="onFileRemove"
      :file-list="fileList"
      accept=".pdf,.png,.jpg,.jpeg,.dwg,.dxf"
      style="margin-bottom: 14px"
    >
      <div style="padding: 24px 0">
        <div style="font-size: 40px; line-height: 1; color: #c0c4cc">⇪</div>
        <div style="margin-top: 10px">将图纸拖到此处，或<em>点击选择</em></div>
        <div style="color: #909399; font-size: 12px; margin-top: 6px">单次最多 50 份</div>
      </div>
    </el-upload>

    <el-button type="primary" :disabled="!fileList.length" :loading="uploading" @click="submit">
      上传并识别（{{ fileList.length }} 份）
    </el-button>

    <div class="page-toolbar" style="margin-top: 20px; margin-bottom: 10px">
      <span class="page-title">识别任务</span>
      <div class="spacer" />
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="taskNo" label="任务号" width="180" />
      <el-table-column prop="fileCount" label="文件数" width="90" />
      <el-table-column prop="successCount" label="成功" width="80" />
      <el-table-column prop="failCount" label="失败" width="80" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createUser" label="提交人" width="120" />
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openFiles(row)">查看结果</el-button>
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

    <el-dialog v-model="fileVisible" :title="`识别结果 - ${currentTask?.taskNo || ''}`" width="900px">
      <el-table :data="files" stripe max-height="440">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="fileStatusType(row.status)" size="small">{{ fileStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recognizeResult" label="识别结果" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 'FAILED'" @click="retry(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="fileVisible = false">关闭</el-button>
        <el-button type="primary" @click="refreshFiles">刷新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageDrawingTasks, uploadDrawings, getDrawingFiles, retryDrawing } from '../../../api'

const fileList = ref([])
const uploading = ref(false)
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const fileVisible = ref(false)
const files = ref([])
const currentTask = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10 })

const statusText = (s) => ({ RUNNING: '识别中', DONE: '已完成', FAILED: '失败' }[s] || s)
const statusType = (s) => ({ RUNNING: 'warning', DONE: 'success', FAILED: 'danger' }[s] || 'info')
const fileStatusText = (s) => ({ PENDING: '排队中', SUCCESS: '成功', FAILED: '失败' }[s] || s)
const fileStatusType = (s) => ({ PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger' }[s] || 'info')
const formatSize = (size) => (!size ? '-' : size > 1024 * 1024 ? `${(size / 1024 / 1024).toFixed(1)} MB` : `${(size / 1024).toFixed(0)} KB`)

const onFileChange = (uploadFile, uploadFiles) => {
  fileList.value = uploadFiles
}

const onFileRemove = (uploadFile, uploadFiles) => {
  fileList.value = uploadFiles
}

const submit = async () => {
  const formData = new FormData()
  fileList.value.forEach((f) => formData.append('files', f.raw))
  uploading.value = true
  try {
    await uploadDrawings(formData)
    ElMessage.success('已提交识别任务')
    fileList.value = []
    query.pageNum = 1
    load()
  } finally {
    uploading.value = false
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageDrawingTasks(query)
    rows.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

const openFiles = async (row) => {
  currentTask.value = row
  fileVisible.value = true
  await refreshFiles()
}

const refreshFiles = async () => {
  if (!currentTask.value) return
  files.value = await getDrawingFiles(currentTask.value.id)
}

const retry = async (row) => {
  await retryDrawing(row.id)
  ElMessage.success('已重新提交识别')
  await refreshFiles()
  load()
}

onMounted(load)
</script>
