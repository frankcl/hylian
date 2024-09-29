<script setup>
import {onMounted, ref, watchEffect} from 'vue'
import {
  ElButton,
  ElDialog,
  ElIcon, ElMessageBox,
  ElNotification,
  ElPagination,
  ElRow,
  ElSpace,
  ElTable,
  ElTableColumn
} from 'element-plus'
import AddUser from '@/components/user/AddUser'
import {deleteUser, searchUsers} from '@/utils/hylian-service'
import {Timer} from "@element-plus/icons-vue";
import {format} from "date-fns";

const addUserVisible = ref(false)
const users = ref([])
const total = ref(7)
const currentPage = ref(1)
const pageSize = ref(20)

const getUserList = async () => {
  const pager = await searchUsers({
    current: currentPage.value,
    size: pageSize.value
  })
  if (!pager) return
  users.value = pager.records
  total.value = pager.total
}

const deleteOneUser = async (id) => {
  ElMessageBox.confirm(
    '是否确定删除用户信息？',
    '删除提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    if (!await deleteUser(id)) {
      ElNotification.error('删除用户失败')
      return
    }
    ElNotification.success('删除用户成功')
    await getUserList()
  })
}

watchEffect(() => {
  getUserList(currentPage.value, pageSize.value)
})
</script>

<template>
  <el-button @click="addUserVisible=true">添加用户</el-button>
  <el-dialog v-model="addUserVisible" title="新用户注册" align-center show-close>
    <AddUser></AddUser>
  </el-dialog>
  <el-table :data="users" style="width: 100%" max-height="500" table-layout="auto">
    <el-table-column prop="user_name" label="用户名" />
    <el-table-column prop="name" label="真实姓名" />
    <el-table-column prop="tenant.name" label="所属租户" />
    <el-table-column label="创建时间">
      <template #default="scope">
        <el-icon><timer /></el-icon>
        {{ format(new Date(scope.row['create_time']), 'yyyy-MM-dd HH:mm:ss') }}
      </template>
    </el-table-column>
    <el-table-column fixed="right" label="操作">
      <template #default="scope">
        <el-button>编辑</el-button>
        <el-button @click="deleteOneUser(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-row justify="center" align="middle" style="margin-top: 20px;">
    <el-pagination background layout="prev, pager, next" :total="total"
                   v-model:page-size="pageSize" v-model:current-page="currentPage"/>
  </el-row>
</template>

<style scoped>

</style>