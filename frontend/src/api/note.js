import request from '../utils/request'

export function pageNotes(params) {
  return request.get('/note/page', { params })
}

export function getNoteDetail(id) {
  return request.get(`/note/${id}`)
}

export function createNote(data) {
  return request.post('/note', data)
}

export function updateNote(data) {
  return request.put('/note', data)
}

export function deleteNote(id) {
  return request.delete(`/note/${id}`)
}

export function recentNotes() {
  return request.get('/note/recent')
}

export function exportNote(id) {
  return request.get(`/note/export/${id}`)
}
