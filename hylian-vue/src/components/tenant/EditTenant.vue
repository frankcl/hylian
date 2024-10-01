<script setup>
import { reactive, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification,
} from 'element-plus'
import { remoteGetTenant, remoteUpdateTenant } from '@/utils/hylian-service'
import { resetForm } from '@/utils/hylian'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const tenantFormRef = useTemplateRef('tenantFormRef')
const tenantForm = reactive({
  id: '',
  name: ''
})

const tenantRules = reactive({
  name: [
    { required: true, message: '请输入租户名', trigger: 'change' }
  ]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate(valid => valid)) return
  if (!await remoteUpdateTenant(tenantForm)) {
    ElNotification.error('编辑租户失败')
    return
  }
  ElNotification.success('编辑租户成功')
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const tenant = await remoteGetTenant(props.id)
  if (!tenant) return
  tenantForm.id = tenant.id
  tenantForm.name = tenant.name
})
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>租户</el-breadcrumb-item>
    <el-breadcrumb-item>编辑</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="tenantFormRef" :model="tenantForm" :rules="tenantRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="租户名" prop="name">
      <el-input v-model.trim="tenantForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submitForm(tenantFormRef)">保存</el-button>
      <el-button @click="resetForm(tenantFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>