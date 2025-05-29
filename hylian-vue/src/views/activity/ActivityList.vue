<script setup>
import { IconClearAll, IconClock } from '@tabler/icons-vue'
import { reactive, ref, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElButton, ElCol, ElConfigProvider,
  ElForm, ElFormItem, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { formatDate } from '@/common/Time'
import {
  asyncSearchActivity,
  changeSearchQuerySort,
  newSearchQuery,
  newSearchRequest
} from '@/common/AsyncRequest'
import HylianCard from '@/components/data/Card'
import TableHead from '@/components/data/TableHead'
import AppSearch from '@/components/app/AppSearch'
import UserSearch from '@/components/user/UserSearch'

const route = useRoute()
const total = ref(0)
const activities = ref([])
const query = reactive(newSearchQuery({ user_id: null, app_id: null }))

const clear = () => query.user_id = query.app_id = null

const search = async () => {
  const request = newSearchRequest(query)
  if (query.app_id) request.app_id = query.app_id
  if (query.user_id) request.user_id = query.user_id
  const pager = await asyncSearchActivity(request)
  total.value = pager.total
  activities.value = pager.records
}

watch(() => route.query.app_id, () => query.app_id = route.query.app_id, { immediate: true })
watch(() => route.query.user_id, () => query.user_id = route.query.user_id, { immediate: true })
watchEffect(async () => await search())
</script>

<template>
  <hylian-card>
    <template #title>
      <el-breadcrumb>
        <el-breadcrumb-item>应用活动</el-breadcrumb-item>
        <el-breadcrumb-item>活动记录</el-breadcrumb-item>
      </el-breadcrumb>
    </template>
    <el-form :model="query" ref="form" label-width="80px" class="mb-4">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属应用" prop="app_id">
            <app-search :ignore-check="true" v-model="query.app_id" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属用户" prop="user_id">
            <user-search v-model="query.user_id" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-button type="primary" plain @click="clear">
            <IconClearAll size="20" class="mr-1" />
            <span>清除筛选</span>
          </el-button>
        </el-col>
      </el-row>
    </el-form>
    <table-head title="活动记录列表" />
    <el-table :data="activities" max-height="550" table-layout="auto" stripe class="mb-4"
              @sort-change="e => changeSearchQuerySort(e.prop, e.order, query)">
      <template #empty>暂无活动记录</template>
      <el-table-column prop="session_id" label="会话ID" show-overflow-tooltip />
      <el-table-column prop="app.name" label="应用名" show-overflow-tooltip />
      <el-table-column prop="user.name" label="用户昵称" show-overflow-tooltip />
      <el-table-column label="登录时间" prop="create_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['create_time']) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" sortable="custom" show-overflow-tooltip>
        <template #default="scope">
          <div class="d-flex align-items-center">
            <IconClock size="16" class="mr-1" />
            <span>{{ formatDate(scope.row['update_time']) }}</span>
          </div>
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
</template>

<style scoped>
</style>