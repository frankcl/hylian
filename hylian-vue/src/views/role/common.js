import { reactive } from 'vue'
import { ElMessageBox } from 'element-plus'
import { remoteSearchApp } from '@/utils/hylian-service'

export const formRules = reactive({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'change' }
  ],
  app_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ]
})

export const initSearchRequest = searchQuery => {
  const searchRequest = {
    current: searchQuery.currentPage,
    size: searchQuery.pageSize
  }
  if (searchQuery.sortField && searchQuery.sortOrder) {
    searchRequest.order_by = [{ field: searchQuery.sortField, asc: searchQuery.sortOrder === 'ascending' }]
  }
  return searchRequest
}

export const fetchAllApps = async () => {
  const pager = await remoteSearchApp({ current: 1, size: 100 })
  return pager ? pager.records : []
}

export const popConfirmBox = (title, message, executeFunc) => {
  ElMessageBox.confirm(
    message,
    title,
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }
  ).then(executeFunc)
}