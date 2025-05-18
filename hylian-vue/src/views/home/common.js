import router from '@/router/index.js'

export const afterLogin = async route => {
  if (route.query && route.query.redirect) {
    window.location.href = decodeURIComponent(route.query.redirect)
    return
  }
  await router.push('/workbench')
}