<script setup>
import { IconLogin2 } from '@tabler/icons-vue'
import { Key, Lock, User } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElLink, ElRow } from 'element-plus'
import { drawCaptcha } from '@/common/Captcha'
import { asyncApplyCaptcha, asyncPasswordLogin, asyncRefreshUser } from '@/common/AsyncRequest'
import { afterLogin } from '@/views/home/common'
import RegisterUser from '@/views/user/RegisterUser'

const route = useRoute()
const captcha = ref('')
const image = ref()
const openRegister = ref(false)
const formRef = useTemplateRef('form')
const loginForm = reactive({})
const formRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'change' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'change' }]
})

const refreshCaptcha = async () => captcha.value = await asyncApplyCaptcha()

const login = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncPasswordLogin(loginForm)) return
  await asyncRefreshUser()
  await afterLogin(route)
}

onMounted(async () => refreshCaptcha())
watchEffect(() => {
  if (!captcha.value) return
  const config = {
    width: 85,
    height: 32,
    backgroundColor: '#f7f7f7',
    fontColor: '#8b8c8c',
    font: 'italic 18px Arial'
  }
  image.value = drawCaptcha(captcha.value, config)
})
</script>

<template>
  <el-form ref="form" :model="loginForm" :rules="formRules">
    <el-form-item prop="username">
      <el-input v-model="loginForm.username" clearable placeholder="请输入用户名" :prefix-icon="User" />
    </el-form-item>
    <el-form-item prop="password">
      <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"
                show-password clearable :prefix-icon="Lock" />
    </el-form-item>
    <el-row>
      <el-col :span="16">
        <el-form-item prop="captcha">
          <el-input v-model="loginForm.captcha" clearable placeholder="请输入验证码" :prefix-icon="Key" />
        </el-form-item>
      </el-col>
      <el-col :span="8" class="d-flex align-items-center justify-content-end">
        <img :src="image" class="mb-4" title="点击刷新验证码" alt="验证码" @click="refreshCaptcha" />
      </el-col>
    </el-row>
    <el-form-item>
      <el-button type="primary" class="w-100p mt-4" @click="login">
        <IconLogin2 size="20" class="mr-1" />
        <span>登录</span>
      </el-button>
    </el-form-item>
    <el-row class="prompt" align="middle" justify="center">
      <span>没有账号？</span>
      <el-link class="fs-10px cursor-pointer mr-1" :underline="false" @click="openRegister = true">
        <span>注册</span>
      </el-link>
    </el-row>
  </el-form>
  <register-user v-model="openRegister" @close="refreshCaptcha" />
</template>

<style scoped>
:deep(.el-input__wrapper) {
  background-color: #f7f7f7;
  box-shadow: none;
}
:deep(.el-input__inner::placeholder){
  color: #bfbfbf;
  font-size: 12px;
  font-weight: 500;
}
.prompt {
  margin-top: 25px;
  line-height: 1.0;
  font-size: 10px;
  height: 10px;
}
</style>