import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken, getUser, setUser } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser() || {})

  function login(data) {
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      role: data.role,
      avatar: data.avatar
    }
    setToken(data.token)
    setUser(userInfo.value)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    removeToken()
  }

  function updateInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    setUser(userInfo.value)
  }

  const isLogin = () => !!token.value
  const isAdmin = () => userInfo.value.role === 'ADMIN'
  const isUser = () => userInfo.value.role === 'USER'

  return { token, userInfo, login, logout, updateInfo, isLogin, isAdmin, isUser }
})
