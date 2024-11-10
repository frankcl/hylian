<script setup>
import { reactive, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol,
  ElDialog, ElForm, ElFormItem, ElInput, ElRow,
} from 'element-plus'
import { asyncGetUser, asyncUpdateUser } from '@/common/service'
import { submitForm } from '@/common/assortment'
import AvatarUpload from '@/components/user/AvatarUpload'
import TenantSelect from '@/components/tenant/TenantSelect'
import { baseRules } from '@/views/user/common'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const model = defineModel()
const formRef = useTemplateRef('formRef')
const userForm = reactive({
  id: '',
  username: '',
  name: '',
  tenant_id: '',
  disabled: false,
  avatar: null,
})
const formRules = { ... baseRules }

const submit = async formEl => {
  if (!await submitForm(formEl, userForm, asyncUpdateUser,
    '编辑用户成功', '编辑用户失败')) return
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const user = await asyncGetUser(props.id)
  userForm.id = user.id
  userForm.name = user.name
  userForm.username = user.username
  userForm.tenant_id = user.tenant.id
  userForm.disabled = user.disabled
  userForm.avatar = user.avatar
})
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="650" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>编辑用户</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="userForm" :rules="formRules" label-width="auto" label-position="right">
      <el-row>
        <el-col :span="16">
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="userForm.username" clearable></el-input>
          </el-form-item>
          <el-form-item label="用户名称" prop="name">
            <el-input v-model.trim="userForm.name" clearable></el-input>
          </el-form-item>
          <el-form-item label="租户" prop="tenant_id">
            <tenant-select v-model="userForm.tenant_id" placeholder="请选择"></tenant-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="submit(formRef)">保存</el-button>
            <el-button @click="formRef.resetFields()">重置</el-button>
          </el-form-item>
        </el-col>
        <el-col :span="6" :offset="1">
          <el-row justify="center" style="margin-bottom: 10px">
            <avatar-upload v-model="userForm.avatar" :size="150"></avatar-upload>
          </el-row>
          <el-row justify="center">上传头像</el-row>
        </el-col>
      </el-row>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>