<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import {ElAvatar, ElCol, ElDialog, ElRow} from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/'
import { logout } from '@/utils/hylian-service'
import UserProfile from '@/components/user/UserProfile'
import { ref } from 'vue'
import AddUser from '@/components/user/AddUser'

const userStore = useUserStore()
const registerDialogVisible = ref(false)
const profileProps = ref({
  positionX: 0,
  positionY: 0,
  show: false
})

function openUserProfileCard(event) {
  console.log(event.x, event.y)
  profileProps.value.positionX = event.x
  profileProps.value.positionY = event.y
  profileProps.value.show = true
}

function closeUserProfileCard() {
  profileProps.value.show = false
}

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
        <a tabindex="1" @blur="closeUserProfileCard" @click="openUserProfileCard">
        <el-avatar :icon="UserFilled" shape="circle" size="small" fit="cover" :src="userStore.avatar"
                    >
        </el-avatar>
        </a>
        <UserProfile v-bind="profileProps"></UserProfile>
        <span style="padding-left: 10px;"><a @click.prevent="executeLogout">注销</a></span>
        <span style="padding-left: 10px;"><a @click="registerDialogVisible=true">添加新用户</a></span>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="registerDialogVisible" title="新用户注册" align-center show-close>
    <AddUser></AddUser>
  </el-dialog>
</template>

<style scoped>
</style>