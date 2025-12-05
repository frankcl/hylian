<script setup>
import { IconAlertCircle, IconCircleCheck, IconClock, IconPlus, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider, ElForm,
  ElFormItem, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveTenant,
  asyncSearchTenant,
  asyncUpdateTenant,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import HylianCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AddTenant from '@/views/tenant/AddTenant'

const userStore = useUserStore()
const tableRef = useTemplateRef('table')
const openAdd = ref(false)
const total = ref(0)
const tenants = ref([])
const query = reactive(newSearchQuery())

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  const pager = await asyncSearchTenant(request)
  total.value = pager.total
  tenants.value = pager.records
  tenants.value.forEach(tenant => tenant.prev = { name: tenant.name })
}

const add = () => openAdd.value = true

const update = async tenant => {
  if (!await asyncUpdateTenant({ id: tenant.id, name: tenant.name })) {
    showMessage('更新租户名失败', ERROR)
    tenant.name = tenant.prev.name
    return
  }
  showMessage('更新权限名成功', SUCCESS)
  tenant.prev.name = tenant.name
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveTenant, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除租户失败', ERROR)
    return
  }
  showMessage('删除租户成功', SUCCESS)
  await search()
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) row.name = row.prev.name
}

const handleSelectAll = selection => {
  const rows = tableRef.value.data
  rows.forEach(row => handleSelect(selection, row))
}

watchEffect(async () => await search())
</script>

<template>
  <add-tenant v-model="openAdd" @close="search" />
  <hylian-card>
    <template #title>
      <el-breadcrumb>
        <el-breadcrumb-item>账号管理</el-breadcrumb-item>
        <el-breadcrumb-item>租户管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" label-width="80px" class="mb-4">
      <el-form-item label="租户搜索">
        <el-col :span="10">
          <el-input v-model="query.name" clearable placeholder="根据租户名搜索" />
        </el-col>
      </el-form-item>
    </el-form>
    <table-head title="租户列表">
      <template #right>
        <el-button type="primary" @click="add" :disabled="!userStore.superAdmin">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="table" :data="tenants" max-height="650" table-layout="auto" stripe
              class="mb-4" @select="handleSelect" @select-all="handleSelectAll"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>暂无租户数据</template>
      <el-table-column v-if="userStore.superAdmin" type="selection" width="55" fixed="left" />
      <el-table-column prop="name" label="租户名" show-overflow-tooltip>
        <template #default="scope">
          <el-input v-if="scope.row.checked" v-model="scope.row.name">
            <template #append>
              <el-popover v-if="scope.row.name !== scope.row.prev.name" content="租户名变更，点击保存">
                <template #reference>
                  <el-button @click="update(scope.row)" class="d-flex align-items-center">
                    <IconAlertCircle color="#f56c6c" size="18" />
                  </el-button>
                </template>
              </el-popover>
              <el-button v-else class="d-flex align-items-center" >
                <IconCircleCheck color="#67c23a" size="18" />
              </el-button>
            </template>
          </el-input>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="创建时间" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="update_time" label="更新时间" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['update_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" :disabled="!userStore.superAdmin" @click="remove(scope.row.id)">
            <IconTrash size="20" class="mr-2" />
            <span>删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-config-provider :locale="zhCn">
        <el-pagination background layout="total, prev, pager, next, jumper" :total="total"
                       v-model:page-size="query.page_size" v-model:current-page="query.page_num"/>
      </el-config-provider>
    </el-row>
  </hylian-card>
</template>

<style scoped>
</style>