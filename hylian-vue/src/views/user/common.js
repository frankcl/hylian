export const appFormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'change' }],
  name: [{ required: true, message: '请输入用户昵称', trigger: 'change' }],
  tenant_id: [{ required: true, message: '请选择租户', trigger: 'change' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'change' },
    { min: 8, message: '密码至少8位', trigger: 'change' }
  ],
}