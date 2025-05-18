<script setup>
import {
  IconActivity, IconApps, IconCircles,
  IconLockCheck, IconSitemap, IconUsers
} from '@tabler/icons-vue'
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeLink = ref(route.fullPath)
const handleClick = (path, query = undefined) => {
  activeLink.value = path
  if (query) {
    activeLink.value += '?'
    Object.keys(query).forEach((key, index) => {
      if (index > 0) activeLink.value += '&'
      activeLink.value += `${key}=${query[key]}`
    })
  }
  router.push({ path: path, query: query })
}
watch(() => route.fullPath, () => activeLink.value = route.fullPath)
</script>

<template>
  <div class="scroll-sidebar">
    <ul class="sidebar-menu">
      <li class="sidebar-menu-cap">账号管理</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/workbench/userList'}"
           @click="handleClick('/workbench/userList')">
          <IconUsers size="22" />
          <span>用户管理</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/workbench/tenantList'}"
           @click="handleClick('/workbench/tenantList')">
          <IconSitemap size="22" />
          <span>租户管理</span>
        </a>
      </li>
      <li class="sidebar-menu-cap">角色授权</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/workbench/roleList'}"
           @click="handleClick('/workbench/roleList')">
          <IconCircles size="22" />
          <span>角色管理</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/workbench/permissionList'}"
           @click="handleClick('/workbench/permissionList')">
          <IconLockCheck size="22" />
          <span>权限管理</span>
        </a>
      </li>
      <li class="sidebar-menu-cap">应用活动</li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink === '/workbench/appList'}"
           @click="handleClick('/workbench/appList')">
          <IconApps size="22" />
          <span>应用管理</span>
        </a>
      </li>
      <li class="sidebar-menu-item">
        <a class="sidebar-menu-link" :class="{ active: activeLink.startsWith('/workbench/activityList')}"
           @click="handleClick('/workbench/activityList')">
          <IconActivity size="22" />
          <span>活动记录</span>
        </a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.scroll-sidebar {
  padding: 0 24px;
  height: calc(100vh - 70px);
  overflow-y: auto;
}
.sidebar-menu {
  list-style: none;
}
.sidebar-menu-cap {
  margin-top: 24px;
  color: #2A3547;
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  line-height: 26px;
  text-transform: uppercase;
}
.sidebar-menu-link {
  color: #2A3547;
  font-size: 14px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  white-space: nowrap;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: start;
  -ms-flex-pack: start;
  justify-content: start;
  line-height: 25px;
  position: relative;
  margin: 0 0 2px;
  padding: 10px;
  border-radius: 7px;
  cursor: pointer;
  gap: 15px;
}
.active {
  background-color: var(--el-color-primary);
  color: #fff
}
.sidebar-menu-link:hover {
  background-color: rgba(93,135,255,0.1);
  color: var(--el-color-primary);
}
.sidebar-menu-link.active:hover {
  background-color: var(--el-color-primary);
  color: #fff
}
</style>