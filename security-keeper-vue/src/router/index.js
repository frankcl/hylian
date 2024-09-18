import { createRouter, createWebHistory } from 'vue-router'
import { hasAccessPermission } from '@/utils/common'
import Login from '@/views/Login'
import Home from '@/views/Home'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home }
]
const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  if (to.path !== '/login' && to.path !== '/' && !hasAccessPermission()) return '/login'
})

export default router