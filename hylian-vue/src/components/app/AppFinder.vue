<script setup>
import { ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchApps } from '@/common/service'

const props = defineProps(['placeholder'])
const model = defineModel()
const loading = ref(false)
const apps = ref([])

const search = async query => {
  loading.value = true
  try {
    const pager = await asyncSearchApps({name: query})
    apps.value = pager.records
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-select v-model="model" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder || '根据应用名称搜索'">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
  </el-select>
</template>

<style scoped>

</style>