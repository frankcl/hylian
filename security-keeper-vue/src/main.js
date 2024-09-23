import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersistedState from 'pinia-plugin-persistedstate'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import router from '@/router'
import '@/style/style.css'
import App from './App'

const pinia = createPinia()
pinia.use(piniaPersistedState)
createApp(App).use(pinia).use(router).mount('#app')

const userStore = useUserStore()

router.beforeEach(to => {
  if (to.meta['requireAuth'] && !userStore.isLogin) {
    ElMessage.error('未登录')
    return '/'
  }
  if (userStore.isLogin && to.path.startsWith('/home')) {
    return '/workbench'
  }
})
