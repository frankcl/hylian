import { createRouter, createWebHistory } from 'vue-router'
import { hasAccessPermission } from '@/utils/common'
import PasswordLogin from '@/components/PasswordLogin.vue'
import WechatLogin from '@/components/WechatLogin'
import Login from '@/views/Login'
import Home from '@/views/Home'

const routes = [
  { path: '/', redirect: '/login/password' },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    children: [
      {
        path: 'password',
        name: 'PasswordLogin',
        component: PasswordLogin
      },
      {
        path: 'wechat',
        name: 'WechatLogin',
        component: WechatLogin
      }
    ]
  },
  { path: '/home', name: 'Home', component: Home }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  if (!to.path.startsWith('/login') && !hasAccessPermission()) return '/login/password'
})

export default router