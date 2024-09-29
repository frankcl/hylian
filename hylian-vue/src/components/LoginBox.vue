<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElCol, ElContainer, ElRow } from 'element-plus'

const route = useRoute()

function stroke() {
  const items = document.getElementsByName('nav-item')
  for (let i = 0; i < items.length; i++) {
    items[i].className = 'login-nav-item'
    const aElement = items[i].querySelector('a')
    if (aElement['href'].endsWith(route.path)) {
      items[i].className += ' login-nav-item-selected'
    }
  }
}

watch(() => route.path, stroke)
onMounted(stroke)
</script>

<template>
  <el-container class="login-box" direction="vertical">
    <el-row class="login-box-logo" justify="center" align="middle">
      <img src="@/assets/logo_blue.png" style="height: 27px;" alt="logo" />
    </el-row>
    <el-row class="login-box-nav">
      <el-col class="login-nav-item" name="nav-item" :span="12">
        <el-row justify="center">
          <RouterLink to="/home/passwordLogin"><strong>密码登录</strong></RouterLink>
        </el-row>
      </el-col>
      <el-col class="login-nav-item" name="nav-item" :span="12">
        <el-row justify="center">
          <RouterLink to="/home/wechatLogin"><strong>微信登录</strong></RouterLink>
        </el-row>
      </el-col>
    </el-row>
    <el-container class="login-container" direction="vertical">
      <RouterView v-slot="{ Component }">
        <keep-alive>
          <component :is="Component"></component>
        </keep-alive>
      </RouterView>
    </el-container>
  </el-container>
</template>

<style scoped>
.login-box {
  padding: 31px 24px;
  background-color: #ffffff;
  border: none;
  border-radius: 15px;
  box-shadow: 0 0 5px #8fb8e2;
  flex: 0 1 276px;
  height: 368px;
}
.login-box-logo {
  margin-bottom: 9px;
  height: 27px;
  font-size: 14pt;
  font-weight: bolder;
}
.login-box-nav {
  padding: 10px 0;
  margin-bottom: 23px;
  height: 41px;
}
.login-nav-item {
  display: flex;
  align-items: start;
  justify-content: center;
  flex: 1 1 auto;
  height: 31px;
  border-bottom: #bfbfbf solid 1px;
}
.login-nav-item:not(:last-child) {
  margin-right: 12px;
}
.login-nav-item a {
  color: #bfbfbf;
}
.login-nav-item-selected {
  border-bottom: #2c5aff solid 1px;
}
.login-nav-item-selected a {
  color: #2c5aff;
}
.login-container {
  justify-content: center;
}
</style>