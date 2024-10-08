<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElDialog, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { asyncDeleteTenant, asyncSearchTenants } from '@/common/service'
import { confirmAndRemove, fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import AddTenant from '@/views/tenant/AddTenant'
import EditTenant from '@/views/tenant/EditTenant'

const formRef = useTemplateRef('formRef')
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const tenantId = ref()
const total = ref(0)
const tenants = ref([])
const query = reactive({
  current: 1,
  size: 20,
  sort_field: null,
  sort_order: null
})
const formData = reactive({
  name: ''
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (formData.name) request.name = formData.name
  const pager = await asyncSearchTenants(request)
  total.value = pager.total
  tenants.value = pager.records
}

const remove = async id => {
  if (!await confirmAndRemove(id, asyncDeleteTenant, '删除提示', '确定删除租户信息？',
    '删除租户成功', '删除租户失败')) return
  await search()
}

const openEditDialog = (id) => {
  tenantId.value = id
  editDialogVisible.value = true
}

const closeEditDialog = async () => {
  editDialogVisible.value = false
  await search()
}

const closeAddDialog = async () => {
  addDialogVisible.value = false
  await search()
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row align="middle">
    <el-col :span="20">
      <el-breadcrumb :separator-icon="ArrowRight">
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>租户</el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
    <el-col :span="4">
      <el-row justify="end">
        <el-button @click="addDialogVisible = true">添加租户</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addDialogVisible" align-center show-close>
    <AddTenant @close="closeAddDialog"></AddTenant>
  </el-dialog>
  <el-dialog v-model="editDialogVisible" align-center show-close>
    <EditTenant :id="tenantId" @close="closeEditDialog"></EditTenant>
  </el-dialog>
  <el-form :inline="true" :model="formData" ref="formRef" style="margin-top: 20px;">
    <el-form-item label="租户" prop="name">
      <el-input v-model="formData.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="search">搜索</el-button>
      <el-button @click="formRef.resetFields(); search()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="tenant-list" :data="tenants" max-height="500" table-layout="auto"
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有租户数据</template>
    <el-table-column prop="name" label="租户" />
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
.tenant-list {
  width: 100%;
  margin-top: 20px;
}
</style>