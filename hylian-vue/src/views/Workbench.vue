<script setup>
import { ref } from 'vue'
import { ArrowDown, Box, User, View } from '@element-plus/icons-vue'
import {
  ElAside, ElContainer, ElDropdown, ElDropdownItem, ElDropdownMenu,
  ElFooter, ElHeader, ElIcon, ElMain, ElMenu, ElMenuItem, ElRow, ElSubMenu
} from 'element-plus'
import { useUserStore } from '@/store'
import { logout } from '@/common/assortment'
import UserProfile from '@/components/user/UserProfile'
import PasswordDialog from '@/views/user/PasswordDialog'
import EditUserDialog from '@/views/user/EditUserDialog'

const userStore = useUserStore()
const openEditDialog = ref(false)
const openPasswordDialog = ref(false)

const handleCommand = command => {
  if (command === 'edit') openEditDialog.value = true
  else if (command === 'password') openPasswordDialog.value = true
  else if (command === 'logout') logout()
}
</script>

<template>
  <el-container class="workbench-container">
    <el-header class="workbench-header">
      <password-dialog v-model="openPasswordDialog"
                       @close="openPasswordDialog = false"></password-dialog>
      <edit-user-dialog v-model="openEditDialog" :id="userStore.id"
                        @close="openEditDialog = false"></edit-user-dialog>
      <el-row style="margin-top: 5px" align="middle" justify="end">
        <user-profile></user-profile>&nbsp;&nbsp;
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="operations">
            用户操作
            <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit">编辑用户</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout">退出系统</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-row>
    </el-header>
    <el-container>
      <el-aside class="workbench-aside">
        <el-menu class="aside-menu" :collapse="false">
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
    <el-footer class="workbench-footer">
      <el-row align="middle" justify="center" style="height: 100%">
        <small>copyright &copy; 2024 manong.xin</small>
      </el-row>
    </el-footer>
  </el-container>
</template>

<style scoped>
.operations {
  font-size: 10px;
  cursor: pointer;
}
.workbench-container {
  height: 100vh;
}
.workbench-header {
  height: 46px;
  background-image: url("@/assets/banner.jpg");
}
.workbench-aside {
  width: 150px;
}
.workbench-footer {
}
.aside-menu {
  border-right: none;
}
</style>