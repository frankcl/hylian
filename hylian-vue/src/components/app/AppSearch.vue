<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchApp } from '@/common/AsyncRequest'

const props = defineProps({
  'placeholder': { default: '根据应用名搜索' },
  'ignoreCheck': { default: false }
})
const appId = defineModel()
const loading = ref(false)
const apps = ref([])

const search = async query => {
  loading.value = true
  try {
    const pager = await asyncSearchApp({ name: query, id: query, ignore_check: props.ignoreCheck })
    apps.value = pager.records
  } finally {
    loading.value = false
  }
}

onMounted(() => search(''))
</script>

<template>
  <el-select v-model="appId" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder || '根据应用名搜索'">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id" />
  </el-select>
</template>

<style scoped>

</style>