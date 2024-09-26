<script setup>
import { ElRow } from 'element-plus'
</script>

<template>
  <el-row justify="center">
    <img src="@/assets/qr_code.png"  alt="二维码" class="wechat-code" />
  </el-row>
</template>

<style scoped>
.wechat-code {
  width: 150px;
  height: 150px;
  display: block;
}
</style>