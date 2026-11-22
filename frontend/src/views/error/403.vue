<template>
  <div class="error-page">
    <div class="code">403</div>
    <div class="desc">抱歉，您没有访问该页面的权限</div>
    <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
  </div>
</template>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: #f5f7fa;
}
.code {
  font-size: 96px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
}
.desc {
  color: #606266;
}
</style>
