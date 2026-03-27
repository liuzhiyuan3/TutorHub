import request from '../utils/request'

export const adminLogin = (payload) => request.post('/api/auth/admin/login', payload)
export const adminMe = () => request.get('/api/auth/admin/me')

// 用户
export const pageUsers = (params) => request.get('/api/admin/users/page', { params })
export const getUserProfile = (id) => request.get(`/api/admin/users/${id}/profile`)
export const updateUserStatus = (id, status) => request.put(`/api/admin/users/${id}/status`, null, { params: { status } })
export const deleteUser = (id) => request.delete(`/api/admin/users/${id}`)

// 教员审核
export const pageTeachers = (params) => request.get('/api/admin/teachers/page', { params })
export const getTeacherProfile = (id) => request.get(`/api/admin/teachers/${id}/profile`)
export const auditTeacher = (id, payload) => request.put(`/api/admin/teachers/${id}/audit`, payload)

// 需求
export const pageRequirements = (params) => request.get('/api/admin/requirements/page', { params })
export const auditRequirement = (id, payload) => request.put(`/api/admin/requirements/${id}/audit`, payload)

// 订单
export const pageOrders = (params) => request.get('/api/admin/orders/page', { params })
export const updateOrderStatus = (id, payload) => request.put(`/api/admin/orders/${id}/status`, payload)
export const auditOrder = (id, payload) => request.put(`/api/admin/orders/${id}/audit`, payload)

// 内容管理
export const pageSubjects = (params) => request.get('/api/admin/content/subjects/page', { params })
export const saveSubject = (payload) => request.post('/api/admin/content/subjects', payload)
export const deleteSubject = (id) => request.delete(`/api/admin/content/subjects/${id}`)
export const pageSubjectCategories = (params) => request.get('/api/admin/content/subject-categories/page', { params })
export const saveSubjectCategory = (payload) => request.post('/api/admin/content/subject-categories', payload)
export const deleteSubjectCategory = (id) => request.delete(`/api/admin/content/subject-categories/${id}`)
export const pageSchools = (params) => request.get('/api/admin/content/schools/page', { params })
export const saveSchool = (payload) => request.post('/api/admin/content/schools', payload)
export const deleteSchool = (id) => request.delete(`/api/admin/content/schools/${id}`)
export const pageRegions = (params) => request.get('/api/admin/content/regions/page', { params })
export const saveRegion = (payload) => request.post('/api/admin/content/regions', payload)
export const deleteRegion = (id) => request.delete(`/api/admin/content/regions/${id}`)

// 系统管理
export const pageRoles = (params) => request.get('/api/admin/system/roles/page', { params })
export const saveRole = (payload) => request.post('/api/admin/system/roles', payload)
export const deleteRole = (id) => request.delete(`/api/admin/system/roles/${id}`)

export const pageMenus = (params) => request.get('/api/admin/system/menus/page', { params })
export const saveMenu = (payload) => request.post('/api/admin/system/menus', payload)
export const deleteMenu = (id) => request.delete(`/api/admin/system/menus/${id}`)

export const pageRoleMenus = (params) => request.get('/api/admin/system/role-menus/page', { params })
export const saveRoleMenu = (payload) => request.post('/api/admin/system/role-menus', payload)
export const deleteRoleMenu = (id) => request.delete(`/api/admin/system/role-menus/${id}`)

export const pageDictionaries = (params) => request.get('/api/admin/system/dictionary/page', { params })
export const saveDictionary = (payload) => request.post('/api/admin/system/dictionary', payload)
export const deleteDictionary = (id) => request.delete(`/api/admin/system/dictionary/${id}`)

export const pageDictionaryContents = (params) => request.get('/api/admin/system/dictionary-content/page', { params })
export const saveDictionaryContent = (payload) => request.post('/api/admin/system/dictionary-content', payload)
export const deleteDictionaryContent = (id) => request.delete(`/api/admin/system/dictionary-content/${id}`)

export const pageSlides = (params) => request.get('/api/admin/system/slides/page', { params })
export const saveSlide = (payload) => request.post('/api/admin/system/slides', payload)
export const deleteSlide = (id) => request.delete(`/api/admin/system/slides/${id}`)
export const uploadImage = (file, biz = 'admin') => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/api/file/upload?biz=${encodeURIComponent(biz)}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const pageAdvertisings = (params) => request.get('/api/admin/system/advertising/page', { params })
export const saveAdvertising = (payload) => request.post('/api/admin/system/advertising', payload)
export const deleteAdvertising = (id) => request.delete(`/api/admin/system/advertising/${id}`)

// 统计
export const statsOverview = () => request.get('/api/admin/stats/overview')
export const statsBusiness = () => request.get('/api/admin/stats/business')
export const statsTrend = (params) => request.get('/api/admin/stats/trend', { params })
export const homeFilters = () => request.get('/api/home/filters')
