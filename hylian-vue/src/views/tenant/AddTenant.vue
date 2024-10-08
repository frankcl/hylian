<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem, ElInput } from 'element-plus'
import { asyncAddTenant } from '@/common/service'
import { submitForm } from '@/common/assortment'
import { formRules } from '@/views/tenant/common'

const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const tenantForm = reactive({
  name: ''
})

const submit = async formEl => {
  if (!await submitForm(formEl, tenantForm, asyncAddTenant,
    '新增租户成功', '新增租户失败')) return
  emits('close')
}
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>租户</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="tenantForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="租户名" prop="name">
      <el-input v-model.trim="tenantForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submit(formRef)">新增</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>