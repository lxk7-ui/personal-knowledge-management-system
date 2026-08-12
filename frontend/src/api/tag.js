import request from '../utils/request'

export function getTagList() {
  return request.get('/tag/list')
}

export function createTag(data) {
  return request.post('/tag', data)
}

export function updateTag(data) {
  return request.put('/tag', data)
}

export function deleteTag(id) {
  return request.delete(`/tag/${id}`)
}
