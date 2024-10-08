<script setup>
import { format } from 'date-fns'
import { reactive, ref, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElIcon,
  ElInput, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { asyncDeleteApp, asyncSearchApps } from '@/common/service'
import { confirmAndRemove, fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import AddApp from '@/views/app/AddApp'
import EditApp from '@/views/app/EditApp'

const appId = ref()
const apps = ref([])
const total = ref(0)
const addDialog = ref(false)
const editDialog = ref(false)
const query = reactive({
  current: 1,
  size: 20,
  name: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  const pager = await asyncSearchApps(request)
  total.value = pager.total
  apps.value = pager.records
}

const remove = async id => {
  if (!await confirmAndRemove(id, asyncDeleteApp, '删除提示', '确定删除应用信息？',
    '删除应用成功', '删除应用失败')) return
  await search()
}

const openEditDialog = id => {
  appId.value = id
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
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>应用管理</el-breadcrumb-item>
        <el-breadcrumb-item>应用列表</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addDialog = true">添加应用</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addDialog" align-center show-close>
    <AddApp @close="closeAddDialog"></AddApp>
  </el-dialog>
  <el-dialog v-model="editDialog" align-center show-close>
    <EditApp :id="appId" @close="closeEditDialog"></EditApp>
  </el-dialog>
  <el-table class="app-list" :data="apps" max-height="500" table-layout="auto"
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有应用数据</template>
    <el-table-column prop="id" label="应用ID" />
    <el-table-column prop="name" label="应用名" />
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
        <el-input v-model="query.name" size="small" clearable placeholder="根据应用名搜索" />
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
.app-list {
  width: 100%;
  margin-top: 20px;
}
</style>