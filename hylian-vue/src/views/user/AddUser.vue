<script setup>
import { IconArrowBackUp, IconPlus } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm,
  ElFormItem, ElInput, ElRow, ElSwitch
} from 'element-plus'
import { useUserStore } from '@/store'
import { asyncAddUser } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { appFormRules } from '@/views/user/common'
import AvatarUpload from '@/components/user/AvatarUpload'
import TenantSelect from '@/components/tenant/TenantSelect'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const user = reactive({ disabled: false })
const formRules = {
  ... appFormRules,
  confirm_password: [{
    required: true, trigger: 'change', validator: (rule, value, callback) => {
      if (!value || value === '') callback(new Error('请输入确认密码'))
      else if (value !== user.password) callback(new Error('确认密码与密码不一致'))
      else callback()
    }
  }]
}

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddUser(user)) {
    showMessage('新增用户失败', ERROR)
    return
  }
  showMessage('新增用户成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="新增用户">
      <el-form ref="form" :model="user" :rules="formRules" label-width="80px" label-position="top">
        <el-row :gutter="50">
          <el-col :span="18">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="user.username" clearable />
            </el-form-item>
            <el-form-item label="用户昵称" prop="name">
              <el-input v-model="user.name" clearable />
            </el-form-item>
            <el-form-item label="所属租户" prop="tenant_id">
              <tenant-select v-model="user.tenant_id" placeholder="请选择" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="user.password" show-password clearable />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm_password">
              <el-input type="password" v-model="user.confirm_password" show-password clearable />
            </el-form-item>
            <el-form-item label="用户状态" prop="disabled">
              <el-switch v-model="user.disabled" inline-prompt size="large" active-text="禁用" inactive-text="启用"
                         style="--el-switch-on-color: #ff4949; --el-switch-off-color: #409eff" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="add" :disabled="!userStore.superAdmin">
                <IconPlus size="20" class="mr-1" />
                <span>新增</span>
              </el-button>
              <el-button type="info" @click="formRef.resetFields()">
                <IconArrowBackUp size="20" class="mr-1" />
                <span>重置</span>
              </el-button>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="用户头像" prop="avatar">
              <avatar-upload v-model="user.avatar" :size="150" @remove="user.avatar = null"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>
</style>