<script setup>
import { format } from 'date-fns'
import { onMounted, reactive, ref, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog,
  ElIcon, ElInput,
  ElNotification, ElOption,
  ElPagination,
  ElRow, ElSelect,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { remoteDeleteRole, remoteSearchRole } from '@/utils/hylian-service'
import AddRole from '@/views/role/AddRole'
import EditRole from '@/views/role/EditRole'
import { fetchAllApps, initSearchRequest, popConfirmBox } from './common'

const roleId = ref()
const roles = ref([])
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
  const searchRequest = initSearchRequest(searchQuery)
  if (searchQuery.name) searchRequest.name = searchQuery.name
  if (searchQuery.app_id) searchRequest.app_id = searchQuery.app_id
  const pager = await remoteSearchRole(searchRequest)
  if (!pager) return
  roles.value = pager.records
  total.value = pager.total
}

const deleteRole = id => {
  popConfirmBox('删除提示', '确定删除角色信息？',
    async () => {
    if (!await remoteDeleteRole(id)) {
      ElNotification.error('删除角色失败')
      return
    }
    ElNotification.success('删除角色成功')
    await search()
  })
}

const openEditDialog = (id) => {
  roleId.value = id
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

const roleSortChange = (event) => {
  if (!event || !event.prop) return
  searchQuery.sortField = event.prop
  searchQuery.sortOrder = event.order
}

watch(searchQuery, () => search(), { immediate: true })
onMounted(async () => apps.value = await fetchAllApps() )
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>授权管理</el-breadcrumb-item>
        <el-breadcrumb-item>角色列表</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addDialog = true">添加角色</el-button>
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
    <AddRole @close="closeAddDialog"></AddRole>
  </el-dialog>
  <el-dialog v-model="editDialog" align-center show-close>
    <EditRole :id="roleId" @close="closeEditDialog"></EditRole>
  </el-dialog>
  <el-table class="role-list" :data="roles" max-height="500" table-layout="auto"
            stripe @sort-change="roleSortChange">
    <template #empty>没有角色数据</template>
    <el-table-column prop="name" label="角色名称" />
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
        <el-input v-model="searchQuery.name" size="small" clearable placeholder="根据角色名搜索" />
      </template>
      <template #default="scope">
        <el-button @click="openEditDialog(scope.row.id)">编辑</el-button>
        <el-button @click="deleteRole(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="searchQuery.pageSize" v-model:current-page="searchQuery.currentPage" />
  </el-row>
</template>

<style scoped>
.role-list {
  width: 100%;
  margin-top: 20px;
}
</style>