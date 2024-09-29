import axios from './axios-plus'

const API_ADD_USER = '/api/user/add'
const API_DELETE_USER = '/api/user/delete'
const API_SEARCH_USER = '/api/user/search'
const API_APPLY_CAPTCHA = '/api/captcha/apply'
const API_GET_CURRENT_USER = '/api/user/getCurrentUser'
const API_LOGOUT = '/api/logout'
const API_PASSWORD_LOGIN = '/api/security/passwordLogin'
const API_TENANT_SEARCH = '/api/tenant/search'
const API_UPLOAD_AVATAR = '/api/user/uploadAvatar'

const HTTP_GET = 'get'
const HTTP_POST = 'post'
const HTTP_DELETE = 'delete'

export const addUser = async (user) => {
  return await axios({
    method: HTTP_POST,
    url: API_ADD_USER,
    data: user
  })
}

export const deleteUser = async (id) => {
  return await axios({
    method: HTTP_DELETE,
    url: API_DELETE_USER,
    params: {
      id: id
    }
  })
}

export const searchUsers = async (searchRequest) => {
  return await axios({
    method: HTTP_POST,
    url: API_SEARCH_USER,
    data: searchRequest
  })
}

export const logout = async () => {
  await axios({
    method: HTTP_GET,
    url: API_LOGOUT
  })
}

export const getCurrentUser = async () => {
  return await axios({
    method: HTTP_GET,
    url: API_GET_CURRENT_USER
  })
}

export const passwordLogin = async (request) => {
  return await axios({
    method: HTTP_POST,
    url: API_PASSWORD_LOGIN,
    data: request
  })
}

export const applyCaptcha = async () => {
  return await axios({
    method: HTTP_GET,
    url: API_APPLY_CAPTCHA
  })
}

export const uploadAvatar = async (options) => {
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
    url: API_UPLOAD_AVATAR,
    headers: requestHeaders,
    data: formData
  })
}

export const searchTenant = async (searchRequest) => {
  return await axios({
    method: HTTP_POST,
    url: API_TENANT_SEARCH,
    data: searchRequest
  })
}