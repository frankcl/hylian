<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight, Refresh } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol,
  ElDialog, ElForm, ElFormItem, ElIcon, ElInput, ElRow
} from 'element-plus'
import {
  asyncAddApp,
  asyncRandomSecret
} from '@/common/service'
import { submitForm } from '@/common/assortment'

const model = defineModel()
const emits = defineEmits(['close'])
const formRef = useTemplateRef('formRef')
const appForm = reactive({
  name: null,
  secret: null,
  description: null
})
const formRules = reactive({
  name: [
    { required: true, message: '请输入应用名', trigger: 'change' }
  ],
  secret: [
    { required: true, message: '请输入应用秘钥', trigger: 'change' },
    { min: 8, message: '应用秘钥至少8位', trigger: 'change' }
  ]
})

const refreshAppSecret = async appForm => appForm.secret = await asyncRandomSecret()

const submit = async formEl => {
  if (!await submitForm(formEl, appForm, asyncAddApp,
    '新增应用成功', '新增应用失败')) return
  emits('close')
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="480" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>应用管理</el-breadcrumb-item>
        <el-breadcrumb-item>新增应用</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" :model="appForm" :rules="formRules"
             label-width="auto" label-position="right">
      <el-form-item label="应用名" prop="name">
        <el-input v-model.trim="appForm.name" clearable></el-input>
      </el-form-item>
      <el-form-item label="应用秘钥" prop="secret">
        <el-col :span="18">
          <el-input v-model.trim="appForm.secret" clearable></el-input>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="5">
          <el-row justify="end">
            <el-button @click="refreshAppSecret(appForm)">
              刷新&nbsp;
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-row>
        </el-col>
      </el-form-item>
      <el-form-item label="应用描述" prop="description">
        <el-input v-model="appForm.description" type="textarea" rows="5" placeholder="请输入应用描述"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submit(formRef)">新增</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>
</style>