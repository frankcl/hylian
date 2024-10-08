<script setup>
import { onMounted, reactive, ref, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm,
  ElFormItem, ElInput, ElOption, ElRow, ElSelect, ElSwitch
} from 'element-plus'
import { asyncAddUser } from '@/common/service'
import { fetchAllTenants, submitForm } from '@/common/assortment'
import AvatarUpload from '@/components/user/AvatarUpload'
import { baseRules, disableUser } from '@/views/user/common'

const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const tenants = ref([])
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

onMounted(async () => tenants.value = await fetchAllTenants())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>用户</el-breadcrumb-item>
    <el-breadcrumb-item>新增</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="userForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-row>
      <el-col :span="16">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="userForm.username" clearable></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model.trim="userForm.name" clearable></el-input>
        </el-form-item>
        <el-form-item label="租户" prop="tenant_id">
          <el-select v-model="userForm.tenant_id" filterable placeholder="请选择租户">
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否禁用" prop="disabled">
          <el-switch v-model="userForm.disabled" @change="v => disableUser(v, userForm)" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model.trim="userForm.password" show-password clearable></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm_password">
          <el-input type="password" v-model.trim="userForm.confirm_password" show-password clearable></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="6" :offset="2">
        <el-form-item>
          <AvatarUpload :width="100" :height="100" @finish="url => userForm.avatar = url"></AvatarUpload>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button @click="submit(formRef)">新增</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>