import request from '../utils/request'

export function communityPage(params) {
  return request.get('/note/community', { params })
}

export function submitShare(id) {
  return request.post(`/note/share/${id}`)
}

export function adminSharePage(params) {
  return request.get('/admin/share/page', { params })
}

export function reviewShare(id, shareStatus) {
  return request.put(`/admin/share/review/${id}?shareStatus=${shareStatus}`)
}
