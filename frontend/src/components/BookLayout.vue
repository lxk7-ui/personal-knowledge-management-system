<template>
  <!-- 木质桌面背景 -->
  <div class="desk-background">
    <!-- 桌面散落的笔装饰 -->
    <div class="desk-pen pen-1">🖊️</div>
    <div class="desk-pen pen-2">✏️</div>
    <div class="desk-pen pen-3">🖋️</div>
    <div class="desk-pen pen-4">✒️</div>
    <!-- 普通用户：左侧边栏 -->
    <div v-if="userStore.isUser()" class="sidebar sidebar-left">
      <div class="sidebar-header">
        <span class="sidebar-logo">📖</span>
        <span class="sidebar-title">知识小屋</span>
      </div>
      <div class="sidebar-menu">
        <div
          v-for="item in userMenus"
          :key="item.path"
          class="menu-item"
          :class="{ active: currentPath === item.path }"
          @click="navigateTo(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">{{ userStore.userInfo.avatar || '🧑‍🎓' }}</span>
          <span class="user-name">{{ userStore.userInfo.nickname }}</span>
        </div>
        <div class="menu-item logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </div>

    <!-- 书本主体 -->
    <div class="book-container">
      <div class="book-body">
        <div class="book-thickness"></div>
        <!-- 书页头部 -->
        <div class="book-header">
          <div class="page-title">
            <el-icon class="icon" v-if="canGoBack" @click="goBack" style="cursor:pointer">
              <ArrowLeft />
            </el-icon>
            <span>{{ pageTitle }}</span>
          </div>
        </div>
        <!-- 书页内容（翻页动画） -->
        <div class="book-content">
          <transition :name="transitionName" mode="out-in">
            <router-view :key="$route.path" />
          </transition>
        </div>
        <!-- 书页底部 -->
        <div class="book-footer">
          个人知识管理系统 · {{ currentYear }}
        </div>
      </div>
    </div>

    <!-- 管理员：右侧边栏 -->
    <div v-if="userStore.isAdmin()" class="sidebar sidebar-right">
      <div class="sidebar-header">
        <span class="sidebar-logo">🔧</span>
        <span class="sidebar-title">管理中心</span>
      </div>
      <div class="sidebar-menu">
        <div
          v-for="item in adminMenus"
          :key="item.path"
          class="menu-item"
          :class="{ active: currentPath === item.path }"
          @click="navigateTo(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <span v-if="item.path === '/admin/share-review' && pendingShareCount > 0" class="menu-badge">{{ pendingShareCount }}</span>
        </div>
      </div>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">{{ userStore.userInfo.avatar || '👨‍💼' }}</span>
          <span class="user-name">{{ userStore.userInfo.nickname }}</span>
        </div>
        <div class="menu-item logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { adminSharePage } from '../api/community'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentYear = new Date().getFullYear()
const transitionName = ref('page-flip-left')
const previousDepth = ref(0)
const pendingShareCount = ref(0)

async function loadPendingCount() {
  if (!userStore.isAdmin()) return
  try {
    const res = await adminSharePage({ page: 1, size: 1 })
    pendingShareCount.value = res.data?.total || 0
  } catch {}
}

onMounted(() => loadPendingCount())

// 从审核页返回时刷新角标
watch(() => route.path, (newPath, oldPath) => {
  if (oldPath === '/admin/share-review') loadPendingCount()
})

// 普通用户菜单
const userMenus = [
  { path: '/user/home', label: '首页', icon: 'House' },
  { path: '/user/notes', label: '我的笔记', icon: 'Notebook' },
  { path: '/user/categories', label: '分类管理', icon: 'Folder' },
  { path: '/user/tags', label: '标签管理', icon: 'PriceTag' },
  { path: '/user/search', label: '搜索笔记', icon: 'Search' },
  { path: '/user/community', label: '知识社区', icon: 'ChatDotRound' },
  { path: '/user/profile', label: '个人中心', icon: 'User' }
]

