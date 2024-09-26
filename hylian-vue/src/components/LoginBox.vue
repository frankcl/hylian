<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElCol, ElContainer, ElRow } from 'element-plus'

const navItems = [
  {
    url: '/home/passwordLogin',
    name: '密码登录'
  },
  {
    url: '/home/wechatLogin',
    name: '微信登录'
  }
]

const route = useRoute()

function stroke() {
  const items = document.getElementsByClassName('nav-item')
  for (let i = 0; i < items.length; i++) {
    items[i].className = 'nav-item'
    const child = items[i].firstElementChild
    if (child['href'].endsWith(route.path)) items[i].className = 'nav-item nav-item-selected'
  }
}

watch(() => route.path, stroke)
onMounted(stroke)

</script>

<template>
  <el-container class="login-box" direction="vertical">
    <el-row class="login-nav" :gutter="1" justify="space-evenly" align="middle">
      <el-col :span="12" v-for="navItem in navItems" :key="navItem.name">
        <el-row justify="center">
          <RouterLink :to="navItem.url">
            <strong>{{ navItem.name }}</strong>
          </RouterLink>
        </el-row>
      </el-col>
    </el-row>
    <el-row class="login-main">
      <RouterView v-slot="{ Component }">
        <keep-alive>
          <component :is="Component"></component>
        </keep-alive>
      </RouterView>
    </el-row>
  </el-container>
</template>

<style scoped>
.login-box {
  border: #888888 solid 1px;
  flex: 0 1 350px;
}
.login-nav {
  border-bottom: #888888 solid 1px;
  height: 55px;
}
.login-main {
  height: 260px;
  justify-content: center;
  flex-direction: column;
}
</style>