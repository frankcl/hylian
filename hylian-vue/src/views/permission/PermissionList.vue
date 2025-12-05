<script setup>
import { IconAlertCircle, IconCircleCheck, IconClearAll, IconClock, IconPlus, IconTrash } from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider, ElForm,
  ElFormItem, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { formatDate } from '@/common/Time'
import {
  asyncRemovePermission,
  asyncSearchPermission,
  asyncUpdatePermission,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import AppSelect from '@/components/app/AppSelect'
import AddPermission from '@/views/permission/AddPermission'
import HylianCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'

const formRef = useTemplateRef('form')
const tableRef = useTemplateRef('table')
const permissions = ref([])
const total = ref(0)
const openAdd = ref(false)
const query = reactive(newSearchQuery({}))

const add = () => openAdd.value = true

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.path) request.path = query.path
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchPermission(request)
  total.value = pager.total
  permissions.value = pager.records
  permissions.value.forEach(permission => permission.prev = { name: permission.name,
    path: permission.path, app: JSON.parse(JSON.stringify(permission.app)) })
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemovePermission, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除权限失败', ERROR)
    return
  }
  showMessage('删除权限成功', SUCCESS)
  await search()
}

const updateName = async permission => {
  if (!await asyncUpdatePermission({ id: permission.id, name: permission.name })) {
    showMessage('更新权限名失败', ERROR)
    permission.name = permission.prev.name
    return
  }
  showMessage('更新权限名成功', SUCCESS)
  permission.prev.name = permission.name
}

const updatePath = async permission => {
  if (!await asyncUpdatePermission({ id: permission.id, path: permission.path })) {
    showMessage('更新资源路径失败', ERROR)
    permission.path = permission.prev.path
    return
  }
  showMessage('更新资源路径成功', SUCCESS)
  permission.prev.path = permission.path
}

const updateApp = async (permission, app) => {
  if (!await asyncUpdatePermission({ id: permission.id, app_id: app.id })) {
    showMessage('更新应用失败', ERROR)
    permission.app.id = permission.prev.app.id
    return
  }
  showMessage('更新应用成功', SUCCESS)
  permission.app = app
  permission.prev.app = JSON.parse(JSON.stringify(permission.app))
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.name = row.prev.name
    row.path = row.prev.path
    row.app.id = row.prev.app.id
  }
}

const handleSelectAll = selection => {
  const rows = tableRef.value.data
  rows.forEach(row => handleSelect(selection, row))
}

watchEffect(async () => await search())
</script>

<template>
  <hylian-card>
    <template #title>
      <el-breadcrumb>
        <el-breadcrumb-item>角色授权</el-breadcrumb-item>
        <el-breadcrumb-item>权限管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-form-item label="应用选择" prop="app_id">
            <app-select v-model="query.app_id" placeholder="全部" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="权限搜索" prop="name">
            <el-input v-model="query.name" clearable placeholder="根据权限名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item prop="path" label-position="top">
            <el-input v-model="query.path" clearable placeholder="根据资源路径搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" plain @click="formRef.resetFields()">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="权限列表">
      <template #right>
        <el-button type="primary" @click="add">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="table" :data="permissions" max-height="650" table-layout="auto" stripe
              class="mb-4" @select="handleSelect" @select-all="handleSelectAll"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>暂无权限数据</template>
      <el-table-column type="selection" width="55" fixed="left" />
      <el-table-column prop="name" label="权限名称" show-overflow-tooltip>
        <template #default="scope">
          <el-input v-if="scope.row.checked" v-model="scope.row.name">
            <template #append>
              <el-popover v-if="scope.row.name !== scope.row.prev.name" content="权限名变更，点击保存">
                <template #reference>
                  <el-button @click="updateName(scope.row)" class="d-flex align-items-center">
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
      <el-table-column prop="path" label="资源路径" show-overflow-tooltip>
        <template #default="scope">
          <el-input v-if="scope.row.checked" v-model="scope.row.path">
            <template #append>
              <el-popover v-if="scope.row.path !== scope.row.prev.path" content="资源路径变更，点击保存">
                <template #reference>
                  <el-button @click="updatePath(scope.row)" class="d-flex align-items-center">
                    <IconAlertCircle color="#f56c6c" size="18" />
                  </el-button>
                </template>
              </el-popover>
              <el-button v-else class="d-flex align-items-center" >
                <IconCircleCheck color="#67c23a" size="18" />
              </el-button>
            </template>
          </el-input>
          <span v-else>{{ scope.row.path }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="app.name" label="所属应用" width="150" show-overflow-tooltip>
        <template #default="scope">
          <app-select v-if="scope.row.checked" v-model="scope.row.app.id"
                      :clearable="false" @change="app => updateApp(scope.row, app)" />
          <span v-else>{{ scope.row.app.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" width="200" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" sortable="custom" width="200" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['update_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" @click="remove(scope.row.id)">
            <IconTrash size="20" class="mr-2" />
            <span>删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-row justify="center" align="middle">
      <el-config-provider :locale="zhCn">
        <el-pagination background layout="total, prev, pager, next, jumper" :total="total"
                       v-model:page-size="query.page_size" v-model:current-page="query.page_num" />
      </el-config-provider>
    </el-row>
  </hylian-card>
  <add-permission v-model="openAdd" @close="search" />
</template>

<style scoped>
</style>