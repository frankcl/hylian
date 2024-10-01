<script setup>
import { onMounted, reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElButton, ElCol, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { paintCaptcha } from '@/utils/hylian'
import { remoteApplyCaptcha, remoteGetCurrentUser, remotePasswordLogin } from '@/utils/hylian-service'

const router = useRouter()
const userStore = useUserStore()
const captcha = ref('')
const userFormRef = useTemplateRef('userFormRef')
const userForm = reactive({
  username: '',
  password: '',
  captcha: ''
})
const rules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'change' }
  ]
})

onMounted(async () => captcha.value = await remoteApplyCaptcha())

watchEffect(() => {
  if (!captcha.value) return
  const config = {
    width: 84,
    height: 32,
    backgroundColor: '#f7f7f7',
    fontColor: '#8b8c8c',
    font: 'italic 18px Arial'
  }
  const image = paintCaptcha(captcha.value, config)
  const imageElement = document.getElementById('image-captcha')
  imageElement.src = image
})

async function refreshCaptcha() {
  captcha.value = await remoteApplyCaptcha()
}

async function submitForm(formEl) {
  if (!formEl) return
  if (!await formEl.validate((valid) => valid)) return
  if (!await remotePasswordLogin(userForm)) return
  const user = await remoteGetCurrentUser()
  if (user) userStore.inject(user)
  await router.push('/workbench')
}
</script>

<template>
  <el-form ref="userFormRef" :model="userForm" :rules="rules">
    <el-form-item prop="username">
      <el-input v-model.trim="userForm.username" clearable placeholder="请输入用户名"></el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input type="password" v-model.trim="userForm.password" show-password
                clearable placeholder="请输入密码"></el-input>
    </el-form-item>
    <el-form-item>
      <el-col :span="13">
        <el-form-item prop="captcha">
          <el-input id="input-captcha" v-model.trim="userForm.captcha" clearable
                    placeholder="请输入验证码"></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="8" :offset="3" class="captcha">
        <img id="image-captcha" src="" title="点击刷新验证码" alt="图片验证码" @click="refreshCaptcha" />
      </el-col>
    </el-form-item>
    <el-form-item>
      <el-button style="width: 100%" color="#6077ff" @click="submitForm(userFormRef)">登录</el-button>
    </el-form-item>
    <el-row class="register-prompt" align="middle" justify="center">
      <span>没有账号？</span><a>注册</a><span>一个</span>
    </el-row>
  </el-form>
</template>

<style scoped>
:deep(.el-input__wrapper) {
  background-color: #f7f7f7;
  box-shadow: none;
}
:deep(.el-input__inner::placeholder){
  color: #bfbfbf;
  font-size: 11px;
  font-weight: bolder;
}
.register-prompt {
  margin-top: 23px;
  line-height: 1.0;
  font-size: 8px;
  height: 8px;
}
.captcha {
  display: flex;
  align-items: center;
  margin-left: 20px;
}
</style>