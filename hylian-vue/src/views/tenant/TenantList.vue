<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Check, Timer, Warning } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm,
  ElFormItem, ElIcon, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { asyncDeleteTenant, asyncSearchTenants, asyncUpdateTenant } from '@/common/service'
import { confirmAndRemove, fillSearchQuerySort, searchQueryToRequest, submitForm } from '@/common/assortment'
import AddTenantDialog from '@/views/tenant/AddTenantDialog'

const tableRef = useTemplateRef('tableRef')
const openAddDialog = ref(false)
const total = ref(0)
const tenants = ref([])
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
  const pager = await asyncSearchTenants(request)
  total.value = pager.total
  tenants.value = pager.records
}

const update = async row => {
  if (row.changed) {
    if (!await submitForm(undefined, { id: row.id, name: row.name },
      asyncUpdateTenant, '更新租户名成功', '更新租户名失败')) return
    row.changed = false
  }
}

const remove = async id => {
  if (!await confirmAndRemove(id, asyncDeleteTenant, '删除提示', '确定删除租户信息？',
    '删除租户成功', '删除租户失败')) return
  await search()
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
}

const handleSelectAll = selection => {
  const rows = selection.length === 0 ? tableRef.value.data : selection
  rows.forEach(row => row.checked = selection.length !== 0)
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <add-tenant-dialog v-model="openAddDialog" @close="openAddDialog = false; search()"></add-tenant-dialog>
  <el-row align="middle">
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>账号管理</el-breadcrumb-item>
      <el-breadcrumb-item>租户列表</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" label-width="auto" style="max-width: 400px">
      <el-form-item label="租户搜索" prop="user_id">
        <el-input v-model="query.name" clearable placeholder="根据租户名搜索"></el-input>
      </el-form-item>
    </el-form>
  </div>
  <el-table ref="tableRef" :data="tenants" max-height="850" table-layout="auto"
            stripe @select="handleSelect" @select-all="handleSelectAll"
            @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有租户数据</template>
    <el-table-column type="selection" width="55" fixed="left" />
    <el-table-column prop="name" label="租户名" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.row.checked" v-model="scope.row.name" @input="scope.row.changed = true">
          <template #append>
            <el-popover v-if="scope.row.changed" content="租户名变更，点击保存">
              <template #reference>
                <el-button @click="update(scope.row)">
                  <el-icon color="#ff0000"><warning></warning></el-icon>
                </el-button>
              </template>
            </el-popover>
            <el-button v-else>
              <el-icon color="#409eff"><check></check></el-icon>
            </el-button>
          </template>
        </el-input>
        <span v-else>{{ scope.row.name }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="create_time" label="创建时间" sortable="custom" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column prop="update_time" label="更新时间" sortable="custom" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column fixed="right" width="120">
      <template #header>
        <el-button @click="openAddDialog = true">新增租户</el-button>
      </template>
      <template #default="scope">
        <a @click="remove(scope.row.id)">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current"/>
  </el-row>
</template>

<style scoped>
</style>