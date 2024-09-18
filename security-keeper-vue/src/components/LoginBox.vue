<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { httpRequest } from '@/utils/http'
import { ElButton, ElText, ElInput } from 'element-plus'

defineProps({
  width : Number,
  height: Number
})
const router = useRouter()
const username = ref('')
const password = ref('')
const userStore = useUserStore()

async function getUser() {
  const { data } = await httpRequest({
    method: 'get',
    url: '/api/user/getCurrentUser'
  })
  userStore.setUser(data)
}

async function submit() {
  const { code } = await httpRequest({
    method: 'post',
    url: '/api/security/login',
    data: {
      username: username.value,
      password: password.value
    }
  })
  if (code === 200) {
    await getUser()
    await router.push('/home')
    return
  }
  window.alert('登录失败')
}
</script>

<template>
  <div class="login-box" :style="{ width: width + 'px', height: height + 'px' }">
    <div class="row">
      <el-text style="width: 50px">用户名</el-text>
      <div style="text-align: right; width: 240px;">
        <el-input type="text" v-model.trim="username" style="width: 150px;" placeholder="请输入用户名"></el-input>
      </div>
    </div>
    <div class="row">
      <el-text>密码</el-text>
      <div style="text-align: right; width: 240px;">
        <el-input type="password" v-model.trim="password" style="width: 150px" :show-password="true" placeholder="请输入密码"></el-input>
      </div>
    </div>
    <div class="row" style="text-align: center">
      <el-button @click="submit">登录</el-button>
    </div>
  </div>
</template>

<style scoped>
.row {
  padding: 10px 10px;
  display: flex;
}
</style>