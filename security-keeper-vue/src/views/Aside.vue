<script setup>
import { Box, User, View } from '@element-plus/icons-vue'
import { ElIcon, ElMenu, ElMenuItem, ElSubMenu } from 'element-plus'

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  console.log(key, keyPath)
}
</script>

<template>
  <el-menu class="layout-menu" :collapse="false" @open="handleOpen" @close="handleClose">
    <el-sub-menu index="1" popper-class="dark-mode">
      <template #title>
        <el-icon><User /></el-icon>
        <span>登录管理</span>
      </template>
      <el-menu-item index="1-1" >用户管理</el-menu-item>
      <el-menu-item>租户管理</el-menu-item>
      <el-menu-item>供应商管理</el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="2" popper-class="dark-mode">
      <template #title>
        <el-icon><View /></el-icon>
        <span>授权管理</span>
      </template>
      <el-menu-item>角色管理</el-menu-item>
      <el-menu-item>权限管理</el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="3" popper-class="dark-mode">
      <template #title>
        <el-icon><Box /></el-icon>
        <span>应用管理</span>
      </template>
      <el-menu-item>应用管理</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<style scoped>
.layout-menu {
  border-right: none;
}
</style>