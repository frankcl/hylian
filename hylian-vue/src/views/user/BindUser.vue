<script setup>
import { ref } from 'vue'
import {ElDialog, ElIcon, ElNotification, ElRow} from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { asyncForceRefresh, asyncGenerateQRCode } from '@/common/service'
import { refreshUser } from '@/common/assortment'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const model = defineModel()

let websocket = undefined
const error = ref(false)
const prompt = ref('请用微信扫一扫绑定账号')
const QRCode = ref()
const refreshQRCode = async () => {
  error.value = false
  prompt.value = '请用微信扫一扫绑定账号'
  const response = await asyncGenerateQRCode({ category: 2, userid: props.id })
  QRCode.value = response.image
  if (websocket !== undefined) websocket.close()
  websocket = new WebSocket(import.meta.env.VITE_WS_BASE_URL + '/api/ws/qrcode?key=' + response.key)
  websocket.onmessage = async function (event) {
    const obj = JSON.parse(event.data)
    if (obj.status === 1) prompt.value = '已扫码，等待绑定'
    else if (obj.status === -1) {
      error.value = true
      prompt.value = '绑定异常：'+ obj.message
      this.close()
    } else if (obj.status === 4) {
      await asyncForceRefresh()
      await refreshUser(true)
      prompt.value = '成功绑定'
      ElNotification.success('成功绑定')
      this.close()
      emits('close')
    }
  }
}
</script>

<template>
  <el-dialog width="250" v-model="model" @close="emits('close')" @open="refreshQRCode()" align-center show-close>
    <el-row justify="center">
      <img v-if="QRCode" :src="QRCode" alt="微信小程序码" class="wechat-code" />
    </el-row>
    <el-row class="wechat-bind-prompt" justify="center">
      <span>刷新小程序码</span>
      <a @click="refreshQRCode()"><el-icon><Refresh /></el-icon></a>
    </el-row>
    <el-row class="wechat-bind-prompt" justify="center">
      <span v-if="error" style="color: #FF0000">{{ prompt }}</span>
      <span v-else>{{ prompt }}</span>
    </el-row>
  </el-dialog>
</template>

<style scoped>
.wechat-bind-prompt {
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