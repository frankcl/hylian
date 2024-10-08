<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm, ElFormItem, ElIcon, ElInput } from 'element-plus'
import { asyncAddApp } from '@/common/service'
import { submitForm } from '@/common/assortment'
import { formRules, refreshAppSecret } from '@/views/app/common'

const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const appForm = reactive({
  name: '',
  secret: ''
})

const submit = async formEl => {
  if (!await submitForm(formEl, appForm, asyncAddApp,
    '新增应用成功', '新增应用失败')) return
  emits('close')
}
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>应用管理</el-breadcrumb-item>
    <el-breadcrumb-item>应用</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="appForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="应用名" prop="name">
      <el-input v-model.trim="appForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="应用秘钥" prop="secret">
      <el-col :span="22">
        <el-input v-model.trim="appForm.secret" clearable></el-input>
      </el-col>
      <el-col :offset="1" :span="1">
        <el-icon @click="refreshAppSecret(appForm)"><Refresh /></el-icon>
      </el-col>
    </el-form-item>
    <el-form-item>
      <el-button @click="submit(formRef)">新增</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>