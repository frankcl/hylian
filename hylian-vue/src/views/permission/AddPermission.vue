<script setup>
import { onMounted, reactive, ref, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification, ElOption, ElSelect,
} from 'element-plus'
import { remoteAddPermission, remoteSearchApp } from '@/utils/hylian-service'

const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const apps = ref([])
const permissionForm = reactive({
  name: '',
  resource: '',
  app_id: ''
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入权限名称', trigger: 'change' }
  ],
  resource: [
    { required: true, message: '请输入资源路径', trigger: 'change' },
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate(valid => valid)) return
  if (!await remoteAddPermission(permissionForm)) {
    ElNotification.error('新增权限失败')
    return
  }
  ElNotification.success('新增权限成功')
  emits('close')
}

onMounted(async () => {
  const pager = await remoteSearchApp({})
  if (pager) apps.value = pager.records
})
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>授权管理</el-breadcrumb-item>
    <el-breadcrumb-item>权限</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="permissionForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="所属应用" prop="app_id">
      <el-select v-model="permissionForm.app_id" filterable placeholder="请选择应用">
        <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="权限名称" prop="name">
      <el-input v-model.trim="permissionForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="资源路径" prop="resource">
      <el-input v-model.trim="permissionForm.resource" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submitForm(formRef)">新增</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>