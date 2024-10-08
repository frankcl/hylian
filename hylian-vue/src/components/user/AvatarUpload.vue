<script setup>
import { computed, ref, watchEffect } from 'vue'
import { ElIcon, ElNotification, ElUpload } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { asyncUploadAvatar } from '@/common/service'

const props = defineProps(['width', 'height', 'avatar'])
const emits = defineEmits(['finish'])
const displayAvatar = ref()
const avatarSize = computed(() => { return { width: props.width + 'px', height: props.height + 'px' } })

const beforeUploadAvatar = rawFile => {
  if (rawFile.type !== 'image/jpeg') {
    ElNotification.error('头像图片必须是jpeg格式')
    return false
  } else if (rawFile.size > 2 * 1024 * 1024) {
    ElNotification.error('头像图片不能超过2MB')
    return false
  }
  return true
}

const uploadAvatar = async file => {
  const response = await asyncUploadAvatar(file)
  displayAvatar.value = response.signed_url
  emits('finish', response.oss_url)
}

watchEffect(() => displayAvatar.value = props.avatar && props.avatar !== '' ? props.avatar : undefined)
</script>

<template>
  <el-upload :http-request="uploadAvatar" :before-upload="beforeUploadAvatar" :show-file-list="false">
    <img v-if="displayAvatar" class="avatar" :style="avatarSize" :src="displayAvatar" alt="头像" />
    <el-icon v-else class="avatar-upload-icon" :style="avatarSize"><Plus /></el-icon>
  </el-upload>
</template>

<style scoped>
.avatar {
  display: block;
}
:deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
:deep(.el-upload):hover {
  border-color: var(--el-color-primary);
}
.avatar-upload-icon {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
}
</style>