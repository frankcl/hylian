<script setup>
import { IconRefresh } from '@tabler/icons-vue'
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElLink, ElRow } from 'element-plus'
import {
  asyncGenerateQRCode,
  asyncRefreshUser,
  asyncWechatLogin
} from '@/common/AsyncRequest'
import { afterLogin } from '@/views/home/common'

let websocket = undefined
const route = useRoute()
const error = ref(false)
const prompt = ref('')
const QRCode = ref()

const buildWebsocket = async key => {
  if (websocket !== undefined) websocket.close()
  websocket = new WebSocket(import.meta.env.VITE_WS_BASE_URL + '/api/ws/qrcode?key=' + key)
  websocket.onmessage = async function(event) {
    const response = JSON.parse(event.data)
    if (response.status === 1) prompt.value = '已扫码，正在授权'
    else if (response.status === -1) {
      error.value = true
      prompt.value = '登录异常：'+ response.message
      this.close()
    } else if (response.status === 2 || response.status === 3) {
      prompt.value = response.status === 2 ? '授权成功' : '注册成功'
      if (!await asyncWechatLogin(response.key)) return
      await asyncRefreshUser()
      await afterLogin(route)
      this.close()
    }
  }
}

const refreshQRCode = async () => {
  error.value = false
  prompt.value = '请用微信扫一扫登录'
  const response = await asyncGenerateQRCode({ category: 1 })
  QRCode.value = response.image
  await buildWebsocket(response.key)
}

onMounted(() => refreshQRCode())
</script>

<template>
  <el-row justify="center" class="mb-4">
    <img v-if="QRCode" :src="QRCode" alt="微信小程序码" class="mini-code" />
  </el-row>
  <el-row class="login-prompt" justify="center">
    <span>点击刷新</span>
    <el-link @click="refreshQRCode" :underline="false">
      <IconRefresh size="10" class="ml-1 cursor-pointer" />
    </el-link>
  </el-row>
  <el-row class="login-prompt" justify="center">
    <span :class="{ 'error-prompt': error }">{{ prompt }}</span>
  </el-row>
</template>

<style scoped>
.error-prompt {
  color: #FF4949;
}
.login-prompt {
  line-height: 1.0;
  font-size: 10px;
  height: 10px;
  margin-bottom: 10px;
}
.mini-code {
  width: 180px;
  height: 180px;
  display: block;
}
</style>