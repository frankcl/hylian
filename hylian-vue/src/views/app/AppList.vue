<script setup>
import {
  IconActivity, IconClock, IconCopy, IconCopyCheck, IconEdit, IconPlus, IconTrash, IconUserCog
} from '@tabler/icons-vue'
import { reactive, ref, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider,
  ElDropdown, ElDropdownItem, ElDropdownMenu, ElForm, ElFormItem,
  ElInput, ElMessage, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { useUserStore } from '@/store'
import { formatDate } from '@/common/Time'
import {
  asyncRemoveApp,
  asyncSearchApp,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import { writeClipboard } from '@/common/Clipboard'
import {
  asyncExecuteAfterConfirming,
  ERROR, showMessage, SUCCESS
} from '@/common/Feedback'
import HylianCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AddApp from '@/views/app/AddApp'
import AppUser from '@/views/app/AppUser'
import EditApp from '@/views/app/EditApp'

const router = useRouter()
const userStore = useUserStore()
const apps = ref([])
const total = ref(0)
const copied = ref()
const openAdd = ref(false)
const openEdit = ref(false)
const openAppUser = ref(false)
const app = reactive({})
const query = reactive(newSearchQuery( { name: null }))

const search = async () => {
  const request = newSearchRequest(query)
  if (query.name) request.name = query.name
  const pager = await asyncSearchApp(request)
  total.value = pager.total
  apps.value = pager.records
}

const add = () => openAdd.value = true

const edit = id => {
  app.id = id
  openEdit.value = true
}

const remove = async id => {
  const success = await asyncExecuteAfterConfirming(asyncRemoveApp, id)
  if (success === undefined) return
  if (!success) {
    showMessage('删除应用失败', ERROR)
    return
  }
  showMessage('删除应用成功', SUCCESS)
  await search()
}

const admin = row => {
  app.id = row.id
  app.name = row.name
  openAppUser.value = true
}

const copy = (id, value) => {
  writeClipboard(value)
  copied.value = `name#${id}`
  ElMessage.success('复制成功')
}

const handleCommand = async (command, app) => {
  if (command === 'remove') {
    await remove(app.id)
  } else if (command === 'activity') {
    await router.push({ path: '/workbench/activityList', query: { app_id: app.id } })
  }
}

watchEffect(async () => await search())
</script>

<template>
  <hylian-card>
    <template #title>
      <el-breadcrumb>
        <el-breadcrumb-item>应用活动</el-breadcrumb-item>
        <el-breadcrumb-item>应用管理</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" label-width="80px" class="mb-4">
      <el-row>
        <el-col :span="12">
          <el-form-item label="应用搜索" prop="name">
            <el-input v-model="query.name" clearable placeholder="根据应用名搜索" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="应用列表">
      <template #right>
        <el-button type="primary" @click="add" :disabled="!userStore.superAdmin">
          <IconPlus size="20" class="mr-1" />
          <span>新增</span>
        </el-button>
      </template>
    </table-head>
    <el-table :data="apps" max-height="650" table-layout="auto" stripe class="mb-4"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>没有应用数据</template>
      <el-table-column prop="id" label="应用ID" show-overflow-tooltip>
        <template #default="scope">
          <span class="d-flex align-items-center">
            <IconCopyCheck v-if="copied === `name#${scope.row.id}`" class="flex-shrink-0" size="16" />
            <IconCopy v-else class="flex-shrink-0" @click="copy(scope.row.id, scope.row.id)" size="16" />
            <span class="ml-2">{{ scope.row.id }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="应用名" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <el-table-column prop="description" label="应用描述" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.description }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="create_time" sortable="custom" width="200" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column width="360">
        <template #default="scope">
          <el-button type="primary" @click="edit(scope.row.id)" :disabled="!userStore.superAdmin">
            <IconEdit size="20" class="mr-2" />
            <span>编辑</span>
          </el-button>
          <el-button type="primary" plain @click="admin(scope.row)">
            <IconUserCog size="20" class="mr-2" />
            <span>管理员</span>
          </el-button>
          <el-dropdown trigger="click" placement="bottom-end" style="margin-left: 12px"
                       @command="c => handleCommand(c, scope.row)">
            <el-button color="#4F73D9" plain>
              <span>更多操作</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="activity">
                  <IconActivity size="20" class="mr-2" />
                  <span>活动记录</span>
                </el-dropdown-item>
                <el-dropdown-item command="remove" >
                  <IconTrash size="20" class="mr-2" />
                  <span>删除应用</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
  <add-app v-model="openAdd" @close="search" />
  <edit-app v-if="openEdit" v-model="openEdit" :id="app.id" @close="search" />
  <app-user v-if="openAppUser" v-model="openAppUser" v-bind="app" @close="search" />
</template>

<style scoped>
</style>