<template>
  <div class="comment-manage-page" v-loading="loading">
    <div class="top-bar">
      <h3 class="page-heading">💬 留言管理</h3>
      <el-input
        v-model="keyword"
        placeholder="搜索留言内容..."
        clearable
        style="width: 220px"
        @keyup.enter="loadList"
        @clear="loadList"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="content" label="留言内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="noteId" label="笔记ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="创建时间" width="120">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && !list.length" class="empty-tip">暂无留言</div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadList"
      style="margin-top: 20px"
    />

    <el-dialog v-model="editVisible" title="编辑留言" width="500px">
      <el-input v-model="editContent" type="textarea" :rows="4" maxlength="1000" show-word-limit />
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminCommentPage, adminUpdateComment, adminDeleteComment } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const editVisible = ref(false)
const editContent = ref('')
const editId = ref(null)

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await adminCommentPage({ page: currentPage.value, size: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载留言列表失败')
  } finally {
    loading.value = false
  }
}

function handleEdit(row) {
  editId.value = row.id
  editContent.value = row.content
  editVisible.value = true
}

async function submitEdit() {
  if (!editContent.value.trim()) {
    ElMessage.warning('留言内容不能为空')
    return
  }
  try {
    await adminUpdateComment(editId.value, { content: editContent.value.trim() })
    ElMessage.success('修改成功')
    editVisible.value = false
    loadList()
  } catch {
    ElMessage.error('修改失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这条留言吗？', '提示', { type: 'warning' })
  try {
    await adminDeleteComment(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch {
    ElMessage.error('删除失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-heading {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
