<script setup>
import { onMounted, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElInput, ElMessage } from 'element-plus'
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
  const imageElement = document.getElementById('image')
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
        <label for="verification_code">验证码</label>
      </div>
      <div class="form-row-input">
        <el-input id="captcha" type="text" v-model.trim="captcha" style="width: 150px;" :clearable="true" placeholder="请输入验证码"></el-input>
        <img id="image" style="padding-left: 10px;" src="" title="点击刷新验证码" alt="图片验证码" @click="refreshCaptcha" />
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