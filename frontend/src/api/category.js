import request from '../utils/request'

export function getCategoryList() {
  return request.get('/category/list')
}

export function createCategory(data) {
  return request.post('/category', data)
}

export function updateCategory(data) {
  return request.put('/category', data)
}

export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
