<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { ArrowRight, Check, Timer, Warning } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import {
  asyncDeletePermission,
  asyncSearchPermissions,
  asyncUpdatePermission
} from '@/common/service'
import {
  fillSearchQuerySort,
  removeAfterConfirm,
  searchQueryToRequest,
  submitForm
} from '@/common/assortment'
import AppSelect from '@/components/app/AppSelect'
import AddPermission from '@/views/permission/AddPermission'

const formRef = useTemplateRef('formRef')
const tableRef = useTemplateRef('tableRef')
const permissions = ref([])
const total = ref(0)
const openAddDialog = ref(false)
const query = reactive({
  current: 1,
  size: 20,
  name: null,
  path: null,
  app_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.name) request.name = query.name
  if (query.path) request.path = query.path
  if (query.app_id) request.app_id = query.app_id
  const pager = await asyncSearchPermissions(request)
  total.value = pager.total
  permissions.value = pager.records
  permissions.value.forEach(permission => permission.prev = { name: permission.name,
    path: permission.path, app: JSON.parse(JSON.stringify(permission.app)) })
}

const remove = async id => {
  if (!await removeAfterConfirm(id, asyncDeletePermission, '删除提示', '确定删除权限信息？',
    '删除权限成功', '删除权限失败')) return
  await search()
}

const updateName = async row => {
  if (!await submitForm(undefined, { id: row.id, name: row.name },
    asyncUpdatePermission, '更新权限名成功', '更新权限名失败')) {
    row.name = row.prev.name
    return
  }
  row.prev.name = row.name
}

const updatePath = async row => {
  if (!await submitForm(undefined, { id: row.id, path: row.path },
    asyncUpdatePermission, '更新资源路径成功', '更新资源路径失败')) {
    row.path = row.prev.path
    return
  }
  row.prev.path = row.path
}

const updateApp = async (row, app) => {
  if (!await submitForm(undefined, { id: row.id, app_id: app.id },
    asyncUpdatePermission, '更新应用成功', '更新应用失败')) {
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
    row.path = row.prev.path
    row.app.id = row.prev.app.id
  }
}

const handleSelectAll = selection => {
  const rows = selection.length === 0 ? tableRef.value.data : selection
  rows.forEach(row => {
    row.checked = selection.length !== 0
    if (!row.checked) {
      row.name = row.prev.name
      row.path = row.prev.path
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
      <el-breadcrumb-item>权限列表</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 600px">
      <el-form-item label="应用选择" prop="app_id">
        <app-select v-model="query.app_id" placeholder="全部"></app-select>
      </el-form-item>
      <el-form-item label="权限搜索">
        <el-col :span="7">
          <el-form-item prop="name">
            <el-input v-model="query.name" clearable placeholder="根据权限名搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="1">
          <el-row align="middle" justify="center">-</el-row>
        </el-col>
        <el-col :span="7">
          <el-form-item prop="path">
            <el-input v-model="query.path" clearable placeholder="根据资源路径搜索" />
          </el-form-item>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="8"><a @click="formRef.resetFields(); search()">清除所有筛选条件</a></el-col>
      </el-form-item>
    </el-form>
  </div>
  <el-table ref="tableRef" :data="permissions" max-height="850" table-layout="auto"
            stripe @select="handleSelect" @select-all="handleSelectAll"
            @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有权限数据</template>
    <el-table-column type="selection" width="55" fixed="left" />
    <el-table-column prop="name" label="权限名称" width="180" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.row.checked" v-model="scope.row.name">
          <template #append>
            <el-popover v-if="scope.row.name !== scope.row.prev.name" content="权限名变更，点击保存">
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
    <el-table-column prop="path" label="资源路径" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.row.checked" v-model="scope.row.path">
          <template #append>
            <el-popover v-if="scope.row.path !== scope.row.prev.path" content="资源路径变更，点击保存">
              <template #reference>
                <el-button @click="updatePath(scope.row)">
                  <el-icon color="#ff0000"><warning></warning></el-icon>
                </el-button>
              </template>
            </el-popover>
            <el-button v-else>
              <el-icon color="#409eff"><check></check></el-icon>
            </el-button>
          </template>
        </el-input>
        <span v-else>{{ scope.row.path }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="app.name" label="所属应用" width="150" show-overflow-tooltip>
      <template #default="scope">
        <app-select v-if="scope.row.checked" v-model="scope.row.app.id"
                    :clearable="false" @change="app => updateApp(scope.row, app)"></app-select>
        <span v-else>{{ scope.row.app.name }}</span>
      </template>
    </el-table-column>
    <el-table-column label="创建时间" prop="create_time" sortable="custom" width="140" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column label="更新时间" prop="update_time" sortable="custom" width="140" show-overflow-tooltip>
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['update_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column fixed="right" width="120">
      <template #header>
        <el-button @click="openAddDialog = true">新增权限</el-button>
      </template>
      <template #default="scope">
        <a @click="remove(scope.row.id)">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current" />
  </el-row>
  <add-permission v-model="openAddDialog" @close="search()"></add-permission>
</template>

<style scoped>
</style>