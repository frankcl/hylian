<script setup>
import { ref } from 'vue'
import { ElCol, ElDialog, ElRow } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/'
import { remoteLogout } from '@/utils/hylian-service'
import UserProfile from '@/components/user/UserProfile'
import ChangePassword from '@/components/user/ChangePassword'

const userStore = useUserStore()
const passwordDialogVisible = ref(false)

async function executeLogout() {
  await remoteLogout()
  userStore.clear()
  await router.push('/')
}
</script>

<template>
  <el-dialog v-model="passwordDialogVisible" align-center show-close>
    <ChangePassword @close="passwordDialogVisible = false"></ChangePassword>
  </el-dialog>
  <el-row>
    <el-col :span="6">LOGO</el-col>
    <el-col :span="6" :offset="12">
      <el-row justify="end">
        <UserProfile></UserProfile>
        <span style="padding-left: 10px;"><a @click="passwordDialogVisible = true">修改密码</a></span>
        <span style="padding-left: 10px;"><a @click="executeLogout">注销</a></span>
      </el-row>
    </el-col>
  </el-row>
</template>

<style scoped>
</style>