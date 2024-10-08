<script setup>
import { ElContainer, ElHeader } from 'element-plus'
import Login from '@/views/login/Login'
</script>

<template>
  <el-container class="layout-home">
    <el-header class="layout-header"></el-header>
    <el-container class="layout-main">
      <Login></Login>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-home {
  min-height: 100vh;
}
.layout-header {
  height: 46px;
  background-image: url("@/assets/banner.jpg");
}
.layout-main {
  align-items: center;
  justify-content: center;
  background-image: url("@/assets/background.jpg");
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
}
</style>