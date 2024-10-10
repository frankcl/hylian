<script setup>
import { onMounted, ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElOption, ElRow, ElSelect, ElTransfer } from 'element-plus'
import { asyncBatchUpdateUserRoles, asyncGetAppUserRoles } from '@/common/service'
import { fetchAllApps, fetchAppRoles, submitForm } from '@/common/assortment'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const appId = ref()
const apps = ref([])
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
    '修改用户角色成功', '修改用户角色失败')) return
  emits('close')
}

watch(() => [props.id, appId.value], async() => {
  if (appId.value && props.id) {
    appRoles.value.splice(0, appRoles.value.length)
    roles.value.splice(0, roles.value.length)
    const tempRoles = await asyncGetAppUserRoles({user_id: props.id, app_id: appId.value})
    tempRoles.forEach(role => roles.value.push(role.id))
    const tempAppRoles = await fetchAppRoles(appId.value)
    tempAppRoles.forEach(role => appRoles.value.push({label: role.name, key: role.id}))
  }
})
onMounted(async () => apps.value = await fetchAllApps())
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>账号管理</el-breadcrumb-item>
    <el-breadcrumb-item>用户列表</el-breadcrumb-item>
    <el-breadcrumb-item>角色分配</el-breadcrumb-item>
  </el-breadcrumb>
  <el-row style="margin-top: 20px;" align="middle">
    <el-col :span="2">
      <span>请选择应用</span>
    </el-col>
    <el-col :span="6">
      <el-select v-model="appId" filterable placeholder="请选择应用">
        <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
      </el-select>
    </el-col>
  </el-row>
  <el-transfer v-if="appId" style="margin-top: 20px;" v-model="roles" :data="appRoles" filterable
               filter-placeholder="根据角色名搜索" :filter-method="search" :titles="['未分配角色', '已分配角色']"
               :button-texts="['撤销', '选取']">
    <template #left-footer>&nbsp;</template>
    <template #right-footer>
      <el-row justify="center" align="middle" style="height: 100%">
        <el-button @click="save">保存</el-button>
      </el-row>
    </template>
  </el-transfer>
</template>

<style scoped>

</style>