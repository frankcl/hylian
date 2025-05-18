<script setup>
import { ElContainer } from 'element-plus'
import Login from '@/views/home/Login'
</script>

<template>
  <el-container class="home">
    <el-container class="main">
      <Login></Login>
    </el-container>
  </el-container>
</template>

<style scoped>
.home {
  min-height: 100vh;
}
.main {
  align-items: center;
  justify-content: center;
  background-image: url("@/assets/background.jpg");
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
}
</style>