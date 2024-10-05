<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, DocumentChecked, DocumentCopy, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem,
  ElIcon, ElInput,
  ElPagination,
  ElRow,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { remoteSearchActivity } from '@/utils/hylian-service'
import { copyToClipboard } from '@/utils/hylian'

const searchFormRef = useTemplateRef('searchFormRef')
const records = ref([])
const total = ref(0)
const copied = ref()
const searchQuery = reactive({
  currentPage: 1,
  pageSize: 20,
  userId: '',
  appId: '',
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
  if (searchQuery.appId) searchRequest.app_id = searchQuery.appId
  if (searchQuery.userId) searchRequest.user_id = searchQuery.userId
  const pager = await remoteSearchActivity(searchRequest)
  if (!pager) return
  records.value = pager.records
  total.value = pager.total
}

const activitySortChange = (event) => {
  if (!event || !event.prop) return
  searchQuery.sortField = event.prop
  searchQuery.sortOrder = event.order
}

watch(searchQuery, () => search(), { immediate: true })
</script>

<template>
  <el-row align="middle">
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>应用管理</el-breadcrumb-item>
      <el-breadcrumb-item>活动记录</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <el-form :inline="true" :model="searchQuery" ref="searchFormRef" style="margin-top: 20px;">
    <el-form-item label="应用ID" prop="appId">
      <el-input v-model="searchQuery.appId" clearable></el-input>
    </el-form-item>
    <el-form-item label="用户ID" prop="userId">
      <el-input v-model="searchQuery.userId" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="search">搜索</el-button>
      <el-button @click="searchFormRef.resetFields(); search()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="record-list" :data="records" max-height="500" table-layout="auto"
            stripe @sort-change="activitySortChange">
    <template #empty>没有活动记录</template>
    <el-table-column prop="session_id" label="会话ID" />
    <el-table-column prop="app.name" label="应用名">
      <template #default="scope">
        {{ scope.row.app.name }}
        <el-icon class="hover-text" @click="copyToClipboard(scope.row.app.id); copied = `app#${scope.row.id}`">
          <document-copy v-if="copied !== `app#${scope.row.id}`"></document-copy>
          <document-checked v-else></document-checked>
        </el-icon>
      </template>
    </el-table-column>
    <el-table-column prop="user.name" label="用户名">
      <template #default="scope">
        {{ scope.row.user.name }}
        <el-icon class="hover-text" @click="copyToClipboard(scope.row.user.id); copied = `user#${scope.row.id}`">
          <document-copy v-if="copied !== `user#${scope.row.id}`"></document-copy>
          <document-checked v-else></document-checked>
        </el-icon>
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
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="searchQuery.pageSize" v-model:current-page="searchQuery.currentPage" />
  </el-row>
</template>

<style scoped>
:deep(div.cell) {
  overflow: visible;
}
.record-list {
  width: 100%;
  margin-top: 20px;
}
.hover-text {
  position: relative;
}

.hover-text::after {
  content: "复制到剪贴板";
  position: absolute;
  top: 100%;
  left: 0;
  display: none;
  padding: 5px;
  font-size: 10px;
  text-wrap: nowrap;
  background-color: #ffffff;
  color: #000000;
}

.hover-text:hover::after {
  display: block;
}
</style>