// 管理员菜单
const adminMenus = [
  { path: '/admin/users', label: '用户管理', icon: 'UserFilled' },
  { path: '/admin/notes', label: '笔记管理', icon: 'Notebook' },
  { path: '/admin/share-review', label: '分享审核', icon: 'Checked' },
  { path: '/admin/comments', label: '留言管理', icon: 'ChatDotSquare' },
  { path: '/admin/categories', label: '分类管理', icon: 'Folder' },
  { path: '/admin/tags', label: '标签管理', icon: 'PriceTag' },
  { path: '/admin/logs', label: '系统日志', icon: 'Document' },
  { path: '/admin/statistics', label: '数据统计', icon: 'DataAnalysis' }
]

const currentPath = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '首页')

// 判断是否可以返回
const canGoBack = computed(() => {
  const path = route.path
  return path.includes('/note/edit') || path.includes('/note/detail')
})

function goBack() {
  transitionName.value = 'page-flip-right'
  router.back()
}

// 计算路由深度，决定翻页方向
function getRouteDepth(path) {
  return path.split('/').filter(Boolean).length
}

// 监听路由变化，设置翻页动画方向
watch(() => route.path, (newPath, oldPath) => {
  const newDepth = getRouteDepth(newPath)
  const oldDepth = getRouteDepth(oldPath || '')
  if (newDepth > oldDepth) {
    transitionName.value = 'page-flip-left'
  } else if (newDepth < oldDepth) {
    transitionName.value = 'page-flip-right'
  } else {
    transitionName.value = 'page-flip-left'
  }
  previousDepth.value = newDepth
})

function navigateTo(path) {
  if (route.path !== path) {
    const newDepth = getRouteDepth(path)
    const oldDepth = getRouteDepth(route.path)
    transitionName.value = newDepth >= oldDepth ? 'page-flip-left' : 'page-flip-right'
    router.push(path)
  }
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
/* 侧边栏通用样式 - 毛玻璃 */
.sidebar {
  width: 185px;
  height: 82vh;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-md), inset 0 1px 0 rgba(255, 255, 255, 0.6);
  padding: 18px 12px;
  z-index: 10;
}

.sidebar-left {
  margin-right: 20px;
}

.sidebar-right {
  margin-left: 20px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 16px;
  border-bottom: 1px solid rgba(74, 60, 46, 0.06);
  margin-bottom: 10px;
}

.sidebar-logo {
  font-size: 26px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-menu .menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 13.5px;
  color: var(--text-secondary);
  user-select: none;
  position: relative;
  overflow: hidden;
}

.sidebar-menu .menu-item .el-icon {
  font-size: 16px;
  opacity: 0.8;
}

.sidebar-footer {
  border-top: 1px solid rgba(74, 60, 46, 0.06);
  padding-top: 10px;
  margin-top: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px 10px;
}

.user-avatar {
  font-size: 22px;
}

.user-name {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  color: var(--danger) !important;
}
.logout-btn:hover {
  background: rgba(232, 160, 160, 0.12) !important;
}

/* 角标 */
.menu-badge {
  margin-left: auto;
  background: var(--danger);
  color: white;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 10px;
  padding: 0 5px;
}

/* 桌面散落的笔 */
.desk-pen {
  position: absolute;
  font-size: 28px;
  opacity: 0.7;
  pointer-events: none;
  z-index: 1;
  filter: drop-shadow(1px 2px 2px rgba(90, 74, 58, 0.3));
}
.pen-1 {
  bottom: 60px;
  left: 6%;
  transform: rotate(-25deg);
}
.pen-2 {
  bottom: 40px;
  right: 8%;
  transform: rotate(15deg);
}
.pen-3 {
  top: 50px;
  right: 5%;
  transform: rotate(-40deg);
  font-size: 24px;
}
.pen-4 {
  bottom: 90px;
  left: 50%;
  transform: rotate(35deg) translateX(-50%);
  font-size: 22px;
  opacity: 0.5;
}
</style>
