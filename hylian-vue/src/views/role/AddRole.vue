<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { asyncAddRole } from '@/common/service'
import { submitForm } from '@/common/assortment'
import AppSelect from '@/components/app/AppSelect'

const model = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const roleForm = reactive({
  name: '',
  app_id: ''
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'change' }
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})

const submit = async formEl => {
  if (!await submitForm(formEl, roleForm, asyncAddRole,
    '新增角色成功', '新增角色失败')) return
  emits('close')
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>授权管理</el-breadcrumb-item>
        <el-breadcrumb-item>新增角色</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="roleForm" :rules="formRules"
             label-width="auto" label-position="right">
      <el-form-item label="所属应用" prop="app_id">
        <app-select v-model="roleForm.app_id" placeholder="请选择"></app-select>
      </el-form-item>
      <el-form-item label="角色名称" prop="name">
        <el-input v-model.trim="roleForm.name" clearable></el-input>
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