<script setup>
import { IconRefresh } from '@tabler/icons-vue'
import { ref } from 'vue'
import { ElDialog, ElLink, ElRow } from 'element-plus'
import { asyncForceRefresh, asyncGenerateQRCode, asyncRefreshUser } from '@/common/AsyncRequest'
import { showMessage, SUCCESS } from '@/common/Feedback'

const props = defineProps(['id'])
const emits = defineEmits(['close'])
const open = defineModel()

let websocket = undefined
const error = ref(false)
const prompt = ref('')
const QRCode = ref()

const buildWebsocket = async key => {
  if (websocket !== undefined) websocket.close()
  websocket = new WebSocket(import.meta.env.VITE_WS_BASE_URL + '/api/ws/qrcode?key=' + key)
  websocket.onmessage = async function(event) {
    const response = JSON.parse(event.data)
    if (response.status === 1) prompt.value = '已扫码，正在绑定'
    else if (response.status === -1) {
      error.value = true
      prompt.value = '绑定异常：'+ response.message
      this.close()
    } else if (response.status === 4) {
      await asyncForceRefresh()
      await asyncRefreshUser()
      prompt.value = '成功绑定'
      showMessage('成功绑定', SUCCESS)
      this.close()
      open.value = false
    }
  }
}

const refreshQRCode = async () => {
  error.value = false
  prompt.value = '请用微信扫一扫'
  const response = await asyncGenerateQRCode({ category: 2, userid: props.id })
  QRCode.value = response.image
  await buildWebsocket(response.key)
}
</script>

<template>
  <el-dialog width="280" v-model="open" align-center show-close
             @close="emits('close')" @open="refreshQRCode">
    <el-row justify="center" class="mb-4"><span class="fs-m fw-500">绑定微信账号</span></el-row>
    <el-row justify="center" class="mb-4">
      <img v-if="QRCode" :src="QRCode" alt="微信小程序码" class="mini-code" />
    </el-row>
    <el-row class="bind-prompt" justify="center">
      <span>点击刷新</span>
      <el-link @click="refreshQRCode" :underline="false">
        <IconRefresh size="8" class="ml-1 cursor-pointer" />
      </el-link>
    </el-row>
    <el-row class="bind-prompt" justify="center">
      <span :class="{ 'error-prompt': error }">{{ prompt }}</span>
    </el-row>
  </el-dialog>
</template>

<style scoped>
.error-prompt {
  color: #FF4949;
}
.bind-prompt {
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