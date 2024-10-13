<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Check, Timer, Warning } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import {
  asyncDeleteRole,
  asyncSearchRoles,
  asyncUpdateRole
} from '@/common/service'
import {
  fillSearchQuerySort,
  removeAfterConfirm,
  searchQueryToRequest,
  submitForm
} from '@/common/assortment'
import AppSelect from '@/components/app/AppSelect'
import AddRole from '@/views/role/AddRole'
import AllocatePermission from '@/views/role/AllocatePermission'

const formRef = useTemplateRef('formRef')
const tableRef = useTemplateRef('tableRef')
const roles = ref([])
const total = ref(0)
const openAddDialog = ref(false)
const openAllocateDialog = ref(false)
const role = reactive({})
const query = reactive({
  current: 1,
  size: 20,
  name: null,
  app_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchRoles(request)
  total.value = pager.total
  roles.value = pager.records
  roles.value.forEach(role => role.prev = { name: role.name, app: JSON.parse(JSON.stringify(role.app)) })
}

const allocate = row => {
  role.roleId = row.id
  role.roleName = row.name
  role.appId = row.app.id
  openAllocateDialog.value = true
}

const remove = async id => {
  if (!await removeAfterConfirm(id, asyncDeleteRole, '删除提示', '确定删除角色信息？',
    '删除角色成功', '删除角色失败')) return
  await search()
}

const updateName = async row => {
  if (!await submitForm(undefined, { id: row.id, name: row.name },
    asyncUpdateRole, '更新角色名成功', '更新角色名失败')) {
    row.name = row.prev.name
    return
  }
  row.prev.name = row.name
}

const updateApp = async (row, app) => {
  if (!await submitForm(undefined, { id: row.id, app_id: app.id },
    asyncUpdateRole, '更新应用成功', '更新应用失败')) {
    row.app.id = row.prev.app.id
    return
  }
  row.app = app
  row.prev.app = JSON.parse(JSON.stringify(row.app))
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.name = row.prev.name
    row.app.id = row.prev.app.id
  }
}

const handleSelectAll = selection => {
  const rows = selection.length === 0 ? tableRef.value.data : selection
  rows.forEach(row => {
    row.checked = selection.length !== 0
    if (!row.checked) {
      row.name = row.prev.name
      row.app.id = row.prev.app.id
    }
  })
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row>
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>授权管理</el-breadcrumb-item>
      <el-breadcrumb-item>角色列表</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 400px">
      <el-form-item label="应用选择" prop="app_id">
        <app-select v-model="query.app_id" placeholder="全部"></app-select>
      </el-form-item>
      <el-form-item label="角色搜索" prop="name">
        <el-col :span="14">
          <el-input v-model="query.name" clearable placeholder="根据角色名搜索" />
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="9"><a @click="formRef.resetFields(); search()">清除所有筛选条件</a></el-col>
      </el-form-item>
    </el-form>
  </div>
  <el-table ref="tableRef" :data="roles" max-height="850" table-layout="auto"
            stripe @select="handleSelect" @select-all="handleSelectAll"
            @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有角色数据</template>
    <el-table-column type="selection" width="55" fixed="left" />
    <el-table-column prop="name" label="角色名称" width="250" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.row.checked" v-model="scope.row.name">
          <template #append>
            <el-popover v-if="scope.row.name !== scope.row.prev.name" content="角色名变更，点击保存">
              <template #reference>
                <el-button @click="updateName(scope.row)">
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
    <el-table-column prop="app.name" label="所属应用" width="250" show-overflow-tooltip>
      <template #default="scope">
        <app-select v-if="scope.row.checked" v-model="scope.row.app.id"
                       :clearable="false" @change="app => updateApp(scope.row, app)"></app-select>
        <span v-else>{{ scope.row.app.name }}</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" prop="create_time" sortable="custom" width="200" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column label="更新时间" prop="update_time" sortable="custom" width="200" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column fixed="right">
      <template #header>
        <el-button @click="openAddDialog = true">新增角色</el-button>
      </template>
      <template #default="scope">
        <a @click="allocate(scope.row)">权限分配</a>&nbsp;
        <a @click="remove(scope.row.id)">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current" />
  </el-row>
  <add-role v-model="openAddDialog" @close="openAddDialog = false; search()"></add-role>
  <allocate-permission v-bind="role" v-model="openAllocateDialog"
                       @close="openAllocateDialog = false"></allocate-permission>
</template>

<style scoped>
</style>