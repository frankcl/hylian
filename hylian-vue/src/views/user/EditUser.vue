<script setup>
import {onMounted, reactive, ref, useTemplateRef, watchEffect} from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm,
  ElFormItem, ElInput, ElOption, ElRow, ElSelect, ElSwitch,
} from 'element-plus'
import { asyncGetUser, asyncUpdateUser } from '@/common/service'
import { fetchAllTenants, submitForm } from '@/common/assortment'
import AvatarUpload from '@/components/user/AvatarUpload'
import { baseRules, disableUser } from '@/views/user/common'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const avatar = ref()
const tenants = ref([])
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
  avatar.value = user.avatar && user.avatar !== '' ? user.avatar : null
})
onMounted(async () => tenants.value = await fetchAllTenants())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>用户</el-breadcrumb-item>
    <el-breadcrumb-item>编辑</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="formRef" :model="userForm" :rules="formRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-row>
      <el-col :span="16">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="userForm.username" disabled clearable></el-input>
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
          <el-switch v-model="userForm.disabled" @change="v => disableUser(v, userForm)"/>
        </el-form-item>
      </el-col>
      <el-col :span="6" :offset="2">
        <el-form-item>
          <AvatarUpload :width="100" :height="100" :avatar="avatar"
                        @finish="url => userForm.avatar = url"></AvatarUpload>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button @click="submit(formRef)">保存</el-button>
      <el-button @click="formRef.resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>