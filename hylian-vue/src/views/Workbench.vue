<script setup>
import { ref } from 'vue'
import { Box, User, View } from '@element-plus/icons-vue'
import {
  ElAside, ElContainer, ElDialog, ElFooter, ElHeader,
  ElIcon, ElMain, ElMenu, ElMenuItem, ElRow, ElSubMenu
} from 'element-plus'
import { logout } from '@/common/assortment'
import UserProfile from '@/components/user/UserProfile'
import ChangePassword from '@/views/user/Password'

const passwordVisible = ref(false)
</script>

<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <el-dialog v-model="passwordVisible" align-center show-close>
        <ChangePassword @close="passwordVisible = false"></ChangePassword>
      </el-dialog>
      <el-row justify="end">
        <UserProfile></UserProfile>
        <span style="padding-left: 10px;"><a @click="passwordVisible = true">修改密码</a></span>
        <span style="padding-left: 10px;"><a @click="logout()">注销</a></span>
      </el-row>
    </el-header>
    <el-container>
      <el-aside class="layout-aside">
        <el-menu class="layout-menu" :collapse="false">
          <el-sub-menu index="1" popper-class="dark-mode">
            <template #title>
              <el-icon><User /></el-icon>
              <span>账号管理</span>
            </template>
            <el-menu-item index="1-1">
              <RouterLink :to="{ name: 'UserList' }">用户列表</RouterLink>
            </el-menu-item>
            <el-menu-item index="1-2">
              <RouterLink :to="{ name: 'TenantList' }">租户列表</RouterLink>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="2" popper-class="dark-mode">
            <template #title>
              <el-icon><View /></el-icon>
              <span>授权管理</span>
            </template>
            <el-menu-item index="2-1">
              <RouterLink :to="{ name: 'RoleList' }">角色列表</RouterLink>
            </el-menu-item>
            <el-menu-item index="2-2">
              <RouterLink :to="{ name: 'PermissionList' }">权限列表</RouterLink>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3" popper-class="dark-mode">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>应用管理</span>
            </template>
            <el-menu-item index="3-1">
              <RouterLink :to="{ name: 'AppList' }">应用列表</RouterLink>
            </el-menu-item>
            <el-menu-item index="3-2">
              <RouterLink :to="{ name: 'ActivityList' }">活跃记录</RouterLink>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main>
        <RouterView></RouterView>
      </el-main>
    </el-container>
    <el-footer class="layout-footer">
      <el-row align="middle" justify="center" style="height: 100%">
        <small>copyright &copy; 2024 manong.xin</small>
      </el-row>
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  border: #888888 solid 1px;
}
.layout-header {
  height: 46px;
  background-image: url("@/assets/banner.jpg");
}
.layout-aside {
  width: 150px;
  border-right: #888888 solid 1px;
}
.layout-footer {
  border-top: #888888 solid 1px;
}
.layout-menu {
  border-right: none;
}
</style>