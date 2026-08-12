<template>
  <div class="search-page" v-loading="searching">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="输入关键词模糊搜索笔记..."
        prefix-icon="Search"
        size="large"
        clearable
        @input="onInputChange"
        @clear="handleSearch"
      />
      <div class="filter-row">
        <el-select v-model="filterCategory" placeholder="分类筛选" clearable size="default" style="width:140px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filterTag" placeholder="标签筛选" clearable size="default" style="width:140px">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-button type="primary" class="btn-click" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <div v-if="searched && noteList.length" class="search-results">
      <p class="result-count">找到 {{ total }} 条结果</p>
      <div
        v-for="note in noteList"
        :key="note.id"
        class="result-item card-hover"
        @click="$router.push(`/user/note/detail/${note.id}`)"
      >
        <div class="result-title">{{ note.title }}</div>
        <div class="result-summary">{{ note.summary || '暂无摘要' }}</div>
        <div class="result-meta">
          <span v-if="note.categoryName">📁 {{ note.categoryName }}</span>
          <span>📅 {{ formatTime(note.createTime) }}</span>
          <div class="result-tags">
            <el-tag v-for="tag in note.tags" :key="tag.id" size="small" :color="tag.color + '22'" :style="{ color: tag.color, borderColor: tag.color + '44' }">
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handleSearch"
        style="margin-top: 20px"
      />
    </div>

    <div v-else-if="searched" class="empty-tip">未找到相关笔记，换个关键词试试～</div>
    <div v-else class="empty-tip">输入关键词搜索你的笔记</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { pageNotes } from '../../api/note'
import { getCategoryList } from '../../api/category'
import { getTagList } from '../../api/tag'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const filterCategory = ref(null)
const filterTag = ref(null)
const noteList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searched = ref(false)
const searching = ref(false)
const categories = ref([])
const tags = ref([])

let searchTimer = null

onMounted(async () => {
  try {
    const [catRes, tagRes] = await Promise.all([getCategoryList(), getTagList()])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
  } catch {
    ElMessage.error('加载筛选数据失败')
  }
})

onUnmounted(() => {
  clearTimeout(searchTimer)
})

// 输入时自动触发模糊搜索（防抖 400ms）
function onInputChange() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 400)
}

async function handleSearch() {
  clearTimeout(searchTimer)
  searched.value = true
  searching.value = true
  try {
    const res = await pageNotes({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      categoryId: filterCategory.value || undefined,
      tagId: filterTag.value || undefined
    })
    noteList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.result-count {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 12px;
}

.search-results {
  display: flex;
  flex-direction: column;
}

.result-item {
  padding: 14px 18px;
  background: white;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.result-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.result-summary {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-light);
}

.result-tags {
  display: flex;
  gap: 4px;
}

.empty-tip {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 14px;
}
</style>
