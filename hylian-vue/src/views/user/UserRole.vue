<script setup>
import { IconEdit } from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import {
  asyncBatchUpdateUserRole,
  asyncGetAppRoles,
  asyncGetAppUserRole
} from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import AppSelect from '@/components/app/AppSelect'
import HylianCard from '@/components/data/Card'

const props = defineProps(['id', 'name'])
const emits = defineEmits(['close'])
const open = defineModel()
const appId = ref()
const appRoles = ref([])
const roles = ref([])

const search = (query, role) => role.label.includes(query)

const batchUpdate = async () => {
  const request = {
    app_id: appId.value,
    user_id: props.id,
    role_ids: []
  }
  roles.value.forEach(id => request.role_ids.push(id))
  if (!await asyncBatchUpdateUserRole(request)) {
    showMessage('更新用户角色失败', ERROR)
    return
  }
  showMessage('更新用户角色成功', SUCCESS)
  emits('close')
  open.value = false
}

watch(() => [props.id, appId.value], async() => {
  if (appId.value && props.id) {
    appRoles.value.splice(0, appRoles.value.length)
    roles.value.splice(0, roles.value.length)
    const tempRoles = await asyncGetAppUserRole({ user_id: props.id, app_id: appId.value })
    tempRoles.forEach(role => roles.value.push(role.id))
    const tempAppRoles = await asyncGetAppRoles(appId.value)
    tempAppRoles.forEach(role => appRoles.value.push({ label: role.name, key: role.id }))
  }
})
</script>

<template>
  <el-dialog v-model="open" @close="emits('close')" width="60%" align-center show-close>
    <hylian-card :title="`用户角色：${ name }`">
      <div class="d-flex align-items-center mb-4">
        <label class="mr-4 fs-14px flex-shrink-0">请选择应用</label>
        <app-select v-model="appId" placeholder="请选择" :clearable="false" />
      </div>
      <el-transfer v-if="appId" v-model="roles" :data="appRoles" filterable filter-placeholder="根据角色名搜索"
                   :filter-method="search" :titles="['未分配角色', '已分配角色']"
                   :button-texts="['撤销', '选取']" />
      <el-row v-if="appId" align="middle" class="mt-4">
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