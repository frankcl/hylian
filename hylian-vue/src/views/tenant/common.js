import { reactive } from 'vue'

export const formRules = reactive({
  name: [
    { required: true, message: '请输入租户', trigger: 'change' }
  ]
})