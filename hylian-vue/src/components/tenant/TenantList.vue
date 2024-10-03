<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem,
  ElButton, ElCol,
  ElDialog, ElForm, ElFormItem,
  ElIcon, ElInput, ElMessageBox,
  ElNotification,
  ElPagination,
  ElRow,
  ElTable,
  ElTableColumn
} from 'element-plus'
import { remoteDeleteTenant, remoteSearchTenant } from '@/utils/hylian-service'
import { resetForm } from '@/utils/hylian'
import AddTenant from '@/components/tenant/AddTenant'
import EditTenant from '@/components/tenant/EditTenant'

const searchFormRef = useTemplateRef('searchFormRef')
const addTenantVisible = ref(false)
const editTenantVisible = ref(false)
const editTenantId = ref()
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tenants = ref([])
const orderField = reactive({
  field: null,
  order: null
})
const searchForm = reactive({
  name: ''
})

const searchTenant = async () => {
  const searchRequest = {
    current: currentPage.value,
    size: pageSize.value
  }
  const orderArray = []
  if (orderField.field && orderField.order) {
    orderArray.push({ field: orderField.field, asc: orderField.order === 'ascending'})
  }
  searchRequest.order_by = orderArray
  if (searchForm.name !== '') searchRequest.name = searchForm.name
  const pager = await remoteSearchTenant(searchRequest)
  if (!pager) return
  tenants.value = pager.records
  total.value = pager.total
}

const deleteTenant = async (id) => {
  ElMessageBox.confirm(
    '确定删除租户信息？',
    '删除提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    if (!await remoteDeleteTenant(id)) {
      ElNotification.error('删除租户失败')
      return
    }
    ElNotification.success('删除租户成功')
    await searchTenant()
  })
}

const openEditTenantDialog = (id) => {
  editTenantId.value = id
  editTenantVisible.value = true
}

const closeEditTenantDialog = async () => {
  editTenantVisible.value = false
  await searchTenant()
}

const closeAddTenantDialog = async () => {
  addTenantVisible.value = false
  await searchTenant()
}

const tenantSortChange = (event) => {
  if (!event || !event.prop) return
  orderField.field = event.prop
  orderField.order = event.order
}

watch([currentPage, pageSize, orderField], () => searchTenant(), { immediate: true })
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
        <el-button @click="addTenantVisible = true">添加租户</el-button>
      </el-row>
    </el-col>
  </el-row>
  <el-dialog v-model="addTenantVisible" align-center show-close>
    <AddTenant @close="closeAddTenantDialog"></AddTenant>
  </el-dialog>
  <el-dialog v-model="editTenantVisible" align-center show-close>
    <EditTenant :id="editTenantId" @close="closeEditTenantDialog"></EditTenant>
  </el-dialog>
  <el-form :inline="true" :model="searchForm" ref="searchFormRef" style="margin-top: 20px;">
    <el-form-item label="租户" prop="name">
      <el-input v-model="searchForm.name" clearable></el-input>
    </el-form-item>
    <el-form-item>
      <el-button @click="searchTenant">搜索</el-button>
      <el-button @click="resetForm(searchFormRef); searchTenant()">重置</el-button>
    </el-form-item>
  </el-form>
  <el-table class="tenant-list" :data="tenants" max-height="500" table-layout="auto"
            stripe @sort-change="tenantSortChange">
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
        <el-button @click="openEditTenantDialog(scope.row.id)">编辑</el-button>
        <el-button @click="deleteTenant(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="pageSize" v-model:current-page="currentPage"/>
  </el-row>
</template>

<style scoped>
.tenant-list {
  width: 100%;
  margin-top: 20px;
}
</style>