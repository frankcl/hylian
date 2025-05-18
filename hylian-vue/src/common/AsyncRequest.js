import { useUserStore } from '@/store'
import AxiosRequest from '@/common/AxiosRequest'

export const asyncLogout = async () => await AxiosRequest.get('/api/logout')
export const asyncForceRefresh = async () => await AxiosRequest.post('/api/security/forceRefresh')
export const asyncRegister = async user => await AxiosRequest.post('/api/security/register', user)
export const asyncPasswordLogin = async request => await AxiosRequest.post('/api/security/passwordLogin', request)
export const asyncApplyCaptcha = async () => await AxiosRequest.get('/api/captcha/apply')
export const asyncGenerateQRCode = async request => await AxiosRequest.get('/api/wechat/code/generate', {params: request})
export const asyncWechatLogin = async key => await AxiosRequest.get('/api/wechat/user/login', {params: {key: key}})
export const asyncCurrentUser = async () => await AxiosRequest.get('/api/user/getCurrentUser')
export const asyncSearchUser = async request => await AxiosRequest.get('/api/user/search', {params: request})
export const asyncRemoveUser = async id => await AxiosRequest.delete('/api/user/delete', {params: {id: id}})
export const asyncAddUser = async user => await AxiosRequest.put('/api/user/add', user)
export const asyncUpdateUser = async user => await AxiosRequest.post('/api/user/update', user)
export const asyncUpdatePassword = async request => await AxiosRequest.post('/api/user/changePassword', request)
export const asyncUnbindWechat = async user => await AxiosRequest.post('/api/user/unbind', user)
export const asyncGetUser = async id => await AxiosRequest.get('/api/user/get', {params: {id: id}})
export const asyncGetUsers = async () => await AxiosRequest.get('/api/user/getUsers')
export const asyncGetTenants = async () => await AxiosRequest.get('/api/tenant/getTenants', {allowCancel: false})
export const asyncSearchTenant = async request => await AxiosRequest.get('/api/tenant/search', {params: request})
export const asyncRemoveTenant = async id => await AxiosRequest.delete('/api/tenant/delete', {params: {id: id}})
export const asyncAddTenant = async tenant => await AxiosRequest.put('/api/tenant/add', tenant)
export const asyncUpdateTenant = async tenant => await AxiosRequest.post('/api/tenant/update', tenant)
export const asyncGetAppRoles = async app_id => await AxiosRequest.get('/api/role/getAppRoles', {params: {app_id: app_id}})
export const asyncSearchRole = async request => await AxiosRequest.get('/api/role/search', {params: request})
export const asyncGetAppUserRole = async request => await AxiosRequest.get('/api/user/getAppUserRoles', {params: request})
export const asyncBatchUpdateUserRole = async request => await AxiosRequest.post('/api/user/batchUpdateUserRole', request)
export const asyncGetRolePermission = async roleId => await AxiosRequest.get('/api/role/getRolePermissions', {params: {role_id: roleId}})
export const asyncBatchUpdateRolePermission = async request => await AxiosRequest.post('/api/role/batchUpdateRolePermission', request)
export const asyncGetAppPermissions = async appId => await AxiosRequest.get('/api/permission/getAppPermissions', {params: {app_id: appId}})
export const asyncRemoveRole = async id => await AxiosRequest.delete('/api/role/delete', {params: {id: id}})
export const asyncAddRole = async role => await AxiosRequest.put('/api/role/add', role)
export const asyncUpdateRole = async role => await AxiosRequest.post('/api/role/update', role)
export const asyncSearchActivity = async request => await AxiosRequest.get('/api/activity/search', {params: request})
export const asyncRemovePermission = async id => await AxiosRequest.delete('/api/permission/delete', {params: {id: id}})
export const asyncAddPermission = async permission => await AxiosRequest.put('/api/permission/add', permission)
export const asyncUpdatePermission = async permission => await AxiosRequest.post('/api/permission/update', permission)
export const asyncSearchPermission = async request => await AxiosRequest.get('/api/permission/search', {params: request})
export const asyncAppSecret = async () => await AxiosRequest.get('/api/app/randomSecret')
export const asyncGetApp = async id => await AxiosRequest.get('/api/app/get', {params: {id: id}})
export const asyncGetApps = async () => await AxiosRequest.get('/api/app/getApps', {allowCancel: false})
export const asyncSearchApp = async request => await AxiosRequest.get('/api/app/search', {params: request})
export const asyncRemoveApp = async id => await AxiosRequest.delete('/api/app/delete', {params: {id: id}})
export const asyncAddApp = async app => await AxiosRequest.put('/api/app/add', app)
export const asyncUpdateApp = async app => await AxiosRequest.post('/api/app/update', app)
export const asyncGetAppUsers = async app_id => await AxiosRequest.get('/api/app/getAppUsers', {params: {app_id: app_id}})
export const asyncBatchUpdateAppUser = async request => await AxiosRequest.post('/api/app/batchUpdateAppUser', request)
export const asyncRefreshUser = async () => useUserStore().inject(await asyncCurrentUser())
export const asyncRemoveAvatar = async () => await AxiosRequest.delete('/api/user/removeAvatar')
export const asyncUploadAvatar = async options => {
  const formData = new FormData()
  formData.append(options.filename, options.file, options.file.name)
  if (options.data) {
    for (const [key, value] in Object.entries(options.data)) {
      if (Array.isArray(value) && value.length) formData.append(key, ...value)
      else formData.append(key, value)
    }
  }
  const _headers = options.headers || {}
  const headers = { 'Content-Type': 'multipart/form-data' }
  if (_headers instanceof Headers) {
    _headers.forEach((value, key) => headers[key] = value)
  } else {
    for (const [key, value] in Object.entries(_headers)) {
      if (value) headers[key] = value
    }
  }
  return await AxiosRequest.post('/api/user/uploadAvatar', formData, { headers: headers })
}

export const newSearchQuery = searchQuery => {
  const rawSearchQuery = {
    page_num: 1,
    page_size: 10,
    sort_field: null,
    sort_order: null,
  }
  return searchQuery ? { ... rawSearchQuery, ... searchQuery } : rawSearchQuery
}

export const newSearchRequest = searchQuery => {
  const searchRequest = {
    page_num: searchQuery.page_num || 1,
    page_size: searchQuery.page_size || 10
  }
  if (searchQuery.sort_field && searchQuery.sort_order) {
    searchRequest.order_by = JSON.stringify([{
      field: searchQuery.sort_field,
      asc: searchQuery.sort_order === 'ascending' }])
  }
  searchQueryTimeToRequestTime(searchQuery, searchRequest, 'create_time')
  searchQueryTimeToRequestTime(searchQuery, searchRequest, 'update_time')
  return searchRequest
}

export const changeSearchQuerySort = (field, order, searchQuery) => {
  if (field) {
    searchQuery.sort_field = field
    searchQuery.sort_order = order
  }
}

const searchQueryTimeToRequestTime = (searchQuery, searchRequest, key) => {
  if (searchQuery[key] && searchQuery[key].length === 2) {
    searchRequest[key] = JSON.stringify({
      include_lower: true,
      include_upper: true,
      start: searchQuery[key][0].getTime(),
      end: searchQuery[key][1].getTime()
    })
  }
}