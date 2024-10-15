<script setup>
import { format } from 'date-fns'
import { reactive, ref, useTemplateRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowRight, Timer } from '@element-plus/icons-vue'
import {
  ElBreadcrumb, ElBreadcrumbItem, ElCol, ElForm, ElFormItem,
  ElIcon, ElPagination, ElRow, ElTable, ElTableColumn
} from 'element-plus'
import { fillSearchQuerySort, searchQueryToRequest } from '@/common/assortment'
import { asyncSearchActivities } from '@/common/service'
import AppSearch from '@/components/app/AppSearch'
import UserSearch from '@/components/user/UserSearch'

const route = useRoute()
const formRef = useTemplateRef('formRef')
const total = ref(0)
const activities = ref([])
const query = reactive({
  current: 1,
  size: 20,
  user_id: null,
  app_id: null,
  sort_field: null,
  sort_order: null
})

const search = async () => {
  const request = searchQueryToRequest(query)
  if (query.app_id) request.app_id = query.app_id
  if (query.user_id) request.user_id = query.user_id
  const pager = await asyncSearchActivities(request)
  if (!pager) return
  total.value = pager.total
  activities.value = pager.records
}

watch(() => route.query.appId, () => query.app_id = route.query.appId, { immediate: true, flush: 'post' })
watch(() => route.query.userId, () => query.user_id = route.query.userId, { immediate: true, flush: 'post' })
watch(query, () => search(), { immediate: true })
</script>

<template>
  <el-row>
    <el-breadcrumb :separator-icon="ArrowRight">
      <el-breadcrumb-item>应用管理</el-breadcrumb-item>
      <el-breadcrumb-item>活动记录</el-breadcrumb-item>
    </el-breadcrumb>
  </el-row>
  <div class="square-block">
    <el-form :model="query" ref="formRef" label-width="auto" style="max-width: 400px">
      <el-form-item label="所属应用" prop="app_id">
        <app-search :ignore-check="true" v-model="query.app_id"></app-search>
      </el-form-item>
      <el-form-item label="所属用户" prop="user_id">
        <el-col :span="14">
          <user-search v-model="query.user_id"></user-search>
        </el-col>
        <el-col :span="1"></el-col>
        <el-col :span="9"><a @click="formRef.resetFields(); search()">清除所有筛选条件</a></el-col>
      </el-form-item>
    </el-form>
  </div>
  <el-table :data="activities" max-height="850" table-layout="auto"
            stripe @sort-change="event => fillSearchQuerySort(event, query)">
    <template #empty>没有活动记录</template>
    <el-table-column prop="session_id" label="会话ID" show-overflow-tooltip />
    <el-table-column prop="app.name" label="应用名" width="200" show-overflow-tooltip />
    <el-table-column prop="user.name" label="用户名称" width="200" show-overflow-tooltip />
    <el-table-column label="登录时间" prop="create_time" sortable="custom" width="200" show-overflow-tooltip>
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
  </el-table>
  <el-row justify="center" align="middle">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="query.size" v-model:current-page="query.current" />
  </el-row>
</template>

<style scoped>
</style>