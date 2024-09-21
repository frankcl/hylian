<script setup>
import {ref, watchEffect} from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { httpRequest } from '@/utils/http'
import { ElInput, ElMessage } from 'element-plus'
import { createVerifyCodeImage } from '@/utils/common'

const router = useRouter()
const username = ref('')
const password = ref('')
const verifyCode = ref('')
const userStore = useUserStore()

watchEffect(() => refreshVerifyCode())

async function getCurrentUser() {
  const { code, data } = await httpRequest({
    method: 'get',
    url: '/api/user/getCurrentUser'
  })
  if (code === 200) userStore.setUser(data)
}

async function refreshVerifyCode() {
  const { code, data } = await httpRequest({
    method: 'get',
    url: '/api/verifyCode/create'
  })
  if (code !== 200) return ''
  const href = createVerifyCodeImage(data)
  const image = document.getElementById('verify_code');
  image.src = href
}

function checkLogin() {
  if (!username.value || username.value === '') {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!password.value || password.value === '') {
    ElMessage.error('请输入密码')
    return false
  }
  if (!verifyCode.value || verifyCode.value === '') {
    ElMessage.error('请输入验证码')
    return false
  }
  return true
}

async function login() {
  if (!checkLogin()) return false
  const { code } = await httpRequest({
    method: 'post',
    url: '/api/security/login',
    data: {
      username: username.value,
      password: password.value,
      verify_code: verifyCode.value
    }
  })
  if (code !== 200) return
  await getCurrentUser()
  await router.push('/home')
}
</script>

<template>
  <div class="form-login">
    <div class="form-row">
      <div class="form-row-label">
        <label for="username">用户名</label>
      </div>
      <div class="form-row-input">
        <el-input id="username" type="text" v-model.trim="username" style="width: 200px;" :clearable="true" placeholder="请输入用户名"></el-input>
      </div>
    </div>
    <div class="form-row">
      <div class="form-row-label">
        <label for="password">密码</label>
      </div>
      <div class="form-row-input">
        <el-input id="password" type="password" v-model.trim="password" style="width: 200px" :clearable="true" :show-password="true" placeholder="请输入密码"></el-input>
      </div>
    </div>
    <div class="form-row">
      <div class="form-row-label">
        <label for="verifyCode">验证码</label>
      </div>
      <div class="form-row-input">
        <el-input id="verifyCode" type="text" v-model.trim="verifyCode" style="width: 150px;" :clearable="true" placeholder="请输入验证码"></el-input>
        <img id="verify_code" style="padding-left: 10px;" src="" title="点击刷新验证码" alt="图片验证码" @click="refreshVerifyCode" />
      </div>
    </div>
    <div class="form-row">
      <button @click="login">登录</button>
    </div>
  </div>
</template>

<style scoped>
.form-login {
  border: 1px solid #888888;
  border-top: none;
  display: flex;
  flex-flow: row wrap;
  height: 250px;
}
.form-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1 1 calc(100% - 10px);
  padding: 5px;
  height: 50px;
}
.form-row-label {
  justify-self: end;
  flex-basis: 55px;
  padding: 5px 5px 5px 25px;
}
.form-row-input {
  justify-self: start;
  flex: 1 1 auto;
  display: flex;
  padding: 5px 5px 5px 10px;
}
</style>