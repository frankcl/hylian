<script setup>
import {
  IconAlertCircle, IconClearAll, IconClock,
  IconCircleCheck, IconLockCheck, IconPlus, IconTrash
} from '@tabler/icons-vue'
import { reactive, ref, useTemplateRef, watchEffect } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider, ElForm,
  ElFormItem, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import AppSelect from '@/components/app/AppSelect'
import AddRole from '@/views/role/AddRole'
import RolePermission from '@/views/role/RolePermission'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveRole,
  asyncSearchRole,
  asyncUpdateRole,
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

const formRef = useTemplateRef('form')
const tableRef = useTemplateRef('table')
const roles = ref([])
const total = ref(0)
const openAdd = ref(false)
const openRolePermission = ref(false)
const role = reactive({})
const query = reactive(newSearchQuery({ name: null, app_id: null }))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchRole(request)
  total.value = pager.total
  roles.value = pager.records
  roles.value.forEach(role => role.prev = { name: role.name, app: JSON.parse(JSON.stringify(role.app)) })
}

const permission = row => {
  role.roleId = row.id
  role.roleName = row.name
  role.appId = row.app.id
  openRolePermission.value = true
}

const add = () => openAdd.value = true

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveRole, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除角色失败', ERROR)
    return
  }
  showMessage('删除角色成功', SUCCESS)
  await search()
}

const updateName = async role => {
  if (!await asyncUpdateRole({ id: role.id, name: role.name })) {
    showMessage('更新角色名失败', ERROR)
    role.name = role.prev.name
    return
  }
  showMessage('更新角色名成功', SUCCESS)
  role.prev.name = role.name
}

const updateApp = async (role, app) => {
  if (!await asyncUpdateRole({ id: role.id, app_id: app.id })) {
    showMessage('更新应用失败', ERROR)
    role.app.id = role.prev.app.id
    return
  }
  showMessage('更新应用成功', SUCCESS)
  role.app = app
  role.prev.app = JSON.parse(JSON.stringify(role.app))
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.name = row.prev.name
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
        <el-breadcrumb-item>角色管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="应用选择" prop="app_id">
            <app-select v-model="query.app_id" placeholder="全部" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色搜索" prop="name">
            <el-input v-model="query.name" clearable placeholder="根据角色名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-button type="primary" plain @click="formRef.resetFields()">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="角色列表">
      <template #right>
        <el-button type="primary" @click="add">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table ref="table" :data="roles" max-height="650" table-layout="auto" class="mb-4"
              stripe @select="handleSelect" @select-all="handleSelectAll"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>暂无角色数据</template>
      <el-table-column type="selection" width="55" fixed="left" />
      <el-table-column prop="name" label="角色名称" show-overflow-tooltip>
        <template #default="scope">
          <el-input v-if="scope.row.checked" v-model="scope.row.name">
            <template #append>
              <el-popover v-if="scope.row.name !== scope.row.prev.name" content="角色名变更，点击保存">
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
      <el-table-column prop="app.name" label="所属应用" show-overflow-tooltip>
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
      <el-table-column>
        <template #default="scope">
          <el-button type="primary" plain @click="permission(scope.row)">
            <IconLockCheck size="20" class="mr-2" />
            <span>权限</span>
          </el-button>
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
  <add-role v-model="openAdd" @close="search" />
  <role-permission v-bind="role" v-model="openRolePermission" />
</template>

<style scoped>
</style>