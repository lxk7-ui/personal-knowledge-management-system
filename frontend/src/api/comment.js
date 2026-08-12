import request from '../utils/request'

export function createComment(data) {
  return request.post('/comment', data)
}

export function getCommentList(params) {
  return request.get('/comment/list', { params })
}

export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}
