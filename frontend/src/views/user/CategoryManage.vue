<template>
  <div class="category-page" v-loading="loading">
    <div class="top-bar">
      <h3>📁 我的分类</h3>
      <el-button type="primary" class="btn-click" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增分类
      </el-button>
    </div>

    <div v-if="list.length" class="category-list">
      <div v-for="(item, index) in list" :key="item.id" class="category-block" :style="{ animationDelay: index * 0.05 + 's' }">
        <div class="category-item card-hover" @click="toggleExpand(item)">
          <div class="category-info">
            <el-icon class="expand-icon" :class="{ expanded: item._expanded }"><ArrowRight /></el-icon>
            <span class="category-name">{{ item.name }}</span>
            <span class="category-count">{{ item._noteCount || 0 }} 篇</span>
          </div>
          <div class="category-actions" @click.stop>
            <el-button size="small" class="btn-click" @click="openDialog(item)">编辑</el-button>
            <el-button size="small" type="danger" class="btn-click" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
        <!-- 下拉展开的笔记列表 -->
        <transition name="fade-in">
          <div v-if="item._expanded" class="note-dropdown">
            <div v-if="item._notes && item._notes.length" class="note-sub-list">
              <div
                v-for="note in item._notes"
                :key="note.id"
                class="note-sub-item card-hover"
                @click="$router.push(`/user/note/detail/${note.id}`)"
              >
                <span class="note-sub-title">📄 {{ note.title }}</span>
                <span class="note-sub-time">{{ formatTime(note.createTime) }}</span>
              </div>
            </div>
            <div v-else class="note-sub-empty">该分类下暂无笔记</div>
          </div>
        </transition>
      </div>
    </div>
    <div v-else class="empty-tip">暂无分类，点击"新增分类"创建</div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="btn-click">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" class="btn-click">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '../../api/category'
import { pageNotes } from '../../api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref(null)
const form = reactive({ name: '', sortOrder: 0 })

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await getCategoryList()
    const categories = res.data || []
    categories.forEach(cat => { cat._expanded = false; cat._notes = []; cat._noteCount = 0 })
    list.value = categories
    // 并行加载笔记数量
    await Promise.all(categories.map(cat =>
      pageNotes({ page: 1, size: 1, categoryId: cat.id })
        .then(noteRes => { cat._noteCount = noteRes.data?.total || 0 })
        .catch(() => { cat._noteCount = 0 })
    ))
  } catch {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

async function toggleExpand(item) {
  if (item._expanded) {
    item._expanded = false
    return
  }
  // 加载该分类下的笔记
  const res = await pageNotes({ page: 1, size: 50, categoryId: item.id })
  item._notes = res.data?.records || []
  item._expanded = true
}

function openDialog(item) {
  editItem.value = item || null
  form.name = item ? item.name : ''
  form.sortOrder = item ? item.sortOrder : 0
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('请输入分类名称')
  saving.value = true
  try {
    if (editItem.value) {
      await updateCategory({ id: editItem.value.id, name: form.name, sortOrder: form.sortOrder })
      ElMessage.success('更新成功')
    } else {
      await createCategory({ name: form.name, sortOrder: form.sortOrder })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(item) {
  await ElMessageBox.confirm(`确定删除分类"${item.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteCategory(item.id)
    ElMessage.success('删除成功')
    loadList()
  } catch {
    ElMessage.error('删除失败')
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

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-block {
  animation: listItemIn 0.3s ease backwards;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.category-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.expand-icon {
  transition: transform 0.25s ease;
  font-size: 13px;
  color: var(--text-light);
}

.expand-icon.expanded {
  transform: rotate(90deg);
  color: var(--primary);
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.category-count {
  font-size: 11px;
  color: var(--primary-dark);
  background: var(--primary-bg);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.category-actions {
  display: flex;
  gap: 6px;
}

/* 下拉笔记列表 */
.note-dropdown {
  margin: 4px 0 4px 28px;
  padding-left: 14px;
  border-left: 2px solid rgba(106, 173, 207, 0.2);
}

.note-sub-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.note-sub-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  border: 1px solid rgba(74, 60, 46, 0.03);
}

.note-sub-title {
  color: var(--text-primary);
  font-weight: 500;
}

.note-sub-time {
  color: var(--text-light);
  font-size: 12px;
}

.note-sub-empty {
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-light);
}

.empty-tip {
  text-align: center;
  padding: 50px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
