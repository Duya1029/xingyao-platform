<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu v-if="menu.type === 'DIR' || (menu.children && menu.children.length)" :index="String(menu.id)">
      <template #title>
        <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <menu-item :menus="menu.children || []" />
    </el-sub-menu>
    <el-menu-item v-else :index="menu.path">
      <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
      <template #title>{{ menu.name }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
defineOptions({ name: 'MenuItem' })
defineProps({ menus: { type: Array, default: () => [] } })
</script>
