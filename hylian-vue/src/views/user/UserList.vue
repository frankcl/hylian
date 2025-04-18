<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm, ElFormItem, ElIcon,
  ElPagination, ElRadio, ElRadioButton, ElRadioGroup, ElRow, ElSwitch, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import {
  asyncDeleteUser,
  asyncSearchUsers,
  asyncUpdateUser
} from '@/common/service'
import {
  fillSearchQuerySort,
  removeAfterConfirm,
  searchQueryToRequest,
  submitForm
} from '@/common/assortment'
import TenantSelect from '@/components/tenant/TenantSelect'
import UserSearch from '@/components/user/UserSearch'
import AddUser from '@/views/user/AddUser'
import AllocateRole from '@/views/user/AllocateRole'

const userStore = useUserStore()
const formRef = useTemplateRef('formRef')
const tableRef = useTemplateRef('tableRef')
const openAddDialog = ref(false)
const openAllocateDialog = ref(false)
const userId = ref()
const username = ref()
const total = ref(0)
const users = ref([])
const query = reactive({
  current: 1,
  size: 20,
  disabled: 'all',
  bind_wechat: 'all',
  register_mode: -1,
  user_id: null,
  tenant_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.user_id) request.ids = JSON.stringify([query.user_id])
  if (query.tenant_id) request.tenant_id = query.tenant_id
  if (query.disabled !== 'all') request.disabled = query.disabled
  if (query.bind_wechat !== 'all') request.bind_wechat = query.bind_wechat
  if (query.register_mode !== -1) request.register_mode = query.register_mode
  const pager = await asyncSearchUsers(request)
  total.value = pager.total
  users.value = pager.records
  users.value.forEach(user => user.prev = { disabled: user.disabled, tenant: JSON.parse(JSON.stringify(user.tenant))})
}

const remove = async id => {
  if (!await removeAfterConfirm(id, asyncDeleteUser, '删除提示', '确定删除用户信息？',
    '删除用户成功', '删除用户失败')) return
  await search()
}

const updateDisabled = async (row, disabled) => {
  if (!await submitForm(undefined, { id: row.id, disabled: disabled },
    asyncUpdateUser, '更新用户状态成功', '更新用户状态失败')) {
    row.disabled = row.prev.disabled
    return
  }
  row.prev.disabled = row.disabled
}

const updateTenant = async (row, tenant) => {
  if (!await submitForm(undefined, { id: row.id, tenant_id: tenant.id },
    asyncUpdateUser, '更新租户成功', '更新租户失败')) {
    row.tenant.id = row.prev.tenant.id
    return
  }
  row.tenant = tenant
  row.prev.tenant = JSON.parse(JSON.stringify(row.tenant))
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.disabled = row.prev.disabled
    row.tenant.id = row.prev.tenant.id
  }
}

const handleSelectAll = selection => {
  const rows = selection.length === 0 ? tableRef.value.data : selection
  rows.forEach(row => {
    row.checked = selection.length !== 0
    if (!row.checked) {
      row.disabled = row.prev.disabled
      row.tenant.id = row.prev.tenant.id
    }
  })
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row align="middle">
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>账号管理</el-breadcrumb-item>
      <el-breadcrumb-item>用户列表</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 400px">
      <el-form-item label="用户状态" prop="disabled">
        <el-radio-group v-model="query.disabled">
          <el-radio-button value="all" label="全部"></el-radio-button>
          <el-radio-button :value="false" label="启用"></el-radio-button>
          <el-radio-button :value="true" label="禁用"></el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="微信账号" prop="bind_wechat">
        <el-radio-group v-model="query.bind_wechat">
          <el-radio-button value="all" label="全部"></el-radio-button>
          <el-radio-button :value="false" label="未绑定"></el-radio-button>
          <el-radio-button :value="true" label="已绑定"></el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="注册方式" prop="register_mode">
        <el-radio-group v-model="query.register_mode">
          <el-radio :value="-1" label="全部"></el-radio>
          <el-radio :value="0" label="普通"></el-radio>
          <el-radio :value="1" label="微信"></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="租户选择" prop="tenant_id">
        <tenant-select v-model="query.tenant_id" placeholder="全部"></tenant-select>
      </el-form-item>
      <el-form-item label="用户搜索" prop="user_id">
        <el-col :span="14">
          <user-search v-model="query.user_id" placeholder="根据用户名称搜索"></user-search>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="9"><a @click="formRef.resetFields(); search()">清除所有筛选条件</a></el-col>
      </el-form-item>
    </el-form>
  </div>
  <el-table ref="tableRef" :data="users" max-height="850" table-layout="auto"
            stripe @select="handleSelect" @select-all="handleSelectAll"
            @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有用户数据</template>
    <el-table-column v-if="userStore.superAdmin" type="selection" width="55" fixed="left" />
    <el-table-column prop="username" width="150" label="用户名" show-overflow-tooltip />
    <el-table-column prop="name" width="150" label="用户名称" show-overflow-tooltip />
    <el-table-column prop="tenant.name" width="180" label="租户" show-overflow-tooltip>
      <template #default="scope">
        <tenant-select v-if="scope.row.checked" v-model="scope.row.tenant.id"
                        :clearable="false" @change="tenant => updateTenant(scope.row, tenant)"></tenant-select>
        <span v-else>{{ scope.row.tenant.name }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="disabled" width="80" label="状态">
      <template #default="scope">
        <el-switch v-if="scope.row.checked" v-model="scope.row.disabled"
                   @change="v => updateDisabled(scope.row, v)"
                   style="--el-switch-on-color: #ff4949; --el-switch-off-color: #409eff"
                   inline-prompt size="large" active-text="禁用" inactive-text="启用" />
        <div v-else>
          <span v-if="scope.row.disabled">禁用</span>
          <span v-else>启用</span>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="register_mode" width="80" label="注册方式">
      <template #default="scope">
        <span v-if="scope.row.register_mode === 0">普通</span>
        <span v-else-if="scope.row.register_mode === 1">微信</span>
        <span v-else>其他</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" width="140" prop="create_time" show-overflow-tooltip sortable="custom">
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column label="更新时间" width="140" prop="update_time" show-overflow-tooltip sortable="custom">
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column label="操作" fixed="right">
      <template #header>
        <el-button v-if="userStore.superAdmin" @click="openAddDialog = true">新增用户</el-button>
      </template>
      <template #default="scope">
        <RouterLink :to="{ name: 'ActivityList', query: { userId: scope.row.id } }">活跃记录</RouterLink>&nbsp;
        <a @click="userId = scope.row.id; username = scope.row.username; openAllocateDialog = true">角色分配</a>&nbsp;
        <a v-if="userStore.superAdmin" @click="remove(scope.row.id)">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination layout="prev, pager, next" :total="total" background
                   v-model:page-size="query.size" v-model:current-page="query.current"/>
  </el-row>
  <add-user v-model="openAddDialog" @close="search()"></add-user>
  <allocate-role v-model="openAllocateDialog" :id="userId" :username="username" @close="search()"></allocate-role>
</template>

<style scoped>
</style>