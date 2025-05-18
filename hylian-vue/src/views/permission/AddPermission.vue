<script setup>
import { IconArrowBackUp, IconPlus } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import AppSelect from '@/components/app/AppSelect'
import { asyncAddPermission } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('form')
const permission = reactive({})
const formRules = reactive({
  name: [{ required: true, message: '请输入权限名称', trigger: 'change' }],
  path: [{ required: true, message: '请输入资源路径', trigger: 'change' }],
  app_id: [{ required: true, message: '请选择应用', trigger: 'change' }]
})

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddPermission(permission)) {
    showMessage('新增权限失败', ERROR)
    return
  }
  showMessage('新增权限成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="新增权限">
      <el-form ref="form" :model="permission" :rules="formRules" label-width="80px" label-position="top">
        <el-form-item label="所属应用" prop="app_id">
          <app-select v-model="permission.app_id" placeholder="请选择" :clearable="false" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permission.name" clearable />
        </el-form-item>
        <el-form-item label="资源路径" prop="path">
          <el-input v-model="permission.path" clearable />
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