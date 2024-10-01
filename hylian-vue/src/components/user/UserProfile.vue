<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import { ElAvatar, ElCol, ElPopover, ElRow } from 'element-plus'
import { useUserStore } from '@/store'

const userStore = useUserStore()
</script>

<template>
  <el-popover :width="300">
    <template #reference>
      <el-avatar :icon="UserFilled" shape="circle" size="small" fit="cover" :src="userStore.avatar"></el-avatar>
    </template>
    <template #default>
      <el-avatar :icon="UserFilled" shape="circle" size="small" fit="cover" :src="userStore.avatar"></el-avatar>
      <el-row>
        <el-col :span="10">用户名:</el-col>
        <el-col :span="14">{{ userStore.username }}</el-col>
      </el-row>
      <el-row>
        <el-col :span="10">用户姓名:</el-col>
        <el-col :span="14">{{ userStore.name }}</el-col>
      </el-row>
      <el-row>
        <el-col :span="10">所属租户:</el-col>
        <el-col :span="14" v-if="userStore.tenant">{{ userStore.tenant.name }}</el-col>
      </el-row>
    </template>
  </el-popover>
</template>

<style scoped>
</style>