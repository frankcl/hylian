<script setup>
import { IconArrowBackUp, IconEdit, IconHelp } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElTooltip } from 'element-plus'
import { useUserStore } from '@/store'
import { asyncUpdatePassword } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const passwordForm = reactive({ id: userStore.id })
const formRules = {
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
  confirm_password: [{
    required: true, trigger: 'change', validator: (rule, value, callback) => {
      if (!value || value === '') callback(new Error('请输入确认密码'))
      else if (value !== passwordForm.new_password) callback(new Error('确认密码与密码不一致'))
      else callback()
    }
  }]
}

const update = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncUpdatePassword(passwordForm)) {
    showMessage('修改密码失败', ERROR)
    return
  }
  showMessage('修改密码成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="修改密码">
      <el-form ref="form" label-width="100px" label-position="top" :model="passwordForm" :rules="formRules">
        <el-form-item label="原始密码" prop="password">
          <template #label>
            <span class="d-flex-inline align-items-center">
              <span>原始密码</span>
              <el-tooltip effect="dark" placement="top" content="系统初始密码：123456">
                <IconHelp size="12" class="ml-2" />
              </el-tooltip>
            </span>
          </template>
          <el-input type="password" v-model="passwordForm.password" clearable show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="new_password">
          <el-input type="password" v-model="passwordForm.new_password" clearable show-password />
        </el-form-item>
        <el-form-item label="重复密码" prop="confirm_password">
          <el-input type="password" v-model="passwordForm.confirm_password" clearable show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="update">
            <IconEdit size="20" class="mr-1" />
            <span>修改</span>
          </el-button>
          <el-button type="info" @click="formRef.resetFields()">
            <IconArrowBackUp size="20" class="mr-1" />
            <span>重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>

</style>