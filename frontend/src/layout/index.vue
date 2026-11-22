<template>
  <el-container class="layout">
    <el-aside :width="isMobile ? '216px' : (collapsed ? '64px' : '216px')" class="aside" :class="{ 'mobile-open': mobileOpen }">
      <div class="logo">
        <div class="logo-mark">星</div>
        <span v-show="isMobile || !collapsed" class="logo-text">星曜数字化平台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activePath"
          :collapse="!isMobile && collapsed"
          :collapse-transition="false"
          router
          background-color="#111c2e"
          text-color="#b9c4d4"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <menu-item :menus="store.menus" />
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <div v-if="isMobile && mobileOpen" class="aside-mask" @click="mobileOpen = false" />

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleAside">
            <Expand v-if="isMobile || collapsed" />
            <Fold v-else />
          </el-icon>
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-tag v-if="pending > 0" type="warning" effect="plain" size="small" class="todo-tag">
            待审核报工 {{ pending }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-box">
              <el-avatar :size="28" class="avatar">{{ (store.realName || 'U').slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ store.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ store.user?.deptName || '未设置部门' }}</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <div class="tabs">
        <el-tag
          v-for="tab in tabs"
          :key="tab.path"
          :closable="tab.path !== '/dashboard'"
          :effect="tab.path === route.path ? 'dark' : 'plain'"
          class="tab"
          @click="router.push(tab.path)"
          @close="closeTab(tab.path)"
        >
          {{ tab.title }}
        </el-tag>
      </div>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import MenuItem from './MenuItem.vue'
import { pageFeedback } from '../api'

const store = useUserStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const isMobile = ref(window.innerWidth <= 768)
const mobileOpen = ref(false)
const tabs = ref([{ path: '/dashboard', title: '工作台' }])
const pending = ref(0)

const toggleAside = () => {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value
  } else {
    collapsed.value = !collapsed.value
  }
}

const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileOpen.value = false
  }
}

const activePath = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '工作台')

watch(
  () => route.path,
  (path) => {
    if (!path || path === '/login') return
    mobileOpen.value = false
    const exists = tabs.value.find((t) => t.path === path)
    if (!exists) {
      tabs.value.push({ path, title: route.meta.title || path })
    }
  },
  { immediate: true }
)

const closeTab = (path) => {
  const index = tabs.value.findIndex((t) => t.path === path)
  tabs.value.splice(index, 1)
  if (route.path === path) {
    const target = tabs.value[index - 1] || tabs.value[0]
    router.push(target.path)
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await store.logout()
    router.push('/login')
  }
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  try {
    const res = await pageFeedback({ pageNum: 1, pageSize: 1, status: 'PENDING' })
    pending.value = res.total || 0
  } catch (e) {
    pending.value = 0
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #111c2e;
  transition: width 0.2s;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.logo-mark {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #22d3ee);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.logo-text {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eef1f6;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  cursor: pointer;
  font-size: 18px;
  color: #4b5563;
}
.header-title {
  font-size: 15px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.todo-tag {
  cursor: default;
}
.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #374151;
}
.avatar {
  background: #2563eb;
  font-size: 13px;
}
.user-name {
  font-size: 13px;
}
.tabs {
  background: #fff;
  padding: 6px 16px 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-bottom: 1px solid #eef1f6;
}
.tab {
  cursor: pointer;
}
.main {
  padding: 16px;
  overflow: auto;
}
.aside-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1999;
}
@media (max-width: 768px) {
  .aside {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 2000;
    transform: translateX(-100%);
    transition: transform 0.25s;
  }
  .aside.mobile-open {
    transform: translateX(0);
  }
  .header {
    height: 52px;
    padding: 0 10px;
  }
  .header-title {
    font-size: 14px;
  }
  .user-name {
    display: none;
  }
  .todo-tag {
    display: none;
  }
  .tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: 6px 10px;
  }
  .main {
    padding: 10px;
  }
}
</style>
