<script setup>
import LoginBox from '@/components/LoginBox'
import { ElRow } from 'element-plus'
</script>

<template>
  <el-row class="layout-home" align="middle" justify="center">
    <LoginBox></LoginBox>
  </el-row>
</template>

<style scoped>
.layout-home {
  min-height: 100vh;
}
</style>