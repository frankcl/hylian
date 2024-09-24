import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import PasswordLogin from '@/components/PasswordLogin'
import WechatLogin from '@/components/WechatLogin'
import Home from '@/views/Home'
import Workbench from '@/views/Workbench'
import { useUserStore } from '@/store'
import { isLogin, refreshUser } from '@/utils/routine'

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
    meta: { requireAuth: true }
  }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async to => {
  if (isLogin()) await refreshUser()
  if (to.meta['requireAuth'] && !isLogin()) {
    const userStore = useUserStore()
    userStore.clear()
    ElMessage.error('请重新登录')
    return '/'
  }
  if (isLogin() && to.path.startsWith('/home')) {
    return '/workbench'
  }
})

export default router