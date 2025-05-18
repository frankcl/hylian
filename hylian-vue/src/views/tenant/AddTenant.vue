<script setup>
import { IconArrowBackUp, IconPlus } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddTenant } from '@/common/AsyncRequest'
import HylianCard from '@/components/data/Card'

const emits = defineEmits(['close'])
const open = defineModel()
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const tenant = reactive({})
const formRules = reactive({
  name: [{ required: true, message: '请输入租户', trigger: 'change' }]
})

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddTenant(tenant)) {
    showMessage('新增角色失败', ERROR)
    return
  }
  showMessage('新增角色成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="新增租户">
      <el-form ref="form" :model="tenant" :rules="formRules" label-width="80px" label-position="top">
        <el-form-item label="租户名" prop="name">
          <el-input v-model="tenant.name" clearable />
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
      </el-form>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>
</style>