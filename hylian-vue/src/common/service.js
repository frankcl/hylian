import axios from '@/common/axios-plus'

const HTTP_GET = 'get'
const HTTP_PUT = 'put'
const HTTP_POST = 'post'
const HTTP_DELETE = 'delete'

export const asyncSearchActivities = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/activity/search',
    params: request
  })
}

export const asyncGetRole = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/role/get',
    params: {
      id: id
    }
  })
}

export const asyncAddRole = async role => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/role/add',
    data: role
  })
}

export const asyncUpdateRole = async role => {
  return await axios({
    method: HTTP_POST,
    url: '/api/role/update',
    data: role
  })
}

export const asyncDeleteRole = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/role/delete',
    params: {
      id: id
    }
  })
}

export const asyncSearchRoles = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/role/search',
    params: request
  })
}

export const asyncGetRolePermissions = async roleId => {
  return await axios({
    method: HTTP_GET,
    url: '/api/role/getRolePermissions',
    params: { role_id: roleId }
  })
}

export const asyncBatchUpdateRolePermissions = async request => {
  return await axios({
    method: HTTP_POST,
    url: '/api/role/batchUpdateRolePermission',
    data: request
  })
}

export const asyncGetPermission = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/permission/get',
    params: {
      id: id
    }
  })
}

export const asyncAddPermission = async permission => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/permission/add',
    data: permission
  })
}

export const asyncUpdatePermission = async permission => {
  return await axios({
    method: HTTP_POST,
    url: '/api/permission/update',
    data: permission
  })
}

export const asyncDeletePermission = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/permission/delete',
    params: {
      id: id
    }
  })
}

export const asyncSearchPermissions = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/permission/search',
    params: request
  })
}

export const asyncGetApp = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app/get',
    params: {
      id: id
    }
  })
}

export const asyncAddApp = async app => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/app/add',
    data: app
  })
}

export const asyncUpdateApp = async app => {
  return await axios({
    method: HTTP_POST,
    url: '/api/app/update',
    data: app
  })
}

export const asyncDeleteApp = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/app/delete',
    params: {
      id: id
    }
  })
}

export const asyncSearchApps = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app/search',
    params: request
  })
}

export const asyncRandomSecret = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/app/randomSecret'
  })
}

export const asyncGetUser = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/get',
    params: {
      id: id
    }
  })
}

export const asyncAddUser = async user => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/user/add',
    data: user
  })
}

export const asyncUpdateUser = async user => {
  return await axios({
    method: HTTP_POST,
    url: '/api/user/update',
    data: user
  })
}

export const asyncDeleteUser = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/user/delete',
    params: {
      id: id
    }
  })
}

export const asyncSearchUsers = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/search',
    params: request
  })
}

export const asyncChangePassword = async request => {
  return await axios({
    method: HTTP_POST,
    url: '/api/user/changePassword',
    data: request
  })
}

export const asyncCurrentUser = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/getCurrentUser'
  })
}

export const asyncGetAppUserRoles = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/user/getAppUserRoles',
    params: request
  })
}

export const asyncBatchUpdateUserRoles = async request => {
  return await axios({
    method: HTTP_POST,
    url: '/api/user/batchUpdateUserRole',
    data: request
  })
}

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
      if (!value) continue
      headers[key] = value
    }
  }
  return await axios({
    method: HTTP_POST,
    url: '/api/user/uploadAvatar',
    headers: headers,
    data: formData
  })
}

export const asyncGetTenant = async id => {
  return await axios({
    method: HTTP_GET,
    url: '/api/tenant/get',
    params: {
      id: id
    }
  })
}

export const asyncAddTenant = async tenant => {
  return await axios({
    method: HTTP_PUT,
    url: '/api/tenant/add',
    data: tenant
  })
}

export const asyncUpdateTenant = async tenant => {
  return await axios({
    method: HTTP_POST,
    url: '/api/tenant/update',
    data: tenant
  })
}

export const asyncDeleteTenant = async id => {
  return await axios({
    method: HTTP_DELETE,
    url: '/api/tenant/delete',
    params: {
      id: id
    }
  })
}

export const asyncSearchTenants = async request => {
  return await axios({
    method: HTTP_GET,
    url: '/api/tenant/search',
    params: request
  })
}

export const asyncLogout = async () => {
  await axios({
    method: HTTP_GET,
    url: '/api/logout'
  })
}

export const asyncPasswordLogin = async request => {
  return await axios({
    method: HTTP_POST,
    url: '/api/security/passwordLogin',
    data: request
  })
}

export const asyncApplyCaptcha = async () => {
  return await axios({
    method: HTTP_GET,
    url: '/api/captcha/apply'
  })
}