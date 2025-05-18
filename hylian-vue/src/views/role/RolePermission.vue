<script setup>
import { IconEdit } from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import {
  asyncBatchUpdateRolePermission,
  asyncGetAppPermissions,
  asyncGetRolePermission
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const props = defineProps(['roleId', 'roleName', 'appId'])
const emits = defineEmits(['close'])
const appPermissions = ref([])
const permissions = ref([])

const search = (query, permission) => permission.label.includes(query)

const batchUpdate = async () => {
  const request = {
    app_id: props.appId,
    role_id: props.roleId,
    permission_ids: []
  }
  permissions.value.forEach(id => request.permission_ids.push(id))
  if (!await asyncBatchUpdateRolePermission(request)) {
    showMessage('更新角色权限失败', ERROR)
    return
  }
  showMessage('更新角色权限成功', SUCCESS)
  emits('close')
  open.value = false
}

watch(() => [props.appId, props.roleId], async() => {
  if (props.appId && props.roleId) {
    permissions.value.splice(0, permissions.value.length)
    appPermissions.value.splice(0, appPermissions.value.length)
    const tempPermissions = await asyncGetRolePermission(props.roleId)
    tempPermissions.forEach(permission => permissions.value.push(permission.id))
    const tempAppPermissions = await asyncGetAppPermissions(props.appId)
    tempAppPermissions.forEach(permission => appPermissions.value.push({label: permission.name, key: permission.id}))
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <hylian-card :title="`角色权限分配：${roleName}`">
      <el-transfer v-model="permissions" :data="appPermissions" filterable
                   filter-placeholder="根据权限名搜索" :filter-method="search"
                   :titles="['未分配权限', '已分配权限']" :button-texts="['撤销', '选取']" />
      <el-row align="middle" class="mt-4">
        <el-button type="primary" @click="batchUpdate">
          <IconEdit size="20" class="mr-1" />
          <span>批量更新</span>
        </el-button>
      </el-row>
    </hylian-card>
  </el-dialog>
</template>

<style scoped>

</style>