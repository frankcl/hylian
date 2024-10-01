<script setup>
import { onMounted, reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElNotification,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus'
import { remoteGetUser, remoteSearchTenant, remoteUpdateUser } from '@/utils/hylian-service'
import { resetForm } from '@/utils/hylian'
import AvatarUpload from '@/components/user/AvatarUpload'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const userFormRef = useTemplateRef('userFormRef')
const displayAvatar = ref()
const tenants = ref([])
const userForm = reactive({
  id: '',
  user_name: '',
  name: '',
  avatar: undefined,
  tenant_id: ''
})

const userRules = reactive({
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'change' }
  ],
  tenant_id: [
    { required: true, message: '请选择租户', trigger: 'change' }
  ]
})

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate(valid => valid)) return
  if (!await remoteUpdateUser(userForm)) {
    ElNotification.error('编辑用户失败')
    return
  }
  ElNotification.success('编辑用户成功')
  emits('close')
}

watchEffect(async () => {
  if (!props.id) return
  const user = await remoteGetUser(props.id)
  if (!user) return
  userForm.id = user.id
  userForm.name = user.name
  userForm['user_name'] = user['user_name']
  userForm['tenant_id'] = user.tenant.id
  displayAvatar.value = user.avatar && user.avatar !== '' ? user.avatar : undefined
})
onMounted(async () => {
  const pager = await remoteSearchTenant({ size: 1000 })
  if (pager) tenants.value = pager.records
})
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>用户</el-breadcrumb-item>
    <el-breadcrumb-item>编辑</el-breadcrumb-item>
  </el-breadcrumb>
  <el-form ref="userFormRef" :model="userForm" :rules="userRules" style="margin-top: 20px;"
           label-width="auto" label-position="right">
    <el-row>
      <el-col :span="16">
        <el-form-item label="用户名" prop="user_name">
          <el-input v-model.trim="userForm['user_name']" disabled clearable></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model.trim="userForm.name" clearable></el-input>
        </el-form-item>
        <el-form-item label="租户" prop="tenant_id">
          <el-select v-model="userForm['tenant_id']" filterable placeholder="请选择租户">
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="6" :offset="2">
        <el-form-item>
          <AvatarUpload :width="100" :height="100" :avatar="displayAvatar"
                        @finish="(avatar) => userForm.avatar = avatar"></AvatarUpload>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button @click="submitForm(userFormRef)">保存</el-button>
      <el-button @click="resetForm(userFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
</style>