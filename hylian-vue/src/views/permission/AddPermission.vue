<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { asyncAddPermission } from '@/common/service'
import { submitForm } from '@/common/assortment'
import AppSelect from '@/components/app/AppSelect'

const model = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const permissionForm = reactive({
  name: '',
  path: '',
  app_id: ''
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入权限名称', trigger: 'change' }
  ],
  path: [
    { required: true, message: '请输入资源路径', trigger: 'change' },
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})

const submit = async formEl => {
  if (!await submitForm(formEl, permissionForm, asyncAddPermission,
    '新增权限成功', '新增权限失败')) return
  emits('close')
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>授权管理</el-breadcrumb-item>
        <el-breadcrumb-item>新增权限</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="permissionForm" :rules="formRules"
             label-width="auto" label-position="right">
      <el-form-item label="所属应用" prop="app_id">
        <app-select v-model="permissionForm.app_id" placeholder="请选择" :clearable="false"></app-select>
      </el-form-item>
      <el-form-item label="权限名称" prop="name">
        <el-input v-model.trim="permissionForm.name" clearable></el-input>
      </el-form-item>
      <el-form-item label="资源路径" prop="path">
        <el-input v-model.trim="permissionForm.path" clearable></el-input>
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