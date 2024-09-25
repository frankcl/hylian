<script setup>
import Header from '@/views/Header'
import Aside from '@/views/Aside'
import Main from '@/views/Main'
import Footer from '@/views/Footer'
import { ElAside, ElContainer, ElFooter, ElHeader, ElMain } from 'element-plus'
</script>

<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <Header></Header>
    </el-header>
    <el-container>
      <el-aside class="layout-aside">
        <Aside></Aside>
      </el-aside>
      <el-main>
        <Main></Main>
      </el-main>
    </el-container>
    <el-footer class="layout-footer">
      <Footer></Footer>
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  border: #888888 solid 1px;
}
.layout-header {
  border-bottom: #888888 solid 1px;
}
.layout-aside {
  width: 150px;
  border-right: #888888 solid 1px;
}
.layout-footer {
  border-top: #888888 solid 1px;
}
</style>