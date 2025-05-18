<script setup>
import { IconArrowBackUp, IconPlus } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddRole } from '@/common/AsyncRequest'
import AppSelect from '@/components/app/AppSelect'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('form')
const role = reactive({})
const formRules = reactive({
  name: [{ required: true, message: '请输入角色名称', trigger: 'change' }],
  app_id: [{ required: true, message: '请选择应用', trigger: 'change' }]
})

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddRole(role)) {
    showMessage('新增角色失败', ERROR)
    return
  }
  showMessage('新增角色成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="新增角色">
      <el-form ref="form" :model="role" :rules="formRules" label-width="80px" label-position="top">
        <el-form-item label="所属应用" prop="app_id">
          <app-select v-model="role.app_id" placeholder="请选择" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model.trim="role.name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="add">
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