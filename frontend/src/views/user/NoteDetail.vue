<template>
  <div class="note-detail-page" v-if="note">
    <!-- 笔记头部信息 -->
    <div class="detail-header">
      <h2 class="detail-title">
        <el-icon v-if="note.isTop" style="color:var(--warning)"><Top /></el-icon>
        {{ note.title }}
      </h2>
      <div class="detail-meta">
        <span>📅 {{ formatTime(note.createTime) }}</span>
        <span v-if="categoryName">📁 {{ categoryName }}</span>
        <span>👁 {{ note.viewCount }} 次查看</span>
      </div>
      <div class="detail-tags" v-if="noteTags.length">
        <el-tag v-for="tag in noteTags" :key="tag.id" size="small" :color="tag.color + '22'" :style="{ color: tag.color, borderColor: tag.color + '44' }">
          {{ tag.name }}
        </el-tag>
      </div>
    </div>

    <!-- 笔记内容 -->
    <div class="detail-content" v-html="note.content"></div>

    <!-- 操作按钮 -->
    <div class="detail-actions">
      <el-button class="btn-click" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <template v-if="isOwner">
        <el-button type="primary" class="btn-click" @click="$router.push(`/user/note/edit/${note.id}`)">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
        <el-button class="btn-click" @click="handleExport">
          <el-icon><Download /></el-icon> 导出TXT
        </el-button>
        <el-button type="danger" class="btn-click" @click="handleDelete">
          <el-icon><Delete /></el-icon> 删除
        </el-button>
        <el-button
          v-if="note.shareStatus === 0 || note.shareStatus === 3"
          type="success"
          class="btn-click"
          :loading="sharing"
          @click="handleShare"
        >
          <el-icon><Promotion /></el-icon> 分享到社区
        </el-button>
      </template>
      <el-tag v-if="note.shareStatus === 1" type="warning" size="small">审核中</el-tag>
      <el-tag v-if="note.shareStatus === 2" type="success" size="small">已分享到社区</el-tag>
      <el-tag v-if="note.shareStatus === 3 && isOwner" type="danger" size="small">审核未通过</el-tag>
    </div>

    <!-- 留言区（仅已分享到社区的笔记显示） -->
    <div v-if="note.shareStatus === 2" class="comment-section">
      <h3 class="comment-title">留言区 ({{ commentTotal }})</h3>

      <!-- 发表留言 -->
      <div class="comment-input">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的想法..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" class="btn-click" :loading="submitting" @click="handleSubmitComment" style="margin-top: 8px; align-self: flex-end;">
          发表留言
        </el-button>
      </div>

      <!-- 留言列表 -->
      <div v-if="commentList.length" class="comment-list">
        <div v-for="item in commentList" :key="item.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-avatar">{{ item.avatar || '👤' }}</span>
            <span class="comment-nickname">{{ item.nickname }}</span>
            <span class="comment-time">{{ formatTime(item.createTime) }}</span>
            <el-button
              v-if="item.userId == userStore.userInfo.userId"
              type="danger"
              text
              size="small"
              @click="handleDeleteComment(item.id)"
            >删除</el-button>
          </div>
          <div class="comment-content">{{ item.content }}</div>
        </div>
      </div>
      <div v-else class="comment-empty">暂无留言，来写下第一条吧</div>

      <!-- 分页 -->
      <el-pagination
        v-if="commentTotal > commentPageSize"
        v-model:current-page="commentPage"
        :page-size="commentPageSize"
        :total="commentTotal"
        layout="prev, pager, next"
        @current-change="loadComments"
        style="margin-top: 12px"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoteDetail, deleteNote, exportNote } from '../../api/note'
import { submitShare } from '../../api/community'
import { createComment, getCommentList, deleteComment } from '../../api/comment'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const note = ref(null)
const noteTags = ref([])
const categoryName = ref('')
const sharing = ref(false)

const commentContent = ref('')
const commentList = ref([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const submitting = ref(false)

const isOwner = computed(() => {
  return note.value && note.value.userId == userStore.userInfo.userId
})

onMounted(async () => {
  const res = await getNoteDetail(route.params.id)
  note.value = res.data.note
  noteTags.value = res.data.tags || []
  categoryName.value = res.data.categoryName || ''
  if (note.value.shareStatus === 2) {
    loadComments()
  }
})

async function loadComments() {
  try {
    const res = await getCommentList({ noteId: note.value.id, page: commentPage.value, size: commentPageSize.value })
    commentList.value = res.data?.records || []
    commentTotal.value = res.data?.total || 0
  } catch {
    commentList.value = []
  }
}

async function handleSubmitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('留言内容不能为空')
    return
  }
  submitting.value = true
  try {
    await createComment({ noteId: note.value.id, content: commentContent.value.trim() })
    ElMessage.success('留言成功')
    commentContent.value = ''
    commentPage.value = 1
    await loadComments()
  } catch {
    ElMessage.error('留言失败')
  } finally {
    submitting.value = false
  }
}

async function handleDeleteComment(id) {
  await ElMessageBox.confirm('确定删除这条留言吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await deleteComment(id)
    ElMessage.success('删除成功')
    await loadComments()
  } catch {
    ElMessage.error('删除失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

async function handleDelete() {
  await ElMessageBox.confirm('确定删除这篇笔记吗？删除后不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteNote(note.value.id)
  ElMessage.success('删除成功')
  router.push('/user/notes')
}

async function handleExport() {
  const res = await exportNote(note.value.id)
  const { title, content } = res.data
  const blob = new Blob([`${title}\n\n${content}`], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${title}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

async function handleShare() {
  sharing.value = true
  try {
    await submitShare(note.value.id)
    note.value.shareStatus = 1
    ElMessage.success('已提交审核，等待管理员审核')
  } catch {
    ElMessage.error('提交失败')
  } finally {
    sharing.value = false
  }
}
</script>

<style scoped>
.detail-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(90, 74, 58, 0.08);
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 10px;
}

.detail-tags {
  display: flex;
  gap: 6px;
}

.detail-content {
  min-height: 200px;
  line-height: 1.8;
  font-size: 14px;
  color: var(--text-primary);
}

.detail-content :deep(h1),
.detail-content :deep(h2),
.detail-content :deep(h3) {
  margin: 16px 0 8px;
  color: var(--text-primary);
}

.detail-content :deep(p) {
  margin-bottom: 8px;
}

.detail-content :deep(ul),
.detail-content :deep(ol) {
  padding-left: 20px;
  margin-bottom: 8px;
}

.detail-content :deep(code) {
  background: rgba(90, 74, 58, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.detail-content :deep(blockquote) {
  border-left: 3px solid var(--primary);
  padding-left: 12px;
  color: var(--text-secondary);
  margin: 10px 0;
}

.detail-actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 2px solid rgba(90, 74, 58, 0.08);
}

.comment-section {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 2px solid rgba(90, 74, 58, 0.08);
}

.comment-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.comment-input {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  background: rgba(90, 74, 58, 0.03);
  border-radius: 8px;
  padding: 12px 16px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-avatar {
  font-size: 18px;
}

.comment-nickname {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--text-light);
  margin-left: auto;
}

.comment-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-empty {
  text-align: center;
  padding: 30px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
