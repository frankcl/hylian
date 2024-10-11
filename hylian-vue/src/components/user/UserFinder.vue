<script setup>
import { ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchUsers } from '@/common/service'

const props = defineProps(['placeholder'])
const model = defineModel()
const loading = ref(false)
const users = ref([])

const search = async query => {
  loading.value = true
  try {
    const pager = await asyncSearchUsers({name: query})
    users.value = pager.records
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-select v-model="model" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder || '根据用户名称搜索'">
    <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id"></el-option>
  </el-select>
</template>

<style scoped>

</style>