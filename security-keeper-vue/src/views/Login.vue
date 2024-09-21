<script setup>
const navItems = [
  {
    url: '/login/password',
    name: '密码登录'
  },
  {
    url: '/login/wechat',
    name: '微信登录'
  }
]

function selectNav(event) {
  const items = document.getElementsByClassName('nav-item')
  for (let i = 0; i < items.length; i++) {
    items[i].className = 'nav-item'
    if (items[i] === event.currentTarget.parentNode) items[i].className = 'nav-item nav-item-select'
  }
}
</script>

<template>
  <div class="canvas">
    <div class="container">
      <nav class="nav">
        <div class="nav-item" v-for="navItem in navItems" :key="navItem.name">
          <RouterLink :to="navItem.url" @click="selectNav">
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
  </div>
</template>

<style scoped>
.canvas {
  min-height: 100vh;
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
}
.container {
  width: 350px;
  display: flex;
  flex-direction: column;
}
.nav {
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
.nav-item-select {
  background-color: #676868;
}
.nav-item:not(:last-child) {
  margin-right: 1px;
}
</style>