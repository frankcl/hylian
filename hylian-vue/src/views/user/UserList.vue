<script setup>
import { format } from 'date-fns'
import { onMounted, reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElForm, ElFormItem,
  ElIcon, ElInput, ElOption, ElPagination, ElRow, ElSelect, ElTable, ElTableColumn
} from 'element-plus'
import { asyncDeleteUser, asyncSearchUsers } from '@/common/service'
import { confirmAndRemove, fetchAllTenants, fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import AddUser from '@/views/user/AddUser'
import EditUser from '@/views/user/EditUser'
import UserRoles from '@/views/user/UserRoles'

const formRef = useTemplateRef('formRef')
const addDialog = ref(false)
const editDialog = ref(false)
const allocateDialog = ref(false)
const userId = ref()
const total = ref(0)
const users = ref([])
const tenants = ref([])
const query = reactive({
  current: 1,
  size: 20,
  sort_field: null,
  sort_order: null
})
const formData = reactive({
  username : '',
  name: '',
  tenant_id: ''
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (formData.username) request.username = formData.username
  if (formData.name) request.name = formData.name
  if (formData.tenant_id) request.tenant_id = formData.tenant_id
  const pager = await asyncSearchUsers(request)
  total.value = pager.total
  users.value = pager.records
}

const remove = async id => {
  if (!await confirmAndRemove(id, asyncDeleteUser, '删除提示', '确定删除用户信息？',
    '删除用户成功', '删除用户失败')) return
  await search()
}

const openEditDialog = id => {
  userId.value = id
  editDialog.value = true
}

const openAllocateDialog = id => {
  userId.value = id
  allocateDialog.value = true
}

const closeEditDialog = async () => {
  editDialog.value = false
  await search()
}

const closeAllocateDialog = async () => {
  allocateDialog.value = false
  await search()
}

const closeAddDialog = async () => {
  addDialog.value = false
  await search()
}

watch(query, () => search(), { immediate: true })
onMounted(async () => tenants.value = await fetchAllTenants())
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>用户列表</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addDialog = true">添加用户</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addDialog" align-center show-close>
    <AddUser @close="closeAddDialog"></AddUser>
  </el-dialog>
  <el-dialog v-model="editDialog" align-center show-close>
    <EditUser :id="userId" @close="closeEditDialog"></EditUser>
  </el-dialog>
  <el-dialog v-model="allocateDialog" align-center show-close>
    <UserRoles :id="userId" @close="closeAllocateDialog"></UserRoles>
  </el-dialog>
  <el-form :inline="true" :model="formData" ref="formRef" style="margin-top: 20px;">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="formData.username" clearable></el-input>
    </el-form-item>
    <el-form-item label="真实姓名" prop="name">
      <el-input v-model="formData.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="所属租户" prop="tenant_id">
      <el-select v-model="formData.tenant_id" filterable placeholder="请选择租户" clearable style="width: 180px;">
        <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button @click="search">搜索</el-button>
      <el-button @click="formRef.resetFields(); search()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="user-list" :data="users" max-height="500" table-layout="auto"
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有用户数据</template>
    <el-table-column prop="username" label="用户名" />
    <el-table-column prop="name" label="真实姓名" />
    <el-table-column prop="tenant.name" label="所属租户" />
    <el-table-column prop="disabled" label="用户状态" sortable="custom">
      <template #default="scope">
        <span v-if="scope.row.disabled">禁用</span>
        <span v-else>启用</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" prop="create_time" sortable="custom">
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column label="更新时间" prop="update_time" sortable="custom">
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column fixed="right" label="操作">
      <template #default="scope">
        <el-button @click="openEditDialog(scope.row.id)">编辑</el-button>
        <el-button @click="openAllocateDialog(scope.row.id)">角色分配</el-button>
        <el-button @click="remove(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current"/>
  </el-row>
</template>

<style scoped>
.user-list {
  width: 100%;
  margin-top: 20px;
}
</style>