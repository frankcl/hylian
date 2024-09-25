<script setup>
import { onMounted, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElButton, ElCol, ElInput, ElMessage, ElRow } from 'element-plus'
import { paintCaptcha } from '@/utils/routine'
import { applyCaptcha, getCurrentUser, passwordLogin } from '@/utils/backend'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const captcha = ref('')
const backendCaptcha = ref('')

onMounted(async () => backendCaptcha.value = await applyCaptcha())

watchEffect(() => {
  if (!backendCaptcha.value) return
  const image = paintCaptcha(backendCaptcha.value)
  const imageElement = document.getElementById('image-captcha')
  imageElement.src = image
})

function checkLogin() {
  if (!username.value || username.value === '') {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!password.value || password.value === '') {
    ElMessage.error('请输入密码')
    return false
  }
  if (!captcha.value || captcha.value === '') {
    ElMessage.error('请输入验证码')
    return false
  }
  return true
}

async function refreshCaptcha() {
  backendCaptcha.value = await applyCaptcha()
}

async function login() {
  if (!checkLogin()) return false
  const success = await passwordLogin(username.value, password.value, captcha.value)
  if (!success) return
  const user = await getCurrentUser()
  if (user) userStore.inject(user)
  await router.push('/workbench')
}
</script>

<template>
  <el-row class="row" align="middle">
    <el-col :span="6">
      <el-row justify="end">
        <label for="username">用户名</label>
      </el-row>
    </el-col>
    <el-col :span="17" :offset="1">
      <el-input id="username" type="text" v-model.trim="username" style="width: 200px;" :clearable="true" placeholder="请输入用户名"></el-input>
    </el-col>
  </el-row>
  <el-row class="row" align="middle">
    <el-col :span="6">
      <el-row justify="end">
        <label for="password">密码</label>
      </el-row>
    </el-col>
    <el-col :span="17" :offset="1">
      <el-input id="password" type="password" v-model.trim="password" style="width: 200px" :clearable="true" :show-password="true" placeholder="请输入密码"></el-input>
    </el-col>
  </el-row>
  <el-row class="row" align="middle">
    <el-col :span="6">
      <el-row justify="end">
        <label for="input-captcha">验证码</label>
      </el-row>
    </el-col>
    <el-col :span="17" :offset="1">
      <el-row align="middle">
        <el-input id="input-captcha" class="input-captcha" type="text" v-model.trim="captcha" :clearable="true" placeholder="请输入验证码"></el-input>
        <img id="image-captcha" src="" title="点击刷新验证码" alt="图片验证码" @click="refreshCaptcha" />
      </el-row>
    </el-col>
  </el-row>
  <el-row class="row" align="middle" justify="center">
    <el-button type="primary" @click="login">登录</el-button>
  </el-row>
</template>

<style scoped>
.row {
  margin-top: 15px;
  height: 45px;
}
.input-captcha {
  width: 150px;
  margin-right: 10px;
}
</style>