<script setup>
import {onMounted, ref, useTemplateRef} from 'vue'
import {
  ElButton,
  ElCol,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElNotification,
  ElOption,
  ElRow,
  ElSelect,
  ElUpload
} from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { addUser, searchTenant, uploadAvatar } from '@/utils/hylian-service'
import router from '@/router'

const userFormRef = useTemplateRef('userFormRef')

const userForm = ref({
  user_name: '',
  name: '',
  avatar: '',
  tenant_id: '',
  password: '',
  confirmed_password: ''
})

const validateConfirmedPassword = (rule, value, callback) => {
  if (!value || value === '') callback(new Error('请输入确认密码'))
  else if (value !== userForm.value.password) callback(new Error('确认密码与密码不一致'))
  else callback()
}

const userRules = ref({
  user_name: [
    { required: true, message: '请输入用户名', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'change' }
  ],
  tenant_id: [
    { required: true, message: '请选择租户', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
  confirmed_password: [
    { trigger: 'change', validator: validateConfirmedPassword }
  ]
})

const tenants = ref([])
const avatarURL = ref('')

async function upload(file) {
  const response = await uploadAvatar(file)
  if (response) {
    avatarURL.value = response['signed_url']
    userForm.value.avatar = response['oss_url']
  }
}

const beforeUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg') {
    ElNotification.error('头像图片必须是jpeg格式')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElNotification.error('头像图片不能超过2MB')
    return false
  }
  return true
}

const submitForm = async (formEl) => {
  if (!formEl) return
  if (!await formEl.validate((valid) => valid)) return
  if (!await addUser(userForm.value)) {
    ElNotification.error('新增用户失败')
    return
  }
  ElNotification.success('新增用户成功')
  await router.push('/workbench/userList')
}

const resetForm = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
}

onMounted(async () => {
  const pager = await searchTenant({ size: 100 })
  if (pager) tenants.value = pager.records
})

</script>

<template>
  <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="auto" label-position="right">
    <el-row>
      <el-col :span="16">
        <el-form-item label="用户名" prop="user_name">
          <el-input v-model.trim="userForm['user_name']" clearable></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model.trim="userForm.name" clearable></el-input>
        </el-form-item>
        <el-form-item label="租户" prop="tenant_id">
          <el-select v-model="userForm['tenant_id']" filterable placeholder="请选择租户">
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model.trim="userForm.password" show-password clearable></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmed_password">
          <el-input type="password" v-model.trim="userForm['confirmed_password']" show-password clearable></el-input>
        </el-form-item>
      </el-col>
      <el-col :span="6" :offset="2">
        <el-form-item>
          <el-upload :http-request="upload" :before-upload="beforeUpload" class="avatar-uploader" :show-file-list="false">
            <img v-if="avatarURL" :src="avatarURL" alt="头像" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item>
      <el-button @click.prevent="submitForm(userFormRef)">新增</el-button>
      <el-button @click.prevent="resetForm(userFormRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
}
</style>