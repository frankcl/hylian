<script setup>
import { ref } from 'vue'
import { ElIcon, ElNotification, ElUpload } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import { asyncUploadAvatar } from '@/common/service'

const props = defineProps({
  size: { default: 150 }
})
const model = defineModel()
const uploadPictureCardSize = ref(props.size + 'px')

const handleBeforeUpload = rawFile => {
  if (rawFile.type !== 'image/jpeg') {
    ElNotification.error('头像图片必须是jpeg格式')
    return false
  } else if (rawFile.size > 2 * 1024 * 1024) {
    ElNotification.error('头像图片不能超过2MB')
    return false
  }
  return true
}

const handleUpload = async file => model.value = await asyncUploadAvatar(file)

const handleRemove = () => model.value = null
</script>

<template>
  <div v-if="model && model !== ''" class="upload--picture-card">
    <img class="upload-list__item-thumbnail" :src="model" alt="用户头像" />
    <span class="upload-list__item-actions">
        <span class="upload-list__item-delete" @click="handleRemove">
          <el-icon><Delete /></el-icon>
        </span>
      </span>
  </div>
  <el-upload v-else class="upload--picture-card" :http-request="handleUpload"
             :before-upload="handleBeforeUpload" :show-file-list="false">
    <el-icon class="el-icon"><Plus /></el-icon>
  </el-upload>
</template>

<style scoped>
.upload--picture-card {
  --upload-picture-card-size: v-bind(uploadPictureCardSize);
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
  height: var(--upload-picture-card-size);
  width: var(--upload-picture-card-size);
}
.upload-list__item-thumbnail {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  object-fit: cover;
}
.upload-list__item-actions {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  left: 0;
  top: 0;
  cursor: default;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  color: #ffffff;
  opacity: 0;
  font-size: 20px;
  background-color: #0000007f;
  transition: opacity 0.3s
}
.upload-list__item-actions span {
  display: none;
  cursor: pointer
}
.upload-list__item-actions span+span {
  margin-left: 16px
}
.upload-list__item-actions:hover {
  opacity: 1
}
.upload-list__item-actions:hover span {
  display: inline-flex
}
.upload-list__item-delete {
  position: static;
  font-size: inherit;
  color: inherit
}
.el-icon {
  height: 1em;
  width: 1em;
  line-height: 1em;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  position: relative;
  fill: currentColor;
  color: #8c939d;
  font-size: 28px;
}
</style>