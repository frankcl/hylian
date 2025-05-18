<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncGetApps } from '@/common/AsyncRequest'

const props = defineProps({
  placeholder: { default: '全部' },
  clearable: { default: true }
})
const emits = defineEmits(['change'])
const appId = defineModel()
const apps = ref([])
const appMap = new Map()

const handleChange = id => {
  const app = appMap.get(id)
  emits('change', JSON.parse(JSON.stringify(app)))
}

onMounted(async() => {
  apps.value = await asyncGetApps()
  apps.value.forEach(app => appMap.set(app.id, app))
})
</script>

<template>
  <el-select v-model="appId" @change="handleChange" filterable
             :clearable="props.clearable" :placeholder="props.placeholder">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id" />
  </el-select>
</template>

<style scoped>

</style>