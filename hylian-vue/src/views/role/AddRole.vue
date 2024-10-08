<script setup>
import { onMounted, reactive, ref, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus'
import { asyncAddRole } from '@/common/service'
import { fetchAllApps, submitForm } from '@/common/assortment'
import { formRules } from '@/views/role/common'

const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const apps = ref([])
const roleForm = reactive({
  name: '',
  app_id: ''
})

const submit = async formEl => {
  if (!await submitForm(formEl, roleForm, asyncAddRole,
    '新增角色成功', '新增角色失败')) return
  emits('close')
}

onMounted(async () => apps.value = await fetchAllApps())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>授权管理</el-breadcrumb-item>
    <el-breadcrumb-item>角色</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="roleForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-form-item label="所属应用" prop="app_id">
      <el-select v-model="roleForm.app_id" filterable placeholder="请选择应用">
        <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="角色名称" prop="name">
      <el-input v-model.trim="roleForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="submit(formRef)">新增</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>