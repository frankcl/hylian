<script setup>
import { reactive, useTemplateRef } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElRow } from 'element-plus'
import { useUserStore } from '@/store'
import { asyncChangePassword } from '@/common/service'
import { submitForm } from '@/common/assortment'
import { baseRules } from '@/views/user/common'

const model = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('formRef')
const passwordForm = reactive({
  id: userStore.id,
  password: '',
  new_password: '',
  confirm_password: ''
})
const formRules = {
  ... baseRules,
  ... {
    confirm_password: [
      { required: true, trigger: 'change', validator: (rule, value, callback) => {
        if (!value || value === '') callback(new Error('请输入确认密码'))
        else if (value !== passwordForm.new_password) callback(new Error('确认密码与密码不一致'))
        else callback()
      } }
    ]
  }
}

const submit = async formEl => {
  if (!await submitForm(formEl, passwordForm, asyncChangePassword,
    '修改密码成功', '修改密码失败')) return
  model.value = false
}
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="450" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>修改密码</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-form ref="formRef" label-width="auto" label-position="right" :model="passwordForm" :rules="formRules">
      <el-form-item label="原始密码" prop="password">
        <el-input type="password" v-model="passwordForm.password" clearable show-password></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="new_password">
        <el-input type="password" v-model="passwordForm.new_password" clearable show-password></el-input>
      </el-form-item>
      <el-form-item label="重复密码" prop="confirm_password">
        <el-input type="password" v-model="passwordForm.confirm_password" clearable show-password></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submit(formRef)">修改密码</el-button>
        <el-button @click="formRef.resetFields()">重置</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<style scoped>

</style>