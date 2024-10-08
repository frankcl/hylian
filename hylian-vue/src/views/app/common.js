import { reactive } from 'vue'
import { asyncRandomSecret } from '@/common/service'

export const formRules = reactive({
  name: [
    { required: true, message: '请输入应用名', trigger: 'change' }
  ],
  secret: [
    { required: true, message: '请输入应用秘钥', trigger: 'change' },
    { min: 8, message: '应用秘钥至少8位', trigger: 'change' }
  ]
})

export const refreshAppSecret = async appForm => appForm.secret = await asyncRandomSecret()