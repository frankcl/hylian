<script setup>
import {
  IconActivity, IconBrandWechat, IconCircles, IconClearAll, IconClock,
  IconPlus, IconCircleCheck, IconCircleX, IconTrash, IconUserPlus
} from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElAvatar, ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider, ElForm,
  ElFormItem, ElPagination, ElRadioButton, ElRadioGroup, ElRow, ElSwitch, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveUser,
  asyncSearchUser,
  asyncUpdateUser,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import TenantSelect from '@/components/tenant/TenantSelect'
import UserSearch from '@/components/user/UserSearch'
import AddUser from '@/views/user/AddUser'
import UserRole from '@/views/user/UserRole'
import HylianCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import ImageAvatar from '@/assets/avatar.jpg'

const router = useRouter()
const userStore = useUserStore()
const formRef = useTemplateRef('form')
const tableRef = useTemplateRef('table')
const openAdd = ref(false)
const openUserRole = ref(false)
const user = reactive({})
const total = ref(0)
const users = ref([])
const query = reactive(newSearchQuery({
  disabled: 'all',
  bind_wechat: 'all',
  register_mode: -1
}))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.user_id) request.ids = JSON.stringify([query.user_id])
  if (query.tenant_id) request.tenant_id = query.tenant_id
  if (query.disabled !== 'all') request.disabled = query.disabled
  if (query.bind_wechat !== 'all') request.bind_wechat = query.bind_wechat
  if (query.register_mode !== -1) request.register_mode = query.register_mode
  const pager = await asyncSearchUser(request)
  total.value = pager.total
  users.value = pager.records
  users.value.forEach(user => user.prev = { disabled: user.disabled, tenant: JSON.parse(JSON.stringify(user.tenant))})
}

const add = () => openAdd.value = true
const activity = async id => await router.push({ path: '/workbench/activityList', query: { user_id: id } })
const role = row => {
  user.id = row.id
  user.name = row.name
  openUserRole.value = true
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveUser, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除用户失败', ERROR)
    return
  }
  showMessage('删除用户成功', SUCCESS)
  await search()
}

const updateDisabled = async (user, disabled) => {
  if (!await asyncUpdateUser({ id: user.id, disabled: disabled })) {
    showMessage('更新状态失败', ERROR)
    user.disabled = user.prev.disabled
    return
  }
  showMessage('更新状态成功', SUCCESS)
  user.prev.disabled = user.disabled
}

const updateTenant = async (user, tenant) => {
  if (!await asyncUpdateUser({ id: user.id, tenant_id: tenant.id })) {
    showMessage('更新所属租户失败', ERROR)
    user.tenant.id = user.prev.tenant.id
    return
  }
  showMessage('更新所属租户成功', SUCCESS)
  user.tenant = tenant
  user.prev.tenant = JSON.parse(JSON.stringify(user.tenant))
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.disabled = row.prev.disabled
    row.tenant.id = row.prev.tenant.id
  }
}

const handleSelectAll = selection => {
  const rows = tableRef.value.data
  rows.forEach(row => handleSelect(selection, row))
}

watchEffect(async () => await search())
</script>

<template>
  <hylian-card>
    <template #title>
      <el-breadcrumb>
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>用户管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-form-item label="用户状态" prop="disabled">
        <el-radio-group v-model="query.disabled">
          <el-radio-button value="all" label="全部" />
          <el-radio-button :value="false" label="启用" />
          <el-radio-button :value="true" label="禁用" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="注册方式" prop="register_mode">
        <el-radio-group v-model="query.register_mode">
          <el-radio-button :value="-1" label="全部" />
          <el-radio-button :value="0" label="普通" />
          <el-radio-button :value="1" label="微信" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="微信账号" prop="bind_wechat">
        <el-radio-group v-model="query.bind_wechat">
          <el-radio-button value="all" label="全部" />
          <el-radio-button :value="false" label="未绑定" />
          <el-radio-button :value="true" label="已绑定" />
        </el-radio-group>
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属租户" prop="tenant_id">
            <tenant-select v-model="query.tenant_id" placeholder="全部" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户搜索" prop="user_id">
            <user-search v-model="query.user_id" placeholder="根据用户昵称搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-button type="primary" plain @click="formRef.resetFields()">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="用户列表">
      <template #right>
        <el-button type="primary" @click="add" :disabled="!userStore.superAdmin">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="table" :data="users" max-height="650" table-layout="auto" stripe
              @select="handleSelect" @select-all="handleSelectAll" class="mb-4"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>暂无用户数据</template>
      <el-table-column v-if="userStore.superAdmin" type="selection" width="55" fixed="left" />
      <el-table-column prop="username" label="用户名" show-overflow-tooltip />
      <el-table-column prop="name" label="用户昵称" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <el-avatar shape="circle" fit="cover" size="small" class="flex-shrink-0 mr-2"
                       :src="scope.row.avatar ? scope.row.avatar : ImageAvatar" />
            <span>{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="tenant.name" label="所属租户" show-overflow-tooltip>
        <template #default="scope">
          <tenant-select v-if="scope.row.checked" v-model="scope.row.tenant.id" :clearable="false"
                         @change="tenant => updateTenant(scope.row, tenant)" />
          <span v-else>{{ scope.row.tenant.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="disabled" label="状态">
        <template #default="scope">
          <el-switch v-if="scope.row.checked" v-model="scope.row.disabled" @change="v => updateDisabled(scope.row, v)"
                     style="--el-switch-on-color: #ff4949; --el-switch-off-color: #409eff"
                     inline-prompt size="large" active-text="禁用" inactive-text="启用" />
          <div v-else>
            <div v-if="scope.row.disabled" class="d-flex align-items-center">
              <IconCircleX size="20" color="#f56c6c" class="mr-1" />
              <span>禁用</span>
            </div>
            <div v-else class="d-flex align-items-center">
              <IconCircleCheck size="20" color="#95D475" class="mr-1" />
              <span>启用</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="register_mode" label="注册方式">
        <template #default="scope">
          <div v-if="scope.row.register_mode === 0" class="d-flex align-items-center">
            <IconUserPlus size="20" color="#409EFF" class="mr-1" />
            <span>普通</span>
          </div>
          <div v-if="scope.row.register_mode === 1" class="d-flex align-items-center">
            <IconBrandWechat size="20" color="#95D475" class="mr-1" />
            <span>微信</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" show-overflow-tooltip sortable="custom">
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="scope">
          <el-button type="primary" @click="activity(scope.row.id)">
            <IconActivity size="20" class="mr-2" />
            <span>活动记录</span>
          </el-button>
          <el-button type="primary" plain @click="role(scope.row)">
            <IconCircles size="20" class="mr-2" />
            <span>角色</span>
          </el-button>
          <el-button type="danger" :disabled="!userStore.superAdmin" @click="remove(scope.row.id)">
            <IconTrash size="20" class="mr-2" />
            <span>删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-config-provider :locale="zhCn">
        <el-pagination layout="total, prev, pager, next, jumper" :total="total" background
                       v-model:page-size="query.page_size" v-model:current-page="query.page_num" />
      </el-config-provider>
    </el-row>
  </hylian-card>
  <add-user v-model="openAdd" @close="search" />
  <user-role v-model="openUserRole" v-bind="user" />
</template>

<style scoped>
</style>