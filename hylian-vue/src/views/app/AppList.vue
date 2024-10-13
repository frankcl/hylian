<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import {ArrowRight, Check, CopyDocument, DocumentCopy, Timer, Warning} from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElForm, ElFormItem,
  ElIcon, ElInput, ElPagination, ElPopover, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import {
  asyncDeleteApp,
  asyncSearchApps,
  asyncUpdateApp
} from '@/common/service'
import {
  copyToClipboard,
  fillSearchQuerySort,
  removeAfterConfirm,
  searchQueryToRequest,
  submitForm
} from '@/common/assortment'
import AddApp from '@/views/app/AddApp'
import AllocateUser from '@/views/app/AllocateUser'

const tableRef = useTemplateRef('tableRef')
const apps = ref([])
const total = ref(0)
const copyApp = ref()
const openAddDialog = ref(false)
const openAllocateDialog = ref(false)
const app = reactive({})
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
  const pager = await asyncSearchApps(request)
  total.value = pager.total
  apps.value = pager.records
  apps.value.forEach(app => app.prev = { name: app.name, description: app.description })
}

const remove = async id => {
  if (!await removeAfterConfirm(id, asyncDeleteApp, '删除提示', '确定删除应用信息？',
    '删除应用成功', '删除应用失败')) return
  await search()
}

const allocate = row => {
  app.appId = row.id
  app.appName = row.name
  openAllocateDialog.value = true
}

const updateName = async row => {
  if (!await submitForm(undefined, { id: row.id, name: row.name },
    asyncUpdateApp, '更新应用名成功', '更新应用名失败')) {
    row.name = row.prev.name
    return
  }
  row.prev.name = row.name
}

const updateDescription = async row => {
  if (!await submitForm(undefined, { id: row.id, description: row.description },
    asyncUpdateApp, '更新应用描述成功', '更新应用描述失败')) {
    row.description = row.prev.description
    return
  }
  row.prev.description = row.description
}

const handleSelect = (selection, row) => {
  row.checked = selection.indexOf(row) !== -1
  if (!row.checked) {
    row.name = row.prev.name
    row.description = row.prev.description
  }
}

const handleSelectAll = selection => {
  const rows = selection.length === 0 ? tableRef.value.data : selection
  rows.forEach(row => {
    row.checked = selection.length !== 0
    if (!row.checked) {
      row.name = row.prev.name
      row.description = row.prev.description
    }
  })
}

watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row>
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>应用管理</el-breadcrumb-item>
      <el-breadcrumb-item>应用列表</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 400px">
      <el-form-item label="应用搜索" prop="name">
        <el-input v-model="query.name" clearable placeholder="根据应用名搜索" />
      </el-form-item>
    </el-form>
  </div>
  <el-table ref="tableRef" :data="apps" max-height="850" table-layout="auto"
            stripe @select="handleSelect" @select-all="handleSelectAll"
            @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有应用数据</template>
    <el-table-column type="selection" width="55" fixed="left" />
    <el-table-column prop="id" label="应用ID" width="200" show-overflow-tooltip>
      <template #default="scope">
        <el-icon v-if="copyApp === scope.row.id"><document-copy></document-copy></el-icon>
        <el-popover v-else content="点击复制">
          <template #reference>
            <el-icon @click="copyToClipboard(scope.row.id); copyApp = scope.row.id">
              <copy-document></copy-document>
            </el-icon>
          </template>
        </el-popover>
        {{ scope.row.id }}
      </template>
    </el-table-column>
    <el-table-column prop="name" label="应用名" width="180" show-overflow-tooltip>
      <template #default="scope">
        <el-input v-if="scope.row.checked" v-model="scope.row.name">
          <template #append>
            <el-popover v-if="scope.row.name !== scope.row.prev.name" content="应用名变更，点击保存">
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
    <el-table-column prop="description" label="应用描述" show-overflow-tooltip>
      <template #default="scope">
        <div v-if="scope.row.checked">
          <el-input type="textarea" autosize v-model="scope.row.description"></el-input>
          <el-popover v-if="scope.row.description !== scope.row.prev.description" content="应用描述变更，点击保存">
            <template #reference>
              <el-row style="margin-top: 5px" align="middle" justify="center">
                <el-button size="small" @click="updateDescription(scope.row)">
                  点击保存&nbsp;
                  <el-icon color="#ff0000"><warning></warning></el-icon>
                </el-button>
              </el-row>
            </template>
          </el-popover>
        </div>
        <span v-else>{{ scope.row.description }}</span>
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
    <el-table-column width="180" fixed="right">
      <template #header>
        <el-button @click="openAddDialog = true">增加应用</el-button>
      </template>
      <template #default="scope">
        <RouterLink :to="{ name: 'ActivityList', query: { appId: scope.row.id } }">活跃记录</RouterLink>&nbsp;
        <a @click="allocate(scope.row)">管理员</a>&nbsp;
        <a @click="remove(scope.row.id)">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current" />
  </el-row>
  <add-app v-model="openAddDialog" @close="openAddDialog = false; search()"></add-app>
  <allocate-user v-model="openAllocateDialog" v-bind="app"
                 @close="openAllocateDialog = false; search()"></allocate-user>
</template>

<style scoped>
</style>