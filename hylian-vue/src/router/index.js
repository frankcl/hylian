import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { isLogin, refreshUser } from '@/common/assortment'
import Home from '@/views/Home'
import Workbench from '@/views/Workbench'
import PasswordLogin from '@/views/login/PasswordLogin'
import WechatLogin from '@/views/login/WechatLogin'
import UserList from '@/views/user/UserList'
import TenantList from '@/views/tenant/TenantList'
import RoleList from '@/views/role/RoleList'
import PermissionList from '@/views/permission/PermissionList'
import AppList from '@/views/app/AppList'
import ActivityList from '@/views/activity/ActivityList'

const routes = [
  {
    path: '/',
    redirect: '/home/passwordLogin' },
  {
    path: '/home',
    name: 'Home',
    redirect: '/home/passwordLogin',
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
    component: Workbench,
    meta: { requireAuth: true },
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

router.beforeEach(async to => {
  if (isLogin()) await refreshUser()
  if (to.meta.requireAuth && !isLogin()) {
    const userStore = useUserStore()
    userStore.clear()
    ElMessage.error('请重新登录')
    return '/'
  }
  if (isLogin() && to.path.startsWith('/home')) return '/workbench'
})

export default router