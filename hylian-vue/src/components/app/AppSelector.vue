<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { fetchAllApps } from '@/common/assortment'

const props = defineProps({
  placeholder: { default: '全部' },
  clearable: { default: true }
})
const emits = defineEmits(['change'])
const model = defineModel()
const apps = ref([])

const handleChange = value => emits('change', value)

onMounted(async() => apps.value = await fetchAllApps(false))
</script>

<template>
  <el-select v-model="model" @change="handleChange" filterable :clearable="props.clearable"
             :placeholder="props.placeholder">
    <el-option v-for="app in apps" :key="app.id" :label="app.name" :value="app.id"></el-option>
  </el-select>
</template>

<style scoped>

</style>