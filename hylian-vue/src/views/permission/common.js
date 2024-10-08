import { reactive } from 'vue'

export const formRules = reactive({
  name: [
    { required: true, message: '请输入权限名称', trigger: 'change' }
  ],
  resource: [
    { required: true, message: '请输入资源路径', trigger: 'change' },
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})