import axios from './axios-plus'

const API_APPLY_CAPTCHA = '/api/captcha/apply'
const API_GET_CURRENT_USER = '/api/user/getCurrentUser'
const API_LOGOUT = '/api/logout'
const API_PASSWORD_LOGIN = '/api/security/passwordLogin'

const HTTP_METHOD_GET = 'get'
const HTTP_METHOD_POST = 'post'

export const logout = async () => {
  await axios({
    method: HTTP_METHOD_GET,
    url: API_LOGOUT
  })
}

export const getCurrentUser = async () => {
  return await axios({
    method: HTTP_METHOD_GET,
    url: API_GET_CURRENT_USER
  })
}

export const passwordLogin = async (username, password, captcha) => {
  return await axios({
    method: HTTP_METHOD_POST,
    url: API_PASSWORD_LOGIN,
    data: {
      username: username,
      password: password,
      captcha: captcha
    }
  })
}

export const applyCaptcha = async () => {
  return await axios({
    method: HTTP_METHOD_GET,
    url: API_APPLY_CAPTCHA
  })
}