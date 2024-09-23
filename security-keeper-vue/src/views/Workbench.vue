<script setup>
import router from '@/router'
import { useUserStore } from '@/store'
import { logout } from '@/utils/backend'

const userStore = useUserStore()

async function executeLogout() {
  await logout()
  userStore.reset()
  await router.push('/')
}
</script>

<template>
  <div>
    <span>登录成功</span>&nbsp;<span>{{ userStore.username }}</span>
    <div>
      <a @click.prevent="executeLogout">注销</a>
    </div>
    <div>
      <RouterLink to="/home/passwordLogin">首页</RouterLink>
    </div>
  </div>
</template>

<style scoped>

</style>