<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncGetTenants } from '@/common/AsyncRequest'

const props = defineProps({
  placeholder: { default: '全部' },
  clearable: { default: true },
  disable: { default: false }
})
const emits = defineEmits(['change'])
const tenantId = defineModel()
const tenants = ref([])
const tenantMap = new Map()

const handleChange = id => {
  const tenant = tenantMap.get(id)
  emits('change', JSON.parse(JSON.stringify(tenant)))
}

onMounted(async() => {
  tenants.value = await asyncGetTenants()
  tenants.value.forEach(tenant => tenantMap.set(tenant.id, tenant))
})
</script>

<template>
  <el-select v-model="tenantId" @change="handleChange" filterable :disabled="disable"
             :clearable="props.clearable" :placeholder="props.placeholder">
    <el-option v-for="tenant in tenants" :key="tenant.id" :label="tenant.name" :value="tenant.id" />
  </el-select>
</template>

<style scoped>

</style>