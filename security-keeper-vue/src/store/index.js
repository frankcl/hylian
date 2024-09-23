import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useUserStore = defineStore(
  'user',
  () => {
    const id = ref()
    const username = ref()
    const name = ref()
    const avatar = ref()
    const tenant = ref()
    const vendor = ref()
    const roles = ref()

    const isLogin = computed(() => username.value)

    function setUser(user) {
      id.value = user.id
      username.value = user['user_name']
      name.value = user.name
      avatar.value = user.avatar
      tenant.value = user.tenant
      vendor.value = user.vendor
      roles.value = user.roles
    }

    function reset() {
      id.value = undefined
      username.value = undefined
      name.value = undefined
      avatar.value = undefined
      tenant.value = undefined
      vendor.value = undefined
      roles.value = undefined
    }

    return { id, username, name, avatar, tenant, vendor, roles, isLogin, setUser, reset }
  },
  {
    persist: {
      storage: localStorage,
      omit: ['reset', 'setUser', 'isLogin']
    }
  }
)