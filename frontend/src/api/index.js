import request from './request'

/* 认证 */
export const getAuthConfig = () => request.get('/auth/config')
export const getCaptcha = () => request.get('/auth/captcha')
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getInfo = () => request.get('/auth/info')
export const getMenus = () => request.get('/auth/menus')
export const logout = () => request.post('/auth/logout')

/* 通用选项 */
export const getOptions = () => request.get('/lookup/options')

/* 工作台 */
export const getOverview = () => request.get('/dashboard/overview')

/* 系统管理 */
export const pageUsers = (params) => request.get('/system/users', { params })
export const allUsers = () => request.get('/system/users/all')
export const createUser = (data) => request.post('/system/users', data)
export const updateUser = (id, data) => request.put(`/system/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/system/users/${id}`)
export const getRoles = () => request.get('/system/roles')
export const pageLoginLogs = (params) => request.get('/system/login-logs', { params })

/* MES */
export const pageTasks = (params) => request.get('/mes/tasks', { params })
export const getTask = (id) => request.get(`/mes/tasks/${id}`)
export const createTask = (data) => request.post('/mes/tasks', data)
export const updateTask = (id, data) => request.put(`/mes/tasks/${id}`, data)
export const deleteTask = (id) => request.delete(`/mes/tasks/${id}`)
export const pageFeedback = (params) => request.get('/mes/feedback', { params })
export const createFeedback = (data) => request.post('/mes/feedback', data)
export const approveFeedback = (id, remark) => request.post(`/mes/feedback/${id}/approve`, { remark })
export const rejectFeedback = (id, remark) => request.post(`/mes/feedback/${id}/reject`, { remark })
export const getRoutes = (params) => request.get('/mes/routes', { params })
export const createRoute = (data) => request.post('/mes/routes', data)
export const updateRoute = (id, data) => request.put(`/mes/routes/${id}`, data)
export const deleteRoute = (id) => request.delete(`/mes/routes/${id}`)
export const getRouteCategories = (params) => request.get('/mes/route-categories', { params })
export const createRouteCategory = (data) => request.post('/mes/route-categories', data)
export const updateRouteCategory = (id, data) => request.put(`/mes/route-categories/${id}`, data)
export const deleteRouteCategory = (id) => request.delete(`/mes/route-categories/${id}`)
export const getWorkstations = (params) => request.get('/mes/workstations', { params })
export const getWorkshops = () => request.get('/mes/workshops')
export const createWorkstation = (data) => request.post('/mes/workstations', data)
export const updateWorkstation = (id, data) => request.put(`/mes/workstations/${id}`, data)
export const deleteWorkstation = (id) => request.delete(`/mes/workstations/${id}`)
export const getStationMembers = (id) => request.get(`/mes/workstations/${id}/members`)
export const saveStationMembers = (id, userIds) => request.post(`/mes/workstations/${id}/members`, userIds)
export const pageWorkOrders = (params) => request.get('/mes/work-orders', { params })

/* WMS */
export const getWarehouses = () => request.get('/wms/warehouses')
export const createWarehouse = (data) => request.post('/wms/warehouses', data)
export const updateWarehouse = (id, data) => request.put(`/wms/warehouses/${id}`, data)
export const deleteWarehouse = (id) => request.delete(`/wms/warehouses/${id}`)
export const getItemCategories = () => request.get('/wms/item-categories')
export const pageItems = (params) => request.get('/wms/items', { params })
export const createItem = (data) => request.post('/wms/items', data)
export const updateItem = (id, data) => request.put(`/wms/items/${id}`, data)
export const deleteItem = (id) => request.delete(`/wms/items/${id}`)
export const getStocks = (params) => request.get('/wms/stocks', { params })
export const pageInouts = (params) => request.get('/wms/inouts', { params })
export const createInout = (data) => request.post('/wms/inouts', data)
export const postInout = (id) => request.post(`/wms/inouts/${id}/post`)

/* ERP */
export const pageProducts = (params) => request.get('/erp/products', { params })
export const createProduct = (data) => request.post('/erp/products', data)
export const updateProduct = (id, data) => request.put(`/erp/products/${id}`, data)
export const deleteProduct = (id) => request.delete(`/erp/products/${id}`)
export const pageErpOrders = (params) => request.get('/erp/orders', { params })
export const createErpOrder = (data) => request.post('/erp/orders', data)
export const updateErpOrder = (id, data) => request.put(`/erp/orders/${id}`, data)
export const deleteErpOrder = (id) => request.delete(`/erp/orders/${id}`)

/* 导入中心 */
export const getImportTypes = () => request.get('/import/types')
export const pageImportBatches = (params) => request.get('/import/batches', { params })
export const getImportErrors = (id) => request.get(`/import/batches/${id}/errors`)
export const importTemplateUrl = (bizType) => `/api/import/template/${bizType}`
export const uploadImport = (formData) => request.post('/import/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})

/* 图纸批量识别 */
export const pageDrawingTasks = (params) => request.get('/drawing/tasks', { params })
export const uploadDrawings = (formData) => request.post('/drawing/batch', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const getDrawingFiles = (id) => request.get(`/drawing/tasks/${id}/files`)
export const retryDrawing = (id) => request.post(`/drawing/files/${id}/retry`)
