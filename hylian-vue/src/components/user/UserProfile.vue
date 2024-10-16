<script setup>
import { UserFilled } from '@element-plus/icons-vue'
import { ElAvatar, ElCol, ElPopover, ElRow } from 'element-plus'
import { useUserStore } from '@/store'
import { refreshUser } from '@/common/assortment'

const userStore = useUserStore()
</script>

<template>
  <el-popover :width="180" popper-class="user-profile-popper" @show="refreshUser(true)">
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
      <el-row>
        <el-col :span="9">角色:</el-col>
        <el-col :span="15">
          <span v-if="userStore.superAdmin">超级管理员</span>
          <span v-else>普通用户</span>
        </el-col>
      </el-row>
    </template>
  </el-popover>
</template>

<style scoped>
.user-profile-popper {
  box-shadow: rgb(14 18 22 / 35%) 0 10px 38px -10px, rgb(14 18 22 / 20%) 0 10px 20px -15px;
  padding: 20px;
}
</style>