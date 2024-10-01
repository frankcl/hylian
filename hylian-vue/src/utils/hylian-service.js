import axios from './axios-plus'

const API_USER_GET = '/api/user/get'
const API_USER_ADD = '/api/user/add'
const API_USER_UPDATE = '/api/user/update'
const API_USER_DELETE = '/api/user/delete'
const API_USER_SEARCH = '/api/user/search'
const API_USER_UPLOAD_AVATAR = '/api/user/uploadAvatar'
const API_USER_CHANGE_PASSWORD = '/api/user/changePassword'
const API_USER_GET_CURRENT_USER = '/api/user/getCurrentUser'

const API_TENANT_GET = '/api/tenant/get'
const API_TENANT_ADD = '/api/tenant/add'
const API_TENANT_UPDATE = '/api/tenant/update'
const API_TENANT_DELETE = '/api/tenant/delete'
const API_TENANT_SEARCH = '/api/tenant/search'

const API_APPLY_CAPTCHA = '/api/captcha/apply'

const API_LOGOUT = '/api/logout'
const API_PASSWORD_LOGIN = '/api/security/passwordLogin'

const HTTP_GET = 'get'
const HTTP_PUT = 'put'
const HTTP_POST = 'post'
const HTTP_DELETE = 'delete'

export const remoteGetUser = async (id) => {
  return await axios({
    method: HTTP_GET,
    url: API_USER_GET,
    params: {
      id: id
    }
  })
}

export const remoteAddUser = async (user) => {
  return await axios({
    method: HTTP_PUT,
    url: API_USER_ADD,
    data: user
  })
}

export const remoteUpdateUser = async (user) => {
  return await axios({
    method: HTTP_POST,
    url: API_USER_UPDATE,
    data: user
  })
}

export const remoteDeleteUser = async (id) => {
  return await axios({
    method: HTTP_DELETE,
    url: API_USER_DELETE,
    params: {
      id: id
    }
  })
}

export const remoteSearchUser = async (searchRequest) => {
  return await axios({
    method: HTTP_POST,
    url: API_USER_SEARCH,
    data: searchRequest
  })
}

export const remoteChangePassword = async (request) => {
  return await axios({
    method: HTTP_POST,
    url: API_USER_CHANGE_PASSWORD,
    data: request
  })
}

export const remoteGetCurrentUser = async () => {
  return await axios({
    method: HTTP_GET,
    url: API_USER_GET_CURRENT_USER
  })
}

export const remoteGetTenant = async (id) => {
  return await axios({
    method: HTTP_GET,
    url: API_TENANT_GET,
    params: {
      id: id
    }
  })
}

export const remoteAddTenant = async (tenant) => {
  return await axios({
    method: HTTP_PUT,
    url: API_TENANT_ADD,
    data: tenant
  })
}

export const remoteUpdateTenant = async (tenant) => {
  return await axios({
    method: HTTP_POST,
    url: API_TENANT_UPDATE,
    data: tenant
  })
}

export const remoteDeleteTenant = async (id) => {
  return await axios({
    method: HTTP_DELETE,
    url: API_TENANT_DELETE,
    params: {
      id: id
    }
  })
}

export const remoteSearchTenant = async (searchRequest) => {
  return await axios({
    method: HTTP_POST,
    url: API_TENANT_SEARCH,
    data: searchRequest
  })
}

export const remoteLogout = async () => {
  await axios({
    method: HTTP_GET,
    url: API_LOGOUT
  })
}

export const remotePasswordLogin = async (request) => {
  return await axios({
    method: HTTP_POST,
    url: API_PASSWORD_LOGIN,
    data: request
  })
}

export const remoteApplyCaptcha = async () => {
  return await axios({
    method: HTTP_GET,
    url: API_APPLY_CAPTCHA
  })
}

export const remoteUploadAvatar = async (options) => {
  const formData = new FormData()
  formData.append(options.filename, options.file, options.file.name)
  if (options.data) {
    for (const [key, value] in Object.entries(options.data)) {
      if (Array.isArray(value) && value.length) formData.append(key, ...value)
      else formData.append(key, value)
    }
  }
  const headers = options.headers || {}
  const requestHeaders = { 'Content-Type': 'multipart/form-data' }
  if (headers instanceof Headers) {
    headers.forEach((value, key) => requestHeaders[key] = value)
  } else {
    for (const [key, value] in Object.entries(headers)) {
      if (!value) continue
      requestHeaders[key] = value
    }
  }
  return await axios({
    method: HTTP_POST,
    url: API_USER_UPLOAD_AVATAR,
    headers: requestHeaders,
    data: formData
  })
}