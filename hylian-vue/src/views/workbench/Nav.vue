<script setup>
import { IconChevronDown, IconMenu2 } from '@tabler/icons-vue'
import { onMounted, ref } from 'vue'
import { ElAvatar, ElDropdown, ElDropdownItem, ElDropdownMenu, ElLink, ElText } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store'
import { asyncLogout, asyncRefreshUser, asyncUnbindWechat } from '@/common/AsyncRequest'
import { ERROR, showMessage, SUCCESS } from '@/common/Feedback'
import ImageAvatar from '@/assets/avatar.jpg'
import BindWechat from '@/views/user/BindWechat'
import EditUser from '@/views/user/EditUser'
import UpdatePassword from '@/views/user/UpdatePassword'

const emits = defineEmits(['showSidebar'])
const userStore = useUserStore()
const openBind = ref(false)
const openEdit = ref(false)
const openPassword = ref(false)

const handleCommand = async command => {
  if (command === 'edit') openEdit.value = true
  else if (command === 'password') openPassword.value = true
  else if (command === 'bind') openBind.value = true
  else if (command === 'logout') {
    await asyncLogout()
    userStore.$reset()
    await router.push('/')
  }
  else if (command === 'unbind') {
    if (!await asyncUnbindWechat({ id: userStore.id })) {
      showMessage('解绑微信账号失败', ERROR)
      return
    }
    showMessage('解绑微信账号成功', SUCCESS)
    await asyncRefreshUser()
  }
}

onMounted(() => asyncRefreshUser())
</script>

<template>
  <nav class="navbar">
    <ul class="navbar-nav">
      <li class="d-xl-none nav-item">
        <el-link class="nav-link nav-icon-hover" :underline="false" @click="emits('showSidebar')">
          <IconMenu2 />
        </el-link>
      </li>
    </ul>
    <div class="flex-grow-1 d-flex justify-content-end">
      <ul class="navbar-nav">
        <li class="dropdown">
          <el-dropdown trigger="click" @command="handleCommand">
            <div ref="navUser" class="d-flex navbar-user align-items-center">
              <el-link class="nav-link" :underline="false">
                <el-avatar shape="circle" fit="cover" :src="userStore.avatar ? userStore.avatar : ImageAvatar" />
                <div class="flex-grow-1 ml-3">
                  <span class="d-block fs-m fw-500 mb-1">{{ userStore.name }}</span>
                  <el-text v-if="userStore.superAdmin" class="fs-xs">超级管理员</el-text>
                  <el-text class="fs-xs" v-else>普通用户</el-text>
                </div>
                <IconChevronDown class="ml-3" size="14" />
              </el-link>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑用户</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item v-if="userStore.openid" command="unbind">解绑微信</el-dropdown-item>
                <el-dropdown-item v-else command="bind">绑定微信</el-dropdown-item>
                <el-dropdown-item command="logout">退出系统</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </li>
      </ul>
    </div>
  </nav>
  <update-password v-model="openPassword" />
  <edit-user v-model="openEdit" :id="userStore.id" />
  <bind-wechat v-model="openBind" :id="userStore.id" />
</template>

<style scoped>
.navbar {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -ms-flex-wrap: nowrap;
  flex-wrap: nowrap;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: justify;
  -ms-flex-pack: justify;
  justify-content: space-between;
  position: relative;
  min-height: 70px;
}
.navbar-nav {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-orient: horizontal;
  -webkit-box-direction: normal;
  -ms-flex-direction: row;
  flex-direction: row;
  padding-left: 0;
  margin-bottom: 0;
  list-style: none;
}
.navbar-user {
  min-width: 12rem;
  text-align: left;
}
.nav-link {
  padding: 8px 16px;
  height: 70px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  position: relative;
  z-index: 2;
}
.nav-link:focus, .nav-link:hover {
  color: var(--el-color-primary);
}
.nav-icon-hover:hover:before {
  content: "";
  position: absolute;
  left: 50%;
  top: 50%;
  -webkit-transform: translate(-50%,-50%);
  transform: translate(-50%,-50%);
  height: 40px;
  width: 40px;
  z-index: -1;
  border-radius: 100px;
  -webkit-transition: all 0.3s ease-in-out;
  transition: all 0.3s ease-in-out;
  background-color: #ecf2ff
}
@media (min-width: 1280px) {
  .d-xl-none {
    display: none !important;
  }
}
</style>