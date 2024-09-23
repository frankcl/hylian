import { createRouter, createWebHistory } from 'vue-router'
import PasswordLogin from '@/components/PasswordLogin.vue'
import WechatLogin from '@/components/WechatLogin'
import Home from '@/views/Home'
import Workbench from '@/views/Workbench'

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

export default router