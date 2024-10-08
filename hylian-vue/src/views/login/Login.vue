<script setup>
import { onMounted, useTemplateRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElCol, ElContainer, ElRow } from 'element-plus'

const passwordNavRef = useTemplateRef('passwordNavRef')
const wechatNavRef = useTemplateRef('wechatNavRef')
const route = useRoute()

function highlight() {
  [ passwordNavRef, wechatNavRef ].forEach(navRef => {
    navRef.value.$el.className = 'login-nav-item'
    const link = navRef.value.$el.querySelector('a')
    if (link.href.endsWith(route.path)) {
      navRef.value.$el.className += ' login-nav-item-selected'
    }
  })
}

watch(() => route.path, highlight)
onMounted(highlight)
</script>

<template>
  <el-container class="login-box" direction="vertical">
    <el-row class="login-box-logo" justify="center" align="middle">
      <img src="@/assets/logo_blue.png" style="height: 27px;" alt="logo" />
    </el-row>
    <el-row class="login-box-nav">
      <el-col class="login-nav-item" ref="passwordNavRef" :span="12">
        <el-row justify="center">
          <RouterLink :to="{ name: 'PasswordLogin' }"><strong>密码登录</strong></RouterLink>
        </el-row>
      </el-col>
      <el-col class="login-nav-item" ref="wechatNavRef" :span="12">
        <el-row justify="center">
          <RouterLink :to="{ name: 'WechatLogin' }"><strong>微信登录</strong></RouterLink>
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