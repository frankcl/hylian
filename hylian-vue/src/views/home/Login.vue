<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElCol, ElRow } from 'element-plus'

const route = useRoute()
const router = useRouter()
const currentPath = ref(route.path)

const handleChange = path => {
  currentPath.value = path
  if (route.query.redirect) router.push({ path: path, query: { redirect: route.query.redirect }})
  else router.push({ path: path })
}
</script>

<template>
  <div class="login-card">
    <el-row class="brand-logo" justify="center" align="middle">
      <img src="@/assets/logo.jpg" style="height: 60px" alt="logo" />
    </el-row>
    <el-row class="nav">
      <el-col class="nav-item" :class="{ 'nav-item-selected': currentPath === '/home/wechatLogin' }" :span="12">
          <el-link class="nav-link" :underline="false" @click="handleChange('/home/wechatLogin')">
            <span class="fs-m fw-500">微信登录</span>
          </el-link>
      </el-col>
      <el-col class="nav-item" :class="{ 'nav-item-selected': currentPath === '/home/passwordLogin' }" :span="12">
        <el-link class="nav-link" :underline="false" @click="handleChange('/home/passwordLogin')">
          <span class="fs-m fw-500">密码登录</span>
        </el-link>
      </el-col>
    </el-row>
    <div class="login-content">
      <RouterView v-slot="{ Component }">
        <keep-alive><component :is="Component" /></keep-alive>
      </RouterView>
    </div>
  </div>
</template>

<style scoped>
.login-card {
  padding: 30px 25px;
  background-color: #ffffff;
  border: none;
  border-radius: 15px;
  box-shadow: 0 0 5px #8fb8e2;
  display: flex;
  flex-direction: column;
  flex: 0 1 350px;
  height: 450px;
}
.brand-logo {
  margin-bottom: 15px;
  height: 60px;
}
.nav {
  padding: 10px 0;
  margin-bottom: 30px;
  height: 45px;
}
.nav-item {
  display: flex;
  align-items: start;
  justify-content: center;
  flex: 1 1 auto;
  height: 35px;
  border-bottom: #bfbfbf solid 1px;
}
.nav-item:not(:last-child) {
  margin-right: 10px;
}
.nav-item-selected {
  border-bottom: var(--el-color-primary) solid 1px;
}
.nav-item-selected .nav-link {
  color: var(--el-color-primary);
}
.nav-link {
  color: #bfbfbf;
}
.nav-link:focus,.nav-link:hover {
  color: var(--el-color-primary);
}
.login-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
</style>