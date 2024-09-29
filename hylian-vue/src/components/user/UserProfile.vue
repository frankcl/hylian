<script setup>
import { computed } from 'vue'
import { ElCard, ElCol, ElRow } from 'element-plus'
import { useUserStore } from '@/store'

const props = defineProps({
  positionX: {
    type: Number,
    default: () => 0
  },
  positionY: {
    type: Number,
    default: () => 0
  },
  show: {
    type: Boolean,
    default: () => false
  }
})

const userStore = useUserStore()
const showStyle = computed(() => { return {
  left: props.positionX + 'px',
  top: props.positionY + 'px',
  display: props.show ? 'block' : 'none'
}})
</script>

<template>
  <el-card id="user-profile" :style="showStyle" class="user-profile">
    <template #header>
      <strong>用户信息</strong>
    </template>
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
  </el-card>
</template>

<style scoped>
.user-profile {
  position: fixed;
  width: 250px;
}
</style>