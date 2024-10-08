<script setup>
import { reactive, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem, ElInput } from 'element-plus'
import { asyncGetTenant, asyncUpdateTenant } from '@/common/service'
import { submitForm } from '@/common/assortment'
import { formRules } from '@/views/tenant/common'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const tenantForm = reactive({
  id: '',
  name: ''
})

const submit = async formEl => {
  if (!await submitForm(formEl, tenantForm, asyncUpdateTenant,
    '编辑租户成功', '编辑租户失败')) return
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const tenant = await asyncGetTenant(props.id)
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
  <el-form ref="formRef" :model="tenantForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="租户名" prop="name">
      <el-input v-model.trim="tenantForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submit(formRef)">保存</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>