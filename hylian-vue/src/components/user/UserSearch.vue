<script setup>
import { onMounted, ref } from 'vue'
import { ElOption, ElSelect } from 'element-plus'
import { asyncSearchUser } from '@/common/AsyncRequest'

const props = defineProps(['placeholder'])
const userId = defineModel()
const loading = ref(false)
const users = ref([])

const search = async query => {
  loading.value = true
  try {
    const pager = await asyncSearchUser({ name: query })
    users.value = pager.records
  } finally {
    loading.value = false
  }
}

onMounted(async () => await search())
</script>

<template>
  <el-select v-model="userId" clearable filterable remote :remote-method="search"
             :loading="loading" loading-text="搜索中 ..." :placeholder="props.placeholder || '根据用户昵称搜索'">
    <el-option v-for="user in users" :key="user.id" :label="user.name" :value="user.id" />
  </el-select>
</template>

<style scoped>

</style>