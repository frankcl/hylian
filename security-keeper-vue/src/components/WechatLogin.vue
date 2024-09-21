<script setup>
</script>

<template>
  <div class="wechat-login">
    <img src="@/assets/qr_code.png"  alt="二维码" class="wechat-code" />
  </div>
</template>

<style scoped>
.wechat-login {
  border: 1px solid #888888;
  border-top: none;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 250px;
}
.wechat-code {
  width: 150px;
  height: 150px;
  display: block;
}
</style>