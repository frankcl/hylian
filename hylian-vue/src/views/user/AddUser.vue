<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElForm,
  ElFormItem, ElInput, ElRow, ElSwitch
} from 'element-plus'
import { asyncAddUser } from '@/common/service'
import { submitForm } from '@/common/assortment'
import AvatarUpload from '@/components/user/AvatarUpload'
import TenantSelect from '@/components/tenant/TenantSelect'
import { baseRules } from '@/views/user/common'

const model = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const userForm = reactive({
  username: '',
  name: '',
  avatar: '',
  tenant_id: '',
  password: '',
  confirm_password: '',
  disabled: false
})
const formRules = {
  ... baseRules,
  ... {
    confirm_password: [
      { required: true, trigger: 'change', validator: (rule, value, callback) => {
        if (!value || value === '') callback(new Error('请输入确认密码'))
        else if (value !== userForm.password) callback(new Error('确认密码与密码不一致'))
        else callback()
      } }
    ]
  }}

const submit = async formEl => {
  if (!await submitForm(formEl, userForm,asyncAddUser,
    '新增用户成功', '新增用户失败')) return
  emits('close')
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>新增用户</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="userForm" :rules="formRules" style="margin-top: 20px;"
             label-width="auto" label-position="right">
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
          <el-form-item label="密码" prop="password">
            <el-input type="password" v-model.trim="userForm.password" show-password clearable></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirm_password">
            <el-input type="password" v-model.trim="userForm.confirm_password" show-password clearable></el-input>
          </el-form-item>
          <el-form-item label="用户状态" prop="disabled">
            <el-switch v-model="userForm.disabled" inline-prompt size="large" active-text="禁用" inactive-text="启用"
                       style="--el-switch-on-color: #ff4949; --el-switch-off-color: #409eff" />
          </el-form-item>
          <el-form-item>
            <el-button @click="submit(formRef)">新增</el-button>
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