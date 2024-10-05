<script setup>
import { format } from 'date-fns'
import {onMounted, reactive, ref, watch} from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog,
  ElIcon, ElInput, ElMessageBox,
  ElNotification, ElOption,
  ElPagination,
  ElRow, ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus'
import {remoteDeletePermission, remoteSearchApp, remoteSearchPermission} from '@/utils/hylian-service'
import AddPermission from '@/views/permission/AddPermission'
import EditPermission from '@/views/permission/EditPermission'

const permissionId = ref()
const permissions = ref([])
const apps = ref([])
const total = ref(0)
const addDialog = ref(false)
const editDialog = ref(false)
const searchQuery = reactive({
  currentPage: 1,
  pageSize: 20,
  name: null,
  app_id: null,
  sortField: null,
  sortOrder: null
})

const search = async () => {
  const searchRequest = {
    current: searchQuery.currentPage,
    size: searchQuery.pageSize
  }
  if (searchQuery.sortField && searchQuery.sortOrder) {
    searchRequest.order_by = [{ field: searchQuery.sortField, asc: searchQuery.sortOrder === 'ascending' }]
  }
  if (searchQuery.name) searchRequest.name = searchQuery.name
  if (searchQuery.app_id) searchRequest.app_id = searchQuery.app_id
  const pager = await remoteSearchPermission(searchRequest)
  if (!pager) return
  permissions.value = pager.records
  total.value = pager.total
}

const deletePermission = async (id) => {
  ElMessageBox.confirm(
    '确定删除权限信息？',
    '删除提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    if (!await remoteDeletePermission(id)) {
      ElNotification.error('删除权限失败')
      return
    }
    ElNotification.success('删除权限成功')
    await search()
  })
}

const openEditDialog = (id) => {
  permissionId.value = id
  editDialog.value = true
}

const closeEditDialog = async () => {
  editDialog.value = false
  await search()
}

const closeAddDialog = async () => {
  addDialog.value = false
  await search()
}

const permissionSortChange = (event) => {
  if (!event || !event.prop) return
  searchQuery.sortField = event.prop
  searchQuery.sortOrder = event.order
}

watch(searchQuery, () => search(), { immediate: true })
onMounted(async () => {
  const pager = await remoteSearchApp()
  if (pager) apps.value = pager.records
})
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>授权管理</el-breadcrumb-item>
        <el-breadcrumb-item>权限列表</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addDialog = true">添加权限</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-row style="margin-top: 20px;">
    <el-col :span="6">
      <el-select v-model="searchQuery.app_id" filterable clearable placeholder="请选择应用">
        <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
      </el-select>
    </el-col>
  </el-row>
  <el-dialog v-model="addDialog" align-center show-close>
    <AddPermission @close="closeAddDialog"></AddPermission>
  </el-dialog>
  <el-dialog v-model="editDialog" align-center show-close>
    <EditPermission :id="permissionId" @close="closeEditDialog"></EditPermission>
  </el-dialog>
  <el-table class="permission-list" :data="permissions" max-height="500" table-layout="auto"
            stripe @sort-change="permissionSortChange">
    <template #empty>没有权限数据</template>
    <el-table-column prop="name" label="权限名称" />
    <el-table-column prop="resource" label="资源路径" />
    <el-table-column prop="app.name" label="所属应用" />
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
    <el-table-column fixed="right">
      <template #header>
        <el-input v-model="searchQuery.name" size="small" clearable placeholder="根据权限名搜索" />
      </template>
      <template #default="scope">
        <el-button @click="openEditDialog(scope.row.id)">编辑</el-button>
        <el-button @click="deletePermission(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="searchQuery.pageSize" v-model:current-page="searchQuery.currentPage" />
  </el-row>
</template>

<style scoped>
.permission-list {
  width: 100%;
  margin-top: 20px;
}
</style>