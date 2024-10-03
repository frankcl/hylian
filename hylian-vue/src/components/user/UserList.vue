<script setup>
import { format } from 'date-fns'
import {onMounted, reactive, ref, useTemplateRef, watch} from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog, ElForm, ElFormItem,
  ElIcon, ElInput, ElMessageBox,
  ElNotification, ElOption,
  ElPagination,
  ElRow, ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { remoteDeleteUser, remoteSearchTenant, remoteSearchUser } from '@/utils/hylian-service'
import { resetForm } from '@/utils/hylian'
import AddUser from '@/components/user/AddUser'
import EditUser from '@/components/user/EditUser'

const searchFormRef = useTemplateRef('searchFormRef')
const addUserVisible = ref(false)
const editUserVisible = ref(false)
const editUserId = ref()
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const users = ref([])
const tenants = ref([])
const orderField = reactive({
  field: null,
  order: null
})
const searchForm = reactive({
  user_name : '',
  name: '',
  tenant: ''
})

const searchUser = async () => {
  const searchRequest = {
    current: currentPage.value,
    size: pageSize.value
  }
  const orderArray = []
  if (orderField.field && orderField.order) {
    orderArray.push({ field: orderField.field, asc: orderField.order === 'ascending'})
  }
  searchRequest.order_by = orderArray
  if (searchForm['user_name'] !== '') searchRequest['user_name'] = searchForm['user_name']
  if (searchForm.name !== '') searchRequest.name = searchForm.name
  if (searchForm.tenant !== '') searchRequest['tenant_id'] = searchForm.tenant
  const pager = await remoteSearchUser(searchRequest)
  if (!pager) return
  users.value = pager.records
  total.value = pager.total
}

const deleteUser = async (id) => {
  ElMessageBox.confirm(
    '确定删除用户信息？',
    '删除提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    if (!await remoteDeleteUser(id)) {
      ElNotification.error('删除用户失败')
      return
    }
    ElNotification.success('删除用户成功')
    await searchUser()
  })
}

const openEditUserDialog = (id) => {
  editUserId.value = id
  editUserVisible.value = true
}

const closeEditUserDialog = async () => {
  editUserVisible.value = false
  await searchUser()
}

const closeAddUserDialog = async () => {
  addUserVisible.value = false
  await searchUser()
}

const userSortChange = (event) => {
  if (!event || !event.prop) return
  orderField.field = event.prop
  orderField.order = event.order
}

watch([currentPage, pageSize, orderField], () => searchUser(), { immediate: true })
onMounted(async () => {
  const pager = await remoteSearchTenant({ size: 1000 })
  if (pager) tenants.value = pager.records
})
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>用户</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addUserVisible = true">添加用户</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addUserVisible" align-center show-close>
    <AddUser @close="closeAddUserDialog"></AddUser>
  </el-dialog>
  <el-dialog v-model="editUserVisible" align-center show-close>
    <EditUser :id="editUserId" @close="closeEditUserDialog"></EditUser>
  </el-dialog>
  <el-form :inline="true" :model="searchForm" ref="searchFormRef" style="margin-top: 20px;">
    <el-form-item label="用户名" prop="user_name">
      <el-input v-model="searchForm['user_name']" clearable></el-input>
    </el-form-item>
    <el-form-item label="真实姓名" prop="name">
      <el-input v-model="searchForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item label="所属租户" prop="tenant">
      <el-select v-model="searchForm.tenant" filterable placeholder="请选择租户" clearable style="width: 180px;">
        <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button @click="searchUser">搜索</el-button>
      <el-button @click="resetForm(searchFormRef); searchUser()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="user-list" :data="users" max-height="500" table-layout="auto"
            stripe @sort-change="userSortChange">
    <template #empty>没有用户数据</template>
    <el-table-column prop="user_name" label="用户名" />
    <el-table-column prop="name" label="真实姓名" />
    <el-table-column prop="tenant.name" label="所属租户" />
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
        <el-button @click="openEditUserDialog(scope.row.id)">编辑</el-button>
        <el-button @click="deleteUser(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="pageSize" v-model:current-page="currentPage"/>
  </el-row>
</template>

<style scoped>
.user-list {
  width: 100%;
  margin-top: 20px;
}
</style>