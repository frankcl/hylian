import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'
import { showMessage, WARNING } from '@/common/Feedback'
import { checkLogin } from '@/common/Permission'
import Home from '@/views/home/Main'
import Workbench from '@/views/workbench/Main'
import PasswordLogin from '@/views/home/PasswordLogin.vue'
import WechatLogin from '@/views/home/WechatLogin.vue'
import UserList from '@/views/user/UserList'
import TenantList from '@/views/tenant/TenantList'
import RoleList from '@/views/role/RoleList'
import PermissionList from '@/views/permission/PermissionList'
import AppList from '@/views/app/AppList'
import ActivityList from '@/views/activity/ActivityList'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    redirect: '/home/wechatLogin',
    component: Home,
    children: [
      {
        path: 'passwordLogin',
        name: 'PasswordLogin',
        component: PasswordLogin
      },
      {
        path: 'wechatLogin',
        name: 'WechatLogin',
        component: WechatLogin
      }
    ]
  },
  {
    path: '/workbench',
    name: 'Workbench',
    redirect: '/workbench/userList',
    component: Workbench,
    meta: { auth: true },
    children: [
      {
        path: 'userList',
        name: 'UserList',
        component: UserList
      },
      {
        path: 'tenantList',
        name: 'TenantList',
        component: TenantList
      },
      {
        path: 'roleList',
        name: 'RoleList',
        component: RoleList
      },
      {
        path: 'permissionList',
        name: 'PermissionList',
        component: PermissionList
      },
      {
        path: 'appList',
        name: 'AppList',
        component: AppList
      },
      {
        path: 'activityList',
        name: 'ActivityList',
        component: ActivityList
      },
    ]
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async routeLocation => {
  if (routeLocation.meta.auth && !checkLogin()) {
    useUserStore().$reset()
    showMessage('尚未登录', WARNING)
    return '/'
  } else if (!routeLocation.meta.auth && checkLogin()) {
    return '/workbench'
  }
})

export default router