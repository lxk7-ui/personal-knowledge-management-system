<template>
  <div class="home-page" v-loading="loading">
    <!-- 欢迎区域 -->
    <div class="welcome-card card-hover">
      <div class="welcome-left">
        <h2>你好，{{ userStore.userInfo.nickname }} 👋</h2>
        <p>今天也要好好学习呀～</p>
      </div>
      <div class="welcome-stats">
        <div class="stat-item">
          <span class="stat-num">{{ stats.noteCount }}</span>
          <span class="stat-label">篇笔记</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.categoryCount }}</span>
          <span class="stat-label">个分类</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.tagCount }}</span>
          <span class="stat-label">个标签</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="action-btn card-hover btn-click" @click="$router.push('/user/note/edit')">
        <el-icon :size="22"><EditPen /></el-icon>
        <span>写笔记</span>
      </div>
      <div class="action-btn card-hover btn-click" @click="$router.push('/user/notes')">
        <el-icon :size="22"><Notebook /></el-icon>
        <span>我的笔记</span>
      </div>
      <div class="action-btn card-hover btn-click" @click="$router.push('/user/search')">
        <el-icon :size="22"><Search /></el-icon>
        <span>搜索</span>
      </div>
      <div class="action-btn card-hover btn-click" @click="$router.push('/user/categories')">
        <el-icon :size="22"><Folder /></el-icon>
        <span>分类</span>
      </div>
    </div>

    <!-- 最近查看 -->
    <div class="section">
      <h3 class="section-title">📖 最近查看</h3>
      <div v-if="recentList.length" class="recent-list">
        <div
          v-for="note in recentList"
          :key="note.id"
          class="recent-item card-hover"
          @click="$router.push(`/user/note/detail/${note.id}`)"
        >
          <div class="recent-title">{{ note.title }}</div>
          <div class="recent-time">{{ formatTime(note.lastViewTime) }}</div>
        </div>
      </div>
      <div v-else class="empty-tip">暂无最近查看的笔记，快去写一篇吧～</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { recentNotes, pageNotes } from '../../api/note'
import { getCategoryList } from '../../api/category'
import { getTagList } from '../../api/tag'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const recentList = ref([])
const stats = ref({ noteCount: 0, categoryCount: 0, tagCount: 0 })
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [recentRes, noteRes, catRes, tagRes] = await Promise.all([
      recentNotes(),
      pageNotes({ page: 1, size: 1 }),
      getCategoryList(),
      getTagList()
    ])
    recentList.value = recentRes.data || []
    stats.value.noteCount = noteRes.data?.total || 0
    stats.value.categoryCount = catRes.data?.length || 0
    stats.value.tagCount = tagRes.data?.length || 0
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
})

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.welcome-card {
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--primary) 60%, var(--primary-dark) 100%);
  border-radius: var(--radius-lg);
  padding: 26px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-white);
  position: relative;
  overflow: hidden;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -30%;
  right: -10%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
  pointer-events: none;
}

.welcome-left h2 {
  font-size: 19px;
  margin-bottom: 6px;
  font-weight: 600;
}

.welcome-left p {
  font-size: 13px;
  opacity: 0.8;
}

.welcome-stats {
  display: flex;
  gap: 28px;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 26px;
  font-weight: 700;
}

.stat-label {
  font-size: 11px;
  opacity: 0.75;
  letter-spacing: 0.5px;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  background: white;
  border-radius: var(--radius-lg);
  cursor: pointer;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.action-btn .el-icon {
  color: var(--primary);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: var(--radius-md);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.recent-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.recent-time {
  font-size: 12px;
  color: var(--text-light);
}

.empty-tip {
  text-align: center;
  padding: 30px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
