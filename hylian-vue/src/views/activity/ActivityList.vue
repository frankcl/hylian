<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, DocumentChecked, DocumentCopy, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { copyToClipboard, fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import { asyncSearchActivities } from '@/common/service'

const formRef = useTemplateRef('formRef')
const total = ref(0)
const copiedId = ref()
const activityList = ref([])
const query = reactive({
  current: 1,
  size: 20,
  user_id: '',
  app_id: '',
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.app_id) request.app_id = query.app_id
  if (query.user_id) request.user_id = query.user_id
  const pager = await asyncSearchActivities(request)
  if (!pager) return
  total.value = pager.total
  activityList.value = pager.records
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row align="middle">
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>应用管理</el-breadcrumb-item>
      <el-breadcrumb-item>活动记录</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <el-form :inline="true" :model="query" ref="formRef" style="margin-top: 20px;">
    <el-form-item label="应用ID" prop="app_id">
      <el-input v-model="query.app_id" clearable></el-input>
    </el-form-item>
    <el-form-item label="用户ID" prop="user_id">
      <el-input v-model="query.user_id" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="search">搜索</el-button>
      <el-button @click="formRef.resetFields(); search()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="record-list" :data="activityList" max-height="500" table-layout="auto"
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有活动记录</template>
    <el-table-column prop="session_id" label="会话ID" />
    <el-table-column prop="app.name" label="应用名">
      <template #default="scope">
        {{ scope.row.app.name }}
        <el-icon class="hover-text" @click="copyToClipboard(scope.row.app.id); copiedId = `app#${scope.row.id}`">
          <document-copy v-if="copiedId !== `app#${scope.row.id}`"></document-copy>
          <document-checked v-else></document-checked>
        </el-icon>
      </template>
    </el-table-column>
    <el-table-column prop="user.name" label="用户名">
      <template #default="scope">
        {{ scope.row.user.name }}
        <el-icon class="hover-text" @click="copyToClipboard(scope.row.user.id); copiedId = `user#${scope.row.id}`">
          <document-copy v-if="copiedId !== `user#${scope.row.id}`"></document-copy>
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
                   v-model:page-size="query.size" v-model:current-page="query.current" />
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