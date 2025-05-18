<script setup>
import { IconArrowBackUp, IconUserPlus } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ElButton, ElCol, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { asyncApplyCaptcha, asyncRegister } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { drawCaptcha } from '@/common/Captcha'
import HylianCard from '@/components/data/Card'
import { appFormRules } from '@/views/user/common'

const open = defineModel()
const emits = defineEmits(['close'])
const captcha = ref('')
const image = ref()
const formRef = useTemplateRef('form')
const user = reactive({})
const formRules = {
  ... appFormRules,
  captcha: [{ required: true, trigger: 'change', message: '请输入验证码' }],
  confirm_password: [{
    required: true, trigger: 'change', validator: (rule, value, callback) => {
      if (!value || value === '') callback(new Error('请输入确认密码'))
      else if (value !== user.password) callback(new Error('确认密码与密码不一致'))
      else callback()
    }
  }]
}

const refreshCaptcha = async () => captcha.value = await asyncApplyCaptcha()

const register = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncRegister(user)) {
    showMessage('注册用户失败', ERROR)
    return
  }
  showMessage('注册用户成功', SUCCESS)
  open.value = false
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
  image.value = drawCaptcha(captcha.value, config)
})
</script>

<template>
  <el-dialog v-model="open" @open="refreshCaptcha" @close="emits('close')" align-center show-close>
    <hylian-card title="新用户注册">
      <el-form ref="form" :model="user" :rules="formRules" label-width="80px" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="user.username" clearable />
        </el-form-item>
        <el-form-item label="用户昵称" prop="name">
          <el-input v-model="user.name" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="user.password" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm_password">
          <el-input type="password" v-model="user.confirm_password" show-password clearable />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="21">
            <el-form-item label="验证码" prop="captcha">
              <el-input v-model.trim="user.captcha" clearable placeholder="请输入验证码" />
            </el-form-item>
          </el-col>
          <el-col :span="3" class="d-flex align-items-center justify-content-end">
            <img :src="image" class="mt-3" title="点击刷新验证码" alt="验证码" @click="refreshCaptcha" />
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="register">
            <IconUserPlus size="20" class="mr-1" />
            <span>注册</span>
          </el-button>
          <el-button type="info" @click="formRef.resetFields()">
            <IconArrowBackUp size="20" class="mr-1" />
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>
</style>