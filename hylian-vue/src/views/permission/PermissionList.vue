<script setup>
import { format } from 'date-fns'
import { onMounted, reactive, ref, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog,
  ElIcon, ElInput, ElOption, ElPagination,
  ElRow, ElSelect, ElTable, ElTableColumn
} from 'element-plus'
import { asyncDeletePermission, asyncSearchPermissions } from '@/common/service'
import { confirmAndRemove, fetchAllApps, fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import AddPermission from '@/views/permission/AddPermission'
import EditPermission from '@/views/permission/EditPermission'

const permissionId = ref()
const permissions = ref([])
const apps = ref([])
const total = ref(0)
const addDialog = ref(false)
const editDialog = ref(false)
const query = reactive({
  current: 1,
  size: 20,
  name: null,
  app_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchPermissions(request)
  total.value = pager.total
  permissions.value = pager.records
}

const remove = async id => {
  if (!await confirmAndRemove(id, asyncDeletePermission, '删除提示', '确定删除权限信息？',
    '删除权限成功', '删除权限失败')) return
  await search()
}

const openEditDialog = id => {
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

watch(query, () => search(), { immediate: true })
onMounted(async () => apps.value = await fetchAllApps())
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
      <el-select v-model="query.app_id" filterable clearable placeholder="请选择应用">
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
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
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
        <el-input v-model="query.name" size="small" clearable placeholder="根据权限名搜索" />
      </template>
      <template #default="scope">
        <el-button @click="openEditDialog(scope.row.id)">编辑</el-button>
        <el-button @click="remove(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current" />
  </el-row>
</template>

<style scoped>
.permission-list {
  width: 100%;
  margin-top: 20px;
}
</style>