<script setup>
import { onMounted, reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus'
import { asyncGetPermission, asyncUpdatePermission } from '@/common/service'
import { fetchAllApps, submitForm } from '@/common/assortment'
import { formRules } from '@/views/permission/common'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const apps = ref([])
const permissionForm = reactive({
  id: '',
  name: '',
  path: '',
  app_id: '',
})

const submit = async formEl => {
  if (!await submitForm(formEl, permissionForm, asyncUpdatePermission,
    '编辑权限成功', '编辑权限失败')) return
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const permission = await asyncGetPermission(props.id)
  permissionForm.id = permission.id
  permissionForm.name = permission.name
  permissionForm.path = permission.path
  permissionForm.app_id = permission.app_id
})

onMounted(async () => apps.value = await fetchAllApps())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>授权管理</el-breadcrumb-item>
    <el-breadcrumb-item>权限</el-breadcrumb-item>
    <el-breadcrumb-item>编辑</el-breadcrumb-item>
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
    <el-form-item label="资源路径" prop="path">
      <el-input v-model.trim="permissionForm.path" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submit(formRef)">保存</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>