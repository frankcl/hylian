import { createApp } from 'vue'
import { createPinia } from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import piniaPersistedState from 'pinia-plugin-persistedstate'
import router from '@/router'
import '@/style/style.css'
import App from './App'

const pinia = createPinia()
pinia.use(piniaPersistedState)
const app = createApp(App)
app.use(pinia).use(router).mount('#app')
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
