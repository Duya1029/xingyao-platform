import { defineStore } from 'pinia'
import { login as loginApi, getInfo, getMenus, logout as logoutApi } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('xy_token') || '',
    user: null,
    menus: []
  }),
  getters: {
    realName: (state) => state.user?.realName || '',
    roles: (state) => state.user?.roles || [],
    perms: (state) => state.user?.perms || []
  },
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      localStorage.setItem('xy_token', data.token)
      await this.loadProfile()
    },
    async loadProfile() {
      const [user, menus] = await Promise.all([getInfo(), getMenus()])
      this.user = user
      this.menus = menus
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        /* 忽略登出接口异常 */
      }
      this.token = ''
      this.user = null
      this.menus = []
      localStorage.removeItem('xy_token')
    }
  }
})
