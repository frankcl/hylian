<script setup>
import { ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElRow, ElTransfer } from 'element-plus'
import { asyncBatchUpdateRolePermissions, asyncGetRolePermissions } from '@/common/service'
import { fetchAppPermissions, submitForm } from '@/common/assortment'

const props = defineProps(['roleId', 'appId'])
const emits = defineEmits(['close'])
const appPermissions = ref([])
const permissions = ref([])

const search = (query, permission) => permission.label.includes(query)

const save = async () => {
  const request = {
    app_id: props.appId,
    role_id: props.roleId,
    permission_ids: []
  }
  permissions.value.forEach(id => request.permission_ids.push(id))
  if (!await submitForm(undefined, request, asyncBatchUpdateRolePermissions,
    '修改角色权限成功', '修改角色权限失败')) return
  emits('close')
}

watch(() => [props.appId, props.roleId], async() => {
  if (props.appId && props.roleId) {
    permissions.value.splice(0, permissions.value.length)
    appPermissions.value.splice(0, appPermissions.value.length)
    const tempPermissions = await asyncGetRolePermissions(props.roleId)
    tempPermissions.forEach(permission => permissions.value.push(permission.id))
    const tempAppPermissions = await fetchAppPermissions(props.appId)
    tempAppPermissions.forEach(permission => appPermissions.value.push({label: permission.name, key: permission.id}))
  }
}, { immediate: true })
</script>

<template>
  <el-breadcrumb :separator-icon="ArrowRight">
    <el-breadcrumb-item>授权管理</el-breadcrumb-item>
    <el-breadcrumb-item>角色列表</el-breadcrumb-item>
    <el-breadcrumb-item>权限分配</el-breadcrumb-item>
  </el-breadcrumb>
  <el-transfer style="margin-top: 20px;" v-model="permissions" :data="appPermissions" filterable
               filter-placeholder="根据权限名搜索" :filter-method="search" :titles="['未分配权限', '已分配权限']"
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