<script setup>
import { onMounted, reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification, ElOption, ElSelect,
} from 'element-plus'
import { remoteGetRole, remoteUpdateRole } from '@/utils/hylian-service'
import { fetchAllApps, formRules } from './common'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const apps = ref([])
const roleForm = reactive({
  id: '',
  name: '',
  app_id: '',
})

const submitForm = async (formEl) => {
  if (!await formEl.validate(valid => valid)) return
  if (!await remoteUpdateRole(roleForm)) {
    ElNotification.error('编辑角色失败')
    return
  }
  ElNotification.success('编辑角色成功')
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const role = await remoteGetRole(props.id)
  if (!role) return
  roleForm.id = role.id
  roleForm.name = role.name
  roleForm.app_id = role.app_id
})

onMounted(async () => apps.value = await fetchAllApps())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>授权管理</el-breadcrumb-item>
    <el-breadcrumb-item>角色</el-breadcrumb-item>
    <el-breadcrumb-item>编辑</el-breadcrumb-item>
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
      <el-button @click="submitForm(formRef)">保存</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>