<script setup>
import { IconPlus, IconTrash } from '@tabler/icons-vue'
import { computed } from 'vue'
import { ElUpload } from 'element-plus'
import { showMessage, WARNING } from '@/common/Feedback'
import { asyncUploadAvatar } from '@/common/AsyncRequest'

const props = defineProps({
  size: { default: 150 }
})
const emits = defineEmits(['remove'])
const path = defineModel()
const pictureSize = computed(() => props.size + 'px')

const handleBeforeUpload = rawFile => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    showMessage('仅支持jpeg和png格式图片', WARNING)
    return false
  } else if (rawFile.size > 2 * 1024 * 1024) {
    showMessage('图片大小不超过2MB', WARNING)
    return false
  }
  return true
}

const handleUpload = async file => path.value = await asyncUploadAvatar(file)

const handleRemove = () => emits('remove')
</script>

<template>
  <div v-if="path" class="avatar-box">
    <img class="avatar-image" :src="path" alt="用户头像" />
    <span class="avatar-image-actions">
      <IconTrash size="32" @click="handleRemove"/>
    </span>
  </div>
  <el-upload v-else class="avatar-box" :http-request="handleUpload"
             :before-upload="handleBeforeUpload" :show-file-list="false">
    <IconPlus color="#8c939d" size="32" />
  </el-upload>
</template>

<style scoped>
.avatar-box {
  --avatar-size: v-bind(pictureSize);
  position: relative;
  align-items: center;
  justify-content: center;
  vertical-align: top;
  background-color: #fafafa;
  border: 1px dashed #cdd0d6;
  border-radius: 6px;
  box-sizing: border-box;
  cursor: pointer;
  display: inline-flex;
  width: var(--avatar-size);
  height: var(--avatar-size);
}
.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  object-fit: cover;
}
.avatar-image-actions {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  left: 0;
  top: 0;
  cursor: pointer;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  color: #ffffff;
  opacity: 0;
  background-color: #0000007f;
  transition: opacity 0.3s
}
.avatar-image-actions span {
  display: none;
  cursor: pointer
}
.avatar-image-actions span+span {
  margin-left: 16px
}
.avatar-image-actions:hover {
  opacity: 1
}
.avatar-image-actions:hover span {
  display: inline-flex
}
</style>