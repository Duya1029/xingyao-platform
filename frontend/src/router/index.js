import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', component: () => import('../views/login/index.vue') },
  { path: '/register', component: () => import('../views/register/index.vue') },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/dashboard/index.vue'), meta: { title: '工作台' } },
      { path: 'system/user', component: () => import('../views/system/user/index.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', component: () => import('../views/system/role/index.vue'), meta: { title: '角色管理' } },
      { path: 'system/login-log', component: () => import('../views/system/loginlog/index.vue'), meta: { title: '登录日志' } },
      { path: 'mes/task', component: () => import('../views/mes/task/index.vue'), meta: { title: '排产任务' } },
      { path: 'mes/feedback', component: () => import('../views/mes/feedback/index.vue'), meta: { title: '报工审核' } },
      { path: 'mes/route', component: () => import('../views/mes/route/index.vue'), meta: { title: '工艺路线' } },
      { path: 'mes/route-category', component: () => import('../views/mes/routeCategory/index.vue'), meta: { title: '工艺分类' } },
      { path: 'mes/workstation', component: () => import('../views/mes/workstation/index.vue'), meta: { title: '工作站' } },
      { path: 'wms/item', component: () => import('../views/wms/item/index.vue'), meta: { title: '物料管理' } },
      { path: 'wms/warehouse', component: () => import('../views/wms/warehouse/index.vue'), meta: { title: '仓库管理' } },
      { path: 'wms/inout', component: () => import('../views/wms/inout/index.vue'), meta: { title: '出入库单' } },
      { path: 'erp/product', component: () => import('../views/erp/product/index.vue'), meta: { title: '产品管理' } },
      { path: 'erp/order', component: () => import('../views/erp/order/index.vue'), meta: { title: '订单管理' } },
      { path: 'import/center', component: () => import('../views/import/center/index.vue'), meta: { title: '导入中心' } },
      { path: 'drawing/batch', component: () => import('../views/drawing/batch/index.vue'), meta: { title: '图纸识别' } }
    ]
  },
  { path: '/403', component: () => import('../views/error/403.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('../views/error/404.vue') }
]

const router = createRouter({
  history: import.meta.env.VITE_DEMO === '1' ? createWebHashHistory() : createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.path === '/login' || to.path === '/register') {
    return true
  }
  if (!store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!store.user) {
    try {
      await store.loadProfile()
    } catch (e) {
      await store.logout()
      return { path: '/login' }
    }
  }
  return true
})

export default router
