<template>
  <div class="note-list-page" v-loading="loading">
    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <el-button type="primary" class="btn-click" @click="$router.push('/user/note/edit')">
        <el-icon><EditPen /></el-icon> 写笔记
      </el-button>
      <div class="filter-group">
        <el-select v-model="filterCategory" placeholder="按分类筛选" clearable size="default" style="width:140px" @change="loadNotes">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filterTag" placeholder="按标签筛选" clearable size="default" style="width:140px" @change="loadNotes">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div v-if="noteList.length" class="note-cards">
      <div
        v-for="(note, index) in noteList"
        :key="note.id"
        class="note-card card-hover"
        :style="{ animationDelay: index * 0.05 + 's' }"
        @click="$router.push(`/user/note/detail/${note.id}`)"
      >
        <div class="note-card-header">
          <span class="note-title">
            <el-icon v-if="note.isTop" style="color:var(--warning)"><Top /></el-icon>
            {{ note.title }}
          </span>
          <span class="note-time">{{ formatTime(note.createTime) }}</span>
        </div>
        <div class="note-summary">{{ note.summary || '暂无摘要' }}</div>
        <div class="note-card-footer">
          <span v-if="note.categoryName" class="note-category">📁 {{ note.categoryName }}</span>
          <div class="note-tags">
            <el-tag v-for="tag in note.tags" :key="tag.id" size="small" :color="tag.color + '22'" :style="{ color: tag.color, borderColor: tag.color + '44' }">
              {{ tag.name }}
            </el-tag>
          </div>
          <span class="note-views">👁 {{ note.viewCount }}</span>
          <el-tag v-if="note.shareStatus === 1" type="warning" size="small">审核中</el-tag>
          <el-tag v-if="note.shareStatus === 2" type="success" size="small">已分享</el-tag>
          <el-tag v-if="note.shareStatus === 3" type="danger" size="small">已拒绝</el-tag>
        </div>
      </div>
    </div>
    <div v-else class="empty-tip">暂无笔记，点击"写笔记"开始创作吧～</div>

    <!-- 分页 -->
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
import { pageNotes } from '../../api/note'
import { getCategoryList } from '../../api/category'
import { getTagList } from '../../api/tag'
import { ElMessage } from 'element-plus'

const noteList = ref([])
const categories = ref([])
const tags = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterCategory = ref(null)
const filterTag = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [catRes, tagRes] = await Promise.all([getCategoryList(), getTagList()])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
    await loadNotes()
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

async function loadNotes() {
  try {
    const res = await pageNotes({
      page: currentPage.value,
      size: pageSize.value,
      categoryId: filterCategory.value || undefined,
      tagId: filterTag.value || undefined
    })
    noteList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载笔记失败')
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

.filter-group {
  display: flex;
  gap: 10px;
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
  display: flex;
  align-items: center;
  gap: 4px;
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
