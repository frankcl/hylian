<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

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
  <div class="login-box">
    <nav class="nav-box">
      <div class="nav-item" v-for="navItem in navItems" :key="navItem.name">
        <RouterLink :to="navItem.url">
          <strong>{{ navItem.name }}</strong>
        </RouterLink>
      </div>
    </nav>
    <RouterView v-slot="{ Component }">
      <keep-alive>
        <component :is="Component"></component>
      </keep-alive>
    </RouterView>
  </div>
</template>

<style scoped>
.login-box {
  width: 350px;
  display: flex;
  flex-direction: column;
}
.nav-box {
  height: 55px;
  display: flex;
  flex-flow: row nowrap;
}
.nav-item {
  font-weight: bold;
  flex: 1 1 auto;
  align-content: center;
  background-color: #888888;
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
  text-align: center;
  padding: 5px;
}
.nav-item:not(:last-child) {
  margin-right: 1px;
}
.nav-item-selected {
  background-color: #676868;
}
</style>