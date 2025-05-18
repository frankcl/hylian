<script setup>
import { IconArrowBackUp, IconRefresh, IconPlus } from '@tabler/icons-vue'
import { reactive, useTemplateRef } from 'vue'
import {
  ElButton, ElDialog, ElForm, ElFormItem, ElInput
} from 'element-plus'
import { useUserStore } from '@/store'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import { asyncAddApp, asyncAppSecret } from '@/common/AsyncRequest'
import HylianCard from '@/components/data/Card'
import { appFormRules } from '@/views/app/common'

const open = defineModel()
const emits = defineEmits(['close'])
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const app = reactive({
  name: null,
  secret: null,
  description: null
})

const refreshAppSecret = async () => app.secret = await asyncAppSecret()

const add = async () => {
  if (!await formRef.value.validate(valid => valid)) return
  if (!await asyncAddApp(app)) {
    showMessage('新增应用失败', ERROR)
    return
  }
  showMessage('新增应用成功', SUCCESS)
  open.value = false
}
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" align-center show-close>
    <hylian-card title="新增应用">
      <el-form ref="form" :model="app" :rules="appFormRules" label-width="80px" label-position="top">
        <el-form-item label="应用名" prop="name">
          <el-input v-model="app.name" clearable />
        </el-form-item>
        <el-form-item label="应用秘钥" prop="secret">
          <div class="d-flex flex-grow-1">
            <el-input class="mr-4" v-model="app.secret" clearable />
            <el-button type="primary" plain @click="refreshAppSecret">
              <IconRefresh size="20" class="mr-2" />
              <span>刷新</span>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input v-model="app.description" type="textarea" rows="5" placeholder="请输入应用描述" />
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