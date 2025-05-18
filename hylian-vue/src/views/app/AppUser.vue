<script setup>
import { IconEdit } from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { ElButton, ElDialog, ElRow, ElTransfer } from 'element-plus'
import { asyncBatchUpdateAppUser, asyncGetAppUsers, asyncGetUsers } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import HylianCard from '@/components/data/Card'

const open = defineModel()
const props = defineProps(['id', 'name'])
const emits = defineEmits(['close'])
const appUsers = ref([])
const users = ref([])

const search = (query, user) => user.label.includes(query)

const batchUpdate = async () => {
  const request = {
    app_id: props.id,
    user_ids: []
  }
  appUsers.value.forEach(id => request.user_ids.push(id))
  if (!await asyncBatchUpdateAppUser(request)) {
    showMessage('更新应用管理员失败', ERROR)
    return
  }
  showMessage('更新应用管理员成功', SUCCESS)
  emits('close')
  open.value = false
}

watch(() => props.id, async() => {
  if (props.id) {
    users.value.splice(0, users.value.length)
    appUsers.value.splice(0, appUsers.value.length)
    const tempAppUsers = await asyncGetAppUsers(props.id)
    tempAppUsers.forEach(user => appUsers.value.push(user.id))
    const tempUsers = await asyncGetUsers()
    tempUsers.forEach(user => users.value.push({ label: user.name, key: user.id }))
  }
}, { immediate: true })
</script>

<template>
  <el-dialog v-model="open" align-center show-close>
    <hylian-card :title="`应用管理员：${name}`">
      <el-transfer v-model="appUsers" :data="users" filterable filter-placeholder="根据用户名搜索"
                   :filter-method="search" :titles="['普通用户', '应用管理员']"
                   :button-texts="['撤销', '选取']" />
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