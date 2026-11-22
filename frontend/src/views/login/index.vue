<template>
  <div class="login-page">
    <div class="brand">
      <div class="brand-inner">
        <div class="brand-badge">星曜</div>
        <h1>星曜数字化管理平台</h1>
        <p>WMS · MES · ERP 一体化，一套账号贯通仓储、制造与经营。</p>
        <ul>
          <li>排产任务指派到人或工作站，报工全流程可追溯</li>
          <li>基础数据模板化导入，失败行逐条定位</li>
          <li>图纸批量上传识别，单张入口继续可用</li>
        </ul>
      </div>
    </div>

    <div class="form-side">
      <div class="form-card">
        <h2>账号登录</h2>
        <p class="hint">默认账号 admin / 123456</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item v-if="captchaEnabled" prop="captchaCode">
            <div class="captcha-row">
              <el-input v-model="form.captchaCode" placeholder="验证码" :prefix-icon="Key" maxlength="4" />
              <img v-if="captchaImage" :src="captchaImage" class="captcha-img" alt="验证码" @click="loadCaptcha" />
              <div v-else class="captcha-img loading" @click="loadCaptcha">点击加载</div>
            </div>
          </el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
        <div class="register-tip">
          还没有账号？<el-link type="primary" @click="$router.push('/register')">注册账号</el-link>
        </div>
        <p class="foot">登录即代表同意平台使用规范，账号由管理员统一分配。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAuthConfig, getCaptcha } from '../../api'
import { useUserStore } from '../../store/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const captchaEnabled = ref(true)

const form = reactive({
  username: 'admin',
  password: '123456',
  captchaKey: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const loadCaptcha = async () => {
  try {
    const data = await getCaptcha()
    form.captchaKey = data.key
    captchaImage.value = data.image
    form.captchaCode = ''
  } catch (e) {
    captchaImage.value = ''
  }
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await store.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } catch (e) {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const cfg = await getAuthConfig()
    captchaEnabled.value = cfg.captchaEnabled !== false
  } catch (e) {
    captchaEnabled.value = true
  }
  if (captchaEnabled.value) {
    loadCaptcha()
  }
})
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  background: #f6f8fb;
}
.brand {
  flex: 1.1;
  background: radial-gradient(circle at 20% 20%, #1d4ed8 0%, #0f172a 55%, #07101f 100%);
  color: #e5edff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}
.brand-inner {
  max-width: 460px;
}
.brand-badge {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  background: linear-gradient(135deg, #60a5fa, #22d3ee);
  color: #04121f;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
  margin-bottom: 26px;
}
.brand h1 {
  font-size: 32px;
  margin: 0 0 14px;
  letter-spacing: 1px;
}
.brand p {
  color: #a9bbd6;
  line-height: 1.7;
  margin: 0 0 22px;
}
.brand ul {
  padding-left: 18px;
  color: #8fa3c2;
  line-height: 2;
  font-size: 13px;
}
.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.form-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border-radius: 14px;
  padding: 36px 32px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}
.form-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
}
.hint {
  color: #98a2b3;
  font-size: 12px;
  margin: 0 0 24px;
}
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #e4e8ef;
  object-fit: cover;
}
.captcha-img.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #98a2b3;
}
.submit {
  width: 100%;
  margin-top: 6px;
}
.foot {
  margin-top: 18px;
  font-size: 12px;
  color: #98a2b3;
  line-height: 1.6;
}
.register-tip {
  margin-top: 16px;
  font-size: 13px;
  color: #6b7280;
  text-align: center;
}
@media (max-width: 900px) {
  .brand {
    display: none;
  }
  .form-side {
    padding: 20px;
  }
  .form-card {
    padding: 28px 22px;
  }
}
</style>
