import { popConfirmBox } from '@/common/assortment'

export const baseRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'change' }
  ],
  tenant_id: [
    { required: true, message: '请选择租户', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
}

export const disableUser = async (value, userForm) => {
  if (!value) return
  await popConfirmBox('禁用提示', '禁用后用户不可用，是否确认禁用？',
    undefined, () => userForm.disabled = false)
}