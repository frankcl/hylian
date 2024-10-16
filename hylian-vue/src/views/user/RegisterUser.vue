<script setup>
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ElButton, ElCol, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import {
  asyncApplyCaptcha,
  asyncRegister
} from '@/common/service'
import {
  drawCaptcha,
  submitForm
} from '@/common/assortment'
import { baseRules } from '@/views/user/common'

const model = defineModel()
const emits = defineEmits(['close'])
const captcha = ref('')
const formRef = useTemplateRef('formRef')
const captchaRef = useTemplateRef('captchaRef')
const userForm = reactive({
  username: '',
  name: '',
  password: '',
  confirm_password: '',
  captcha: ''
})
const formRules = {
  ... baseRules,
  ... {
    captcha: [
      { required: true, trigger: 'change', message: '请输入验证码' }
    ],
    confirm_password: [
      { required: true, trigger: 'change', validator: (rule, value, callback) => {
        if (!value || value === '') callback(new Error('请输入确认密码'))
        else if (value !== userForm.password) callback(new Error('确认密码与密码不一致'))
        else callback()
      } }
    ]
  }}

const refreshCaptcha = async () => captcha.value = await asyncApplyCaptcha()

const submit = async formEl => {
  if (!await submitForm(formEl, userForm, asyncRegister,
    '注册用户成功', '注册用户失败')) return
  emits('close')
}

watchEffect(() => {
  if (!captcha.value) return
  const config = {
    width: 84,
    height: 32,
    backgroundColor: '#f7f7f7',
    fontColor: '#8b8c8c',
    font: 'italic 18px Arial'
  }
  captchaRef.value.src = drawCaptcha(captcha.value, config)
})
</script>

<template>
  <el-dialog v-model="model" @open="refreshCaptcha()" @close="emits('close')" align-center show-close>
    <el-row>
      <h2>新用户注册</h2>
    </el-row>
    <el-form ref="formRef" :model="userForm" :rules="formRules" style="margin-top: 20px;"
             label-width="auto" label-position="right">
      <el-form-item label="用户名" prop="username">
        <el-input v-model.trim="userForm.username" clearable></el-input>
      </el-form-item>
      <el-form-item label="用户名称" prop="name">
        <el-input v-model.trim="userForm.name" clearable></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model.trim="userForm.password" show-password clearable></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirm_password">
        <el-input type="password" v-model.trim="userForm.confirm_password" show-password clearable></el-input>
      </el-form-item>
      <el-form-item label="验证码" prop="captcha">
        <el-col :span="21">
          <el-form-item prop="captcha">
            <el-input v-model.trim="userForm.captcha" clearable placeholder="请输入验证码"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="2" :offset="1" class="captcha">
          <img ref="captchaRef" src="" title="点击刷新验证码" alt="验证码" @click="refreshCaptcha" />
        </el-col>
      </el-form-item>
      <el-form-item>
        <el-button @click="submit(formRef)">注册</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>