<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import { ElAvatar, ElCol, ElRow } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/'
import { logout } from '@/utils/backend'

const userStore = useUserStore()

async function executeLogout() {
  await logout()
  userStore.clear()
  await router.push('/')
}
</script>

<template>
  <el-row>
    <el-col :span="6">LOGO</el-col>
    <el-col :span="6" :offset="12">
      <el-row justify="end">
        <el-avatar :icon="UserFilled" shape="circle" size="small" fit="cover" :src="userStore.avatar"></el-avatar>
        <span style="padding-left: 10px;"><a @click.prevent="executeLogout">注销</a></span>
      </el-row>
    </el-col>
  </el-row>
</template>

<style scoped>

</style>