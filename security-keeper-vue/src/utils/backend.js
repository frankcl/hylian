import { httpRequest } from '@/utils/http'

export async function logout() {
  await httpRequest({
    method: 'get',
    url: '/api/logout'
  })
}

export async function getCurrentUser() {
  const { code, data } = await httpRequest({
    method: 'get',
    url: '/api/user/getCurrentUser'
  })
  if (code === 200) return data
  return undefined
}

export async function passwordLogin(username, password, captcha) {
  const { code } = await httpRequest({
    method: 'post',
    url: '/api/security/passwordLogin',
    data: {
      username: username,
      password: password,
      captcha: captcha
    }
  })
  return code === 200
}

export async function applyCaptcha() {
  const { code, data } = await httpRequest({
    method: 'get',
    url: '/api/captcha/apply'
  })
  if (code === 200) return data
  return undefined
}