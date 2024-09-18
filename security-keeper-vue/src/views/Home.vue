<script setup>
import { useUserStore } from '@/store'
import { logout } from '@/utils/common.js'

const userStore = useUserStore()
</script>

<template>
  <div>
    <span>登录成功</span>&nbsp;<span>{{ userStore.username }}</span>
    <div>
      <a @click.prevent="logout">注销</a>
    </div>
  </div>
</template>

<style scoped>

</style>