<script setup>
import { ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElRow, ElTransfer } from 'element-plus'
import { asyncBatchUpdateUserRoles, asyncGetAppUserRoles } from '@/common/service'
import { fetchAppRoles, submitForm } from '@/common/assortment'
import AppSelect from '@/components/app/AppSelect'

const props = defineProps(['id', 'username'])
const emits = defineEmits(['close'])
const model = defineModel()
const appId = ref()
const appRoles = ref([])
const roles = ref([])

const search = (query, role) => role.label.includes(query)

const save = async () => {
  const request = {
    app_id: appId.value,
    user_id: props.id,
    role_ids: []
  }
  roles.value.forEach(roleId => request.role_ids.push(roleId))
  if (!await submitForm(undefined, request, asyncBatchUpdateUserRoles,
    '分配用户角色成功', '分配用户角色失败')) return
  model.value = false
}

watch(() => [props.id, appId.value], async() => {
  if (appId.value && props.id) {
    appRoles.value.splice(0, appRoles.value.length)
    roles.value.splice(0, roles.value.length)
    const tempRoles = await asyncGetAppUserRoles({ user_id: props.id, app_id: appId.value })
    tempRoles.forEach(role => roles.value.push(role.id))
    const tempAppRoles = await fetchAppRoles(appId.value)
    tempAppRoles.forEach(role => appRoles.value.push({ label: role.name, key: role.id }))
  }
})
</script>

<template>
  <el-dialog v-model="model" @close="emits('close')" width="650" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>角色分配（当前用户：{{ username }}）</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-row align="middle">
      <el-col :span="2">
        <span>应用选择</span>
      </el-col>
      <el-col :span="6">
        <app-select v-model="appId" placeholder="请选择" :clearable="false"></app-select>
      </el-col>
    </el-row>
    <el-row justify="start">
      <el-transfer v-if="appId" v-model="roles" :data="appRoles" filterable
                   filter-placeholder="根据角色名搜索" :filter-method="search" :titles="['未分配角色', '已分配角色']"
                   :button-texts="['撤销', '选取']"></el-transfer>
    </el-row>
    <el-row v-if="appId" align="middle">
      <el-button @click="save">保存</el-button>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>