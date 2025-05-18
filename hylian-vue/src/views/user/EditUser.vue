<script setup>
import { IconArrowBackUp, IconEdit } from '@tabler/icons-vue'
import { ref, useTemplateRef, watch } from 'vue'
import {
  ElButton, ElCol, ElDialog, ElForm, ElFormItem, ElInput, ElRow
} from 'element-plus'
import {
  asyncForceRefresh,
  asyncGetUser,
  asyncRefreshUser,
  asyncRemoveAvatar,
  asyncUpdateUser
} from '@/common/AsyncRequest'
import {asyncExecuteAfterConfirming, ERROR, showMessage, SUCCESS} from '@/common/Feedback'
import AvatarUpload from '@/components/user/AvatarUpload'
import TenantSelect from '@/components/tenant/TenantSelect'
import { appFormRules } from '@/views/user/common'
import HylianCard from '@/components/data/Card'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const open = defineModel()
const formRef = useTemplateRef('form')
const user = ref({})

const reset = async () => {
  if (props.id) {
    user.value = await asyncGetUser(props.id)
    user.value.tenant_id = user.value.tenant.id
  }
}

const update = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncUpdateUser(user.value)) {
    showMessage('编辑用户失败', ERROR)
    return
  }
  showMessage('编辑用户成功', SUCCESS)
  await asyncForceRefresh()
  await asyncRefreshUser()
  open.value = false
}

const removeAvatar = async () => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveAvatar)
  if (success === undefined) return
  if (!success) {
    showMessage('删除用户头像失败', ERROR)
    return
  }
  showMessage('删除用户头像成功', SUCCESS)
  user.value.avatar = null
  await asyncForceRefresh()
  await asyncRefreshUser()
}

watch(() => [props.id, open.value], async () => {
  if (open.value) await reset()
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="编辑用户">
      <el-form ref="form" :model="user" :rules="appFormRules" label-width="80px" label-position="top">
        <el-row :gutter="50">
          <el-col :span="18">
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="user.username" clearable></el-input>
            </el-form-item>
            <el-form-item label="用户昵称" prop="name">
              <el-input v-model.trim="user.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属租户" prop="tenant_id">
              <tenant-select v-model="user.tenant_id" placeholder="请选择" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="update">
                <IconEdit size="20" class="mr-1" />
                <span>编辑</span>
              </el-button>
              <el-button type="info" @click="reset">
                <IconArrowBackUp size="20" class="mr-1" />
                <span>重置</span>
              </el-button>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="用户头像" prop="avatar">
              <avatar-upload v-model="user.avatar" :size="150" @remove="removeAvatar" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>
</style>