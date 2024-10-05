<script setup>
import { Box, User, View } from '@element-plus/icons-vue'
import { ElIcon, ElMenu, ElMenuItem, ElSubMenu } from 'element-plus'

const handleOpen = (key, keyPath) => {
}
const handleClose = (key, keyPath) => {
}
</script>

<template>
  <el-menu class="layout-menu" :collapse="false" @open="handleOpen" @close="handleClose">
    <el-sub-menu index="1" popper-class="dark-mode">
      <template #title>
        <el-icon><User /></el-icon>
        <span>账号管理</span>
      </template>
      <el-menu-item index="1-1">
        <RouterLink :to="{ name: 'UserList' }">用户</RouterLink>
      </el-menu-item>
      <el-menu-item index="1-2">
        <RouterLink :to="{ name: 'TenantList' }">租户</RouterLink>
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="2" popper-class="dark-mode">
      <template #title>
        <el-icon><View /></el-icon>
        <span>授权管理</span>
      </template>
      <el-menu-item>角色</el-menu-item>
      <el-menu-item>
        <RouterLink :to="{ name: 'PermissionList' }">权限</RouterLink>
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="3" popper-class="dark-mode">
      <template #title>
        <el-icon><Box /></el-icon>
        <span>应用管理</span>
      </template>
      <el-menu-item index="3-1">
        <RouterLink :to="{ name: 'AppList' }">应用</RouterLink>
      </el-menu-item>
      <el-menu-item index="3-2">
        <RouterLink :to="{ name: 'ActiveList' }">活跃</RouterLink>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<style scoped>
.layout-menu {
  border-right: none;
}
</style>