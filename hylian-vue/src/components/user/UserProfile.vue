<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import { ElAvatar, ElCol, ElPopover, ElRow } from 'element-plus'
import { useUserStore } from '@/store'

const userStore = useUserStore()
</script>

<template>
  <el-popover :width="180" popper-style="box-shadow: rgb(14 18 22 / 35%) 0px 10px 38px -10px, rgb(14 18 22 / 20%) 0px 10px 20px -15px; padding: 20px;">
    <template #reference>
      <el-avatar shape="circle" :size="30" fit="cover"
                 :icon="UserFilled" :src="userStore.avatar"></el-avatar>
    </template>
    <template #default>
      <el-avatar shape="circle" :size="60" fit="cover" style="margin-bottom: 20px;"
                 :icon="UserFilled" :src="userStore.avatar"></el-avatar>
      <el-row>
        <el-col :span="9">用户名:</el-col>
        <el-col :span="15">{{ userStore.username }}</el-col>
      </el-row>
      <el-row>
        <el-col :span="9">名称:</el-col>
        <el-col :span="15">{{ userStore.name }}</el-col>
      </el-row>
      <el-row>
        <el-col :span="9">租户:</el-col>
        <el-col :span="15" v-if="userStore.tenant">{{ userStore.tenant.name }}</el-col>
      </el-row>
    </template>
  </el-popover>
</template>

<style scoped>

</style>