import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken, getUser } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  // 普通用户路由
  {
    path: '/user',
    component: () => import('../components/BookLayout.vue'),
    meta: { role: 'USER' },
    children: [
      { path: '', redirect: '/user/home' },
      { path: 'home', name: 'UserHome', component: () => import('../views/user/Home.vue'), meta: { title: '首页' } },
      { path: 'notes', name: 'NoteList', component: () => import('../views/user/NoteList.vue'), meta: { title: '我的笔记' } },
      { path: 'note/edit/:id?', name: 'NoteEdit', component: () => import('../views/user/NoteEdit.vue'), meta: { title: '编辑笔记' } },
      { path: 'note/detail/:id', name: 'NoteDetail', component: () => import('../views/user/NoteDetail.vue'), meta: { title: '笔记详情' } },
      { path: 'categories', name: 'UserCategory', component: () => import('../views/user/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'tags', name: 'UserTag', component: () => import('../views/user/TagManage.vue'), meta: { title: '标签管理' } },
      { path: 'search', name: 'SearchNote', component: () => import('../views/user/SearchNote.vue'), meta: { title: '搜索笔记' } },
      { path: 'community', name: 'Community', component: () => import('../views/user/Community.vue'), meta: { title: '知识社区' } },
      { path: 'profile', name: 'Profile', component: () => import('../views/user/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../components/BookLayout.vue'),
    meta: { role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'notes', name: 'AdminNotes', component: () => import('../views/admin/NoteManage.vue'), meta: { title: '笔记管理' } },
      { path: 'share-review', name: 'ShareReview', component: () => import('../views/admin/ShareReview.vue'), meta: { title: '分享审核' } },
      { path: 'comments', name: 'AdminComments', component: () => import('../views/admin/CommentManage.vue'), meta: { title: '留言管理' } },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'tags', name: 'AdminTags', component: () => import('../views/admin/TagManage.vue'), meta: { title: '标签管理' } },
      { path: 'logs', name: 'SystemLog', component: () => import('../views/admin/SystemLog.vue'), meta: { title: '系统日志' } },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/admin/Statistics.vue'), meta: { title: '数据统计' } }
    ]
  },
  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：权限控制
router.beforeEach((to, from, next) => {
  const token = getToken()
  const user = getUser()

  if (to.path === '/login') {
    if (token && user) {
      next(user.role === 'ADMIN' ? '/admin' : '/user')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色权限校验
  const requiredRole = to.matched.find(r => r.meta.role)?.meta.role
  if (requiredRole && user?.role !== requiredRole) {
    next(user.role === 'ADMIN' ? '/admin' : '/user')
    return
  }

  next()
})

export default router
