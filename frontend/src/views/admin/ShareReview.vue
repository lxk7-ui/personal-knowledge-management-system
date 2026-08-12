<template>
  <div class="share-review-page" v-loading="loading">
    <h3 class="page-heading">📋 分享审核</h3>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="title" label="笔记标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="authorName" label="作者" width="100" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="提交时间" width="120">
        <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" class="btn-click" @click="handleReview(row, 2)">通过</el-button>
          <el-button type="danger" size="small" class="btn-click" @click="handleReview(row, 3)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && !list.length" class="empty-tip">暂无待审核的分享</div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadList"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminSharePage, reviewShare } from '../../api/community'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await adminSharePage({ page: currentPage.value, size: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载审核列表失败')
  } finally {
    loading.value = false
  }
}

async function handleReview(row, status) {
  const action = status === 2 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定${action}「${row.title}」的分享申请吗？`, '审核确认', { type: 'warning' })
  try {
    await reviewShare(row.id, status)
    ElMessage.success(`已${action}`)
    loadList()
  } catch {
    ElMessage.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 10)
}
</script>

<style scoped>
.page-heading {
  font-size: 15px;
  color: var(--text-primary);
  margin-bottom: 20px;
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
