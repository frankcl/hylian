<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { fetchAllTenants } from '@/common/assortment'

const props = defineProps({
  placeholder: { default: '全部' },
  clearable: { default: true }
})
const emits = defineEmits(['change'])
const model = defineModel()
const tenants = ref([])

const handleChange = value => emits('change', value)

onMounted(async() => tenants.value = await fetchAllTenants(false))
</script>

<template>
  <el-select v-model="model" @change="handleChange" filterable :clearable="props.clearable"
             :placeholder="props.placeholder">
    <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id"></el-option>
  </el-select>
</template>

<style scoped>

</style>