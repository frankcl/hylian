<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { asyncAddTenant } from '@/common/service'
import { submitForm } from '@/common/assortment'

const emits = defineEmits(['close'])
const model = defineModel()
const formRef = useTemplateRef('formRef')
const tenantForm = reactive({
  name: ''
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入租户', trigger: 'change' }
  ]
})

const submit = async formEl => {
  if (!await submitForm(formEl, tenantForm, asyncAddTenant,
    '新增租户成功', '新增租户失败')) return
  model.value = false
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="450" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>新增租户</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="tenantForm" :rules="formRules"
             label-width="auto" label-position="right">
      <el-form-item label="租户名" prop="name">
        <el-input v-model.trim="tenantForm.name" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submit(formRef)">新增</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>