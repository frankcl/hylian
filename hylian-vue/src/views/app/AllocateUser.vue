<script setup>
import { ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import {
  asyncBatchUpdateAppUsers,
  asyncGetAppUsers
} from '@/common/service'
import {
  fetchAllUsers,
  submitForm
} from '@/common/assortment'

const model = defineModel()
const props = defineProps(['appId', 'appName'])
const emits = defineEmits(['close'])
const appUsers = ref([])
const users = ref([])

const search = (query, user) => user.label.includes(query)

const save = async () => {
  const request = {
    app_id: props.appId,
    user_ids: []
  }
  appUsers.value.forEach(id => request.user_ids.push(id))
  if (!await submitForm(undefined, request, asyncBatchUpdateAppUsers,
    '分配管理员成功', '分配管理员失败')) return
  model.value = false
}

watch(() => props.appId, async() => {
  if (props.appId) {
    users.value.splice(0, users.value.length)
    appUsers.value.splice(0, appUsers.value.length)
    const tempAppUsers = await asyncGetAppUsers(props.appId)
    tempAppUsers.forEach(user => appUsers.value.push(user.id))
    const tempUsers = await fetchAllUsers()
    tempUsers.forEach(user => users.value.push({ label: user.name, key: user.id }))
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="650" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>应用管理员（当前应用：{{ appName }}）</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-row justify="start">
      <el-transfer v-model="appUsers" :data="users" filterable
                   filter-placeholder="根据用户名称搜索" :filter-method="search" :titles="['普通用户', '应用管理员']"
                   :button-texts="['撤销', '选取']"></el-transfer>
    </el-row>
    <el-row align="middle">
      <el-button @click="save">保存</el-button>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>