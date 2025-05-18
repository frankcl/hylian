export const appFormRules = {
  name: [
    { required: true, message: '请输入应用名', trigger: 'change' }
  ],
  secret: [
    { required: true, message: '请输入应用秘钥', trigger: 'change' },
    { min: 8, message: '应用秘钥至少8位', trigger: 'change' }
  ]
}