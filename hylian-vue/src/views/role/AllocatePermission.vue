<script setup>
import { ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElBreadcrumb, ElBreadcrumbItem, ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import { asyncBatchUpdateRolePermissions, asyncGetRolePermissions } from '@/common/service'
import { fetchAppPermissions, submitForm } from '@/common/assortment'

const model = defineModel()
const props = defineProps(['roleId', 'roleName', 'appId'])
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
    '分配角色权限成功', '分配角色权限失败')) return
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
  <el-dialog v-model="model" @close="emits('close')" align-center show-close>
    <el-row>
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>授权管理</el-breadcrumb-item>
        <el-breadcrumb-item>权限分配（当前角色：{{ roleName }}）</el-breadcrumb-item>
      </el-breadcrumb>
    </el-row>
    <el-row justify="start">
      <el-transfer v-model="permissions" :data="appPermissions" filterable filter-placeholder="根据权限名搜索"
                   :filter-method="search" :titles="['未分配权限', '已分配权限']"
                   :button-texts="['撤销', '选取']"></el-transfer>
    </el-row>
    <el-row align="middle">
      <el-button @click="save">保存</el-button>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>