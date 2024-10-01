<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem, ElInput, ElNotification } from 'element-plus'
import { useUserStore } from '@/store'
import { resetForm } from '@/utils/hylian'
import { remoteChangePassword } from '@/utils/hylian-service'

const emits = defineEmits(['close'])
const userStore = useUserStore()
const passwordFormRef = useTemplateRef('passwordFormRef')
const passwordForm = reactive({
  password: '',
  new_password: '',
  confirm_password: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value || value === '') callback(new Error('请输入确认密码'))
  else if (value !== passwordForm.new_password) callback(new Error('确认密码与密码不一致'))
  else callback()
}

const passwordRule = reactive({
  password: [
    { required: true, message: '请输入原始密码', trigger: 'change' }
  ],
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
  confirm_password: [
    { required: true, trigger: 'change', validator: validateConfirmPassword }
  ]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate(valid => valid)) return
  const request = {
    id: userStore.id,
    password: passwordForm.password,
    new_password: passwordForm.new_password,
    confirm_password: passwordForm.confirm_password
  }
  if (!await remoteChangePassword(request)) {
    ElNotification.error('修改密码失败')
    return
  }
  ElNotification.success('修改密码成功')
  emits('close')
}
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>用户</el-breadcrumb-item>
    <el-breadcrumb-item>修改密码</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="passwordFormRef" label-width="auto" label-position="right"
           style="margin-top: 20px" :model="passwordForm" :rules="passwordRule">
    <el-form-item label="原始密码" prop="password">
      <el-input type="password" v-model="passwordForm.password" clearable show-password></el-input>
    </el-form-item>
    <el-form-item label="新密码" prop="new_password">
      <el-input type="password" v-model="passwordForm.new_password" clearable show-password></el-input>
    </el-form-item>
    <el-form-item label="重复密码" prop="confirm_password">
      <el-input type="password" v-model="passwordForm.confirm_password" clearable show-password></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submitForm(passwordFormRef)">修改密码</el-button>
      <el-button @click="resetForm(passwordFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>

</style>