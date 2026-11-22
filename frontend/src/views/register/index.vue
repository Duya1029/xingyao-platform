<template>
  <div class="register-page">
    <div class="register-card">
      <div class="brand-badge">星曜</div>
      <h2>注册新账号</h2>
      <p class="hint">注册后默认拥有仓储、经营数据的只读权限，如需更高权限请联系管理员分配。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="3~20 位字母、数字或下划线" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">注 册</el-button>
      </el-form>
      <div class="foot">
        已有账号？<el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', realName: '', phone: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{3,20}$/, message: '3~20 位字母、数字或下划线', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      password: form.password
    })
    ElMessage.success('注册成功，请使用新账号登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at 15% 15%, #1d4ed8 0%, #0f172a 55%, #07101f 100%);
}
.register-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 14px;
  padding: 30px 28px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.25);
}
.brand-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #60a5fa, #22d3ee);
  color: #04121f;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
h2 {
  margin: 0 0 6px;
  font-size: 22px;
}
.hint {
  color: #98a2b3;
  font-size: 12px;
  line-height: 1.6;
  margin: 0 0 18px;
}
.submit {
  width: 100%;
  margin-top: 4px;
}
.foot {
  margin-top: 16px;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}
</style>
