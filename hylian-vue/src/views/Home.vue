<script setup>
import LoginBox from '@/components/LoginBox'
import { ElContainer, ElHeader } from 'element-plus'
</script>

<template>
  <el-container class="--hy-layout-home">
    <el-header class="--hy-layout-home-header"></el-header>
    <el-container class="--hy-layout-home-main">
      <LoginBox></LoginBox>
    </el-container>
  </el-container>
</template>

<style scoped>
.--hy-layout-home {
  min-height: 100vh;
}
.--hy-layout-home-header {
  height: 46px;
  background-image: url("@/assets/banner.jpg");
}
.--hy-layout-home-main {
  align-items: center;
  justify-content: center;
  background-image: url("@/assets/background.jpg");
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
}
</style>