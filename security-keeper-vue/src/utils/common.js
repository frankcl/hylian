import router from '@/router'
import { useUserStoreHook } from '@/store'
import { httpRequest } from '@/utils/http'
import { ElMessage } from 'element-plus'

const userStore = useUserStoreHook()

export async function logout() {
  await httpRequest({
    method: 'get',
    url: '/api/logout'
  })
  userStore.$reset()
  await router.push('/')
}

export function hasAccessPermission() {
  if (!userStore.isLogin) ElMessage.warning('未登录')
  return userStore.isLogin
}