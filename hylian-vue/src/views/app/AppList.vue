<script setup>
import { format } from 'date-fns'
import { reactive, ref, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog,
  ElIcon, ElInput, ElMessageBox,
  ElNotification,
  ElPagination,
  ElRow,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { remoteDeleteApp, remoteSearchApp } from '@/utils/hylian-service'
import AddApp from '@/views/app/AddApp'
import EditApp from '@/views/app/EditApp'

const appId = ref()
const apps = ref([])
const total = ref(0)
const addAppDialog = ref(false)
const editAppDialog = ref(false)
const searcher = reactive({
  currentPage: 1,
  pageSize: 20,
  name: null,
  sortField: null,
  sortOrder: null
})

const searchApp = async () => {
  const searchRequest = {
    current: searcher.currentPage,
    size: searcher.pageSize
  }
  if (searcher.sortField && searcher.sortOrder) {
    searchRequest.order_by = [{ field: searcher.sortField, asc: searcher.sortOrder === 'ascending' }]
  }
  if (searcher.name && searcher.name !== '') searchRequest.name = searcher.name
  const pager = await remoteSearchApp(searchRequest)
  if (!pager) return
  apps.value = pager.records
  total.value = pager.total
}

const deleteApp = async (id) => {
  ElMessageBox.confirm(
    '确定删除应用信息？',
    '删除提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    if (!await remoteDeleteApp(id)) {
      ElNotification.error('删除应用失败')
      return
    }
    ElNotification.success('删除应用成功')
    await searchApp()
  })
}

const openEditAppDialog = (id) => {
  appId.value = id
  editAppDialog.value = true
}

const closeEditAppDialog = async () => {
  editAppDialog.value = false
  await searchApp()
}

const closeAddAppDialog = async () => {
  addAppDialog.value = false
  await searchApp()
}

const appSortChange = (event) => {
  if (!event || !event.prop) return
  searcher.sortField = event.prop
  searcher.sortOrder = event.order
}

watch(searcher, () => searchApp(), { immediate: true })
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
        <el-button @click="addAppDialog = true">添加应用</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addAppDialog" align-center show-close>
    <AddApp @close="closeAddAppDialog"></AddApp>
  </el-dialog>
  <el-dialog v-model="editAppDialog" align-center show-close>
    <EditApp :id="appId" @close="closeEditAppDialog"></EditApp>
  </el-dialog>
  <el-table class="app-list" :data="apps" max-height="500" table-layout="auto"
            stripe @sort-change="appSortChange">
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
        <el-input v-model="searcher.name" size="small" clearable placeholder="根据应用名搜索" />
      </template>
      <template #default="scope">
        <el-button @click="openEditAppDialog(scope.row.id)">编辑</el-button>
        <el-button @click="deleteApp(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="searcher.pageSize" v-model:current-page="searcher.currentPage" />
  </el-row>
</template>

<style scoped>
.app-list {
  width: 100%;
  margin-top: 20px;
}
</style>