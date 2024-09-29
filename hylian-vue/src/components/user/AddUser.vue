<script setup>
import { onMounted, ref } from 'vue'
import {
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
import { searchTenant, uploadAvatar } from '@/utils/hylian-service'

const userForm = ref({
  username: '',
  name: '',
  avtar: '',
  tenantId: '',
  password: '',
  confirmPassword: ''
})
const userRules = ref({
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'change'
    }
  ],
  name: [
    {
      required: true,
      message: '请输入真实姓名',
      trigger: 'change'
    }
  ],
  tenantId: [
    {
      required: true,
      message: '请选择租户',
      trigger: 'change'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'change'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: '请输入确认密码',
      trigger: 'change'
    }
  ],
  captcha: [
    {
      required: true,
      message: '请输入验证码',
      trigger: 'change'
    }
  ]
})

const tenants = ref([])
const avatarURL = ref('')

async function upload(file) {
  const response = await uploadAvatar(file)
  if (response) {
    avatarURL.value = response['signed_url']
    userForm.value.avtar = response['oss_url']
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

onMounted(async () => {
  const pager = await searchTenant({ size: 100 })
  if (pager) tenants.value = pager.records
})

</script>

<template>
  <el-form ref="userFormRef" :model="userForm" :rules="userRules">
    <el-row>
      <el-col :span="16">
        <el-form-item label="用户名" label-position="right" prop="username">
          <el-input v-model.trim="userForm.username" :clearable="true" ></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" label-position="right" prop="name">
          <el-input v-model.trim="userForm.name" :clearable="true" ></el-input>
        </el-form-item>
        <el-form-item label="租户" label-position="right" prop="tenantId">
          <el-select v-model="userForm.tenantId" filterable placeholder="请选择租户">
            <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
          </el-select>
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