<script setup>
import { ElRow } from 'element-plus'
</script>

<template>
  <el-row justify="center">
    <img src="@/assets/qr_code.png"  alt="二维码" class="wechat-code" />
  </el-row>
  <el-row class="wechat-login-prompt" justify="center">
    <span>请用微信扫一扫登录</span>
  </el-row>
</template>

<style scoped>
.wechat-login-prompt {
  margin-top: 23px;
  line-height: 1.0;
  font-size: 8px;
  height: 8px;
}
.wechat-code {
  width: 150px;
  height: 150px;
  display: block;
}
</style>