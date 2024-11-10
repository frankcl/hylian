<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElIcon, ElRow } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { asyncCurrentUser, asyncGenerateQRCode, asyncWechatLogin } from '@/common/service'

let websocket = undefined
const router = useRouter()
const userStore = useUserStore()
const error = ref(false)
const prompt = ref('请用微信扫一扫登录')
const QRCode = ref()
const refreshQRCode = async () => {
  error.value = false
  prompt.value = '请用微信扫一扫登录'
  const response = await asyncGenerateQRCode({ category: 1 })
  QRCode.value = response.image
  if (websocket !== undefined) websocket.close()
  websocket = new WebSocket(import.meta.env.VITE_WS_BASE_URL + '/api/ws/qrcode?key=' + response.key)
  websocket.onmessage = async function (event) {
    const obj = JSON.parse(event.data)
    if (obj.status === 1) prompt.value = '已扫码，等待授权'
    else if (obj.status === -1) {
      error.value = true
      prompt.value = '登录异常：'+ obj.message
      this.close()
    } else if (obj.status === 2) {
      prompt.value = '授权成功'
      this.close()
      if (!await asyncWechatLogin(response.key)) return
      const user = await asyncCurrentUser()
      userStore.inject(user)
      await router.push('/workbench')
    }
  }
}

onMounted(() => refreshQRCode())
</script>

<template>
  <el-row justify="center">
    <img v-if="QRCode" :src="QRCode" alt="微信小程序码" class="wechat-code" />
  </el-row>
  <el-row class="wechat-login-prompt" justify="center">
    <span>刷新小程序码</span>
    <a @click="refreshQRCode()"><el-icon><Refresh /></el-icon></a>
  </el-row>
  <el-row class="wechat-login-prompt" justify="center">
    <span v-if="error" style="color: #FF0000">{{ prompt }}</span>
    <span v-else>{{ prompt }}</span>
  </el-row>
</template>

<style scoped>
.wechat-login-prompt {
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