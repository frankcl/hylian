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
    const superAdmin = ref()
    const roles = ref()

    const injected = computed(() => username.value !== undefined)

    const inject = user => {
      if (!user) return
      id.value = user.id
      username.value = user.username
      name.value = user.name
      avatar.value = user.avatar
      superAdmin.value = user.super_admin
      tenant.value = user.tenant
      roles.value = user.roles
    }

    const clear = () => {
      id.value = undefined
      username.value = undefined
      name.value = undefined
      avatar.value = undefined
      superAdmin.value = undefined
      tenant.value = undefined
      roles.value = undefined
    }

    return { id, username, name, avatar, superAdmin, tenant, roles, injected, inject, clear }
  },
  {
    persist: {
      storage: sessionStorage,
      omit: ['inject', 'clear', 'injected']
    }
  }
)