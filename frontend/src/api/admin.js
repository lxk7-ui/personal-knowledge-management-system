import request from '../utils/request'

// 管理员接口
export function adminUserPage(params) {
  return request.get('/admin/user/page', { params })
}

export function toggleUserStatus(id, status) {
  return request.put(`/admin/user/status/${id}?status=${status}`)
}

export function deleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}

export function adminNotePage(params) {
  return request.get('/admin/note/page', { params })
}

export function adminDeleteNote(id) {
  return request.delete(`/admin/note/${id}`)
}

export function adminCategoryPage(params) {
  return request.get('/admin/category/page', { params })
}

export function adminDeleteCategory(id) {
  return request.delete(`/admin/category/${id}`)
}

export function adminTagPage(params) {
  return request.get('/admin/tag/page', { params })
}

export function adminDeleteTag(id) {
  return request.delete(`/admin/tag/${id}`)
}

export function getStatistics() {
  return request.get('/admin/statistics')
}

export function getLogPage(params) {
  return request.get('/admin/log/page', { params })
}

export function clearLogs() {
  return request.delete('/admin/log/clear')
}

export function adminCommentPage(params) {
  return request.get('/admin/comment/page', { params })
}

export function adminUpdateComment(id, data) {
  return request.put(`/admin/comment/${id}`, data)
}

export function adminDeleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}
