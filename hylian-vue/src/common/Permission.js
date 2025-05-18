import Cookies from 'js-cookie'
import { useUserStore } from '@/store'

const NO_PERMISSION_REQUESTS = [
  '/api/app/add',
  '/api/app/update',
  '/api/app/delete',
  '/api/user/add',
  '/api/user/delete',
  '/api/tenant/add',
  '/api/tenant/update',
  '/api/tenant/delete',
]

export const checkLogin = () => Cookies.get('TOKEN') !== undefined
export const checkPermission = requestURL => {
  if (!NO_PERMISSION_REQUESTS.includes(requestURL)) return true
  return useUserStore().superAdmin
}