<template>
  <div class="community-page" v-loading="loading">
    <div class="top-bar">
      <h3>🌐 知识交流社区</h3>
      <el-input
        v-model="keyword"
        placeholder="搜索社区笔记..."
        clearable
        style="width: 240px"
        @keyup.enter="loadNotes"
        @clear="loadNotes"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div v-if="noteList.length" class="note-cards">
      <div
        v-for="(note, index) in noteList"
        :key="note.id"
        class="note-card card-hover"
        :style="{ animationDelay: index * 0.05 + 's' }"
        @click="$router.push(`/user/note/detail/${note.id}`)"
      >
        <div class="note-card-header">
          <span class="note-title">{{ note.title }}</span>
          <span class="note-time">{{ formatTime(note.createTime) }}</span>
        </div>
        <div class="note-summary">{{ note.summary || '暂无摘要' }}</div>
        <div class="note-card-footer">
          <span class="note-author">👤 {{ note.authorName }}</span>
          <span v-if="note.categoryName" class="note-category">📁 {{ note.categoryName }}</span>
          <div class="note-tags">
            <el-tag v-for="tag in note.tags" :key="tag.id" size="small" :color="tag.color + '22'" :style="{ color: tag.color, borderColor: tag.color + '44' }">
              {{ tag.name }}
            </el-tag>
          </div>
          <span class="note-views">👁 {{ note.viewCount }}</span>
        </div>
      </div>
    </div>
    <div v-else-if="!loading" class="empty-tip">暂无社区笔记</div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadNotes"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { communityPage } from '../../api/community'
import { ElMessage } from 'element-plus'

const noteList = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

onMounted(() => loadNotes())

async function loadNotes() {
  loading.value = true
  try {
    const res = await communityPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    noteList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载社区笔记失败')
  } finally {
    loading.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}
</script>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.top-bar h3 {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
}

.note-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.note-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  animation: listItemIn 0.3s ease backwards;
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.note-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.note-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.note-time {
  font-size: 12px;
  color: var(--text-light);
}

.note-summary {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.note-card-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.note-author {
  color: var(--primary-dark);
  font-weight: 500;
}

.note-category {
  color: var(--text-secondary);
}

.note-tags {
  display: flex;
  gap: 4px;
  flex: 1;
}

.note-views {
  color: var(--text-light);
}

.empty-tip {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
