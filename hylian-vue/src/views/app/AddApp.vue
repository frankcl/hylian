<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElForm,
  ElFormItem, ElIcon,
  ElInput,
  ElNotification,
} from 'element-plus'
import { remoteAddApp, remoteCreateRandomSecret } from '@/utils/hylian-service'

const emits = defineEmits(['close'])
const appFormRef = useTemplateRef('appFormRef')
const appForm = reactive({
  name: '',
  secret: ''
})
const appRules = reactive({
  name: [
    { required: true, message: '请输入应用名', trigger: 'change' }
  ],
  secret: [
    { required: true, message: '请输入应用秘钥', trigger: 'change' },
    { min: 8, message: '应用秘钥至少8位', trigger: 'change' }
  ]
})

const refreshAppSecret = async () => {
  const secret = await remoteCreateRandomSecret()
  if (secret) appForm.secret = secret
}

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate(valid => valid)) return
  if (!await remoteAddApp(appForm)) {
    ElNotification.error('新增应用失败')
    return
  }
  ElNotification.success('新增应用成功')
  emits('close')
}
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>应用管理</el-breadcrumb-item>
    <el-breadcrumb-item>应用</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="appFormRef" :model="appForm" :rules="appRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="应用名" prop="name">
      <el-input v-model.trim="appForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="应用秘钥" prop="secret">
      <el-col :span="22">
        <el-input v-model.trim="appForm.secret" clearable></el-input>
      </el-col>
      <el-col :offset="1" :span="1">
        <el-icon @click="refreshAppSecret"><Refresh /></el-icon>
      </el-col>
    </el-form-item>
    <el-form-item>
      <el-button @click="submitForm(appFormRef)">新增</el-button>
      <el-button @click="appFormRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>