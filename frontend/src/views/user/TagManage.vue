<template>
  <div class="tag-page" v-loading="loading">
    <div class="top-bar">
      <h3>🏷️ 我的标签</h3>
      <el-button type="primary" class="btn-click" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增标签
      </el-button>
    </div>

    <div v-if="list.length" class="tag-list">
      <div v-for="(item, index) in list" :key="item.id" class="tag-block" :style="{ animationDelay: index * 0.04 + 's' }">
        <div class="tag-item card-hover" @click="toggleExpand(item)">
          <div class="tag-info">
            <el-icon class="expand-icon" :class="{ expanded: item._expanded }"><ArrowRight /></el-icon>
            <div class="tag-color-dot" :style="{ background: item.color }"></div>
            <span class="tag-name">{{ item.name }}</span>
            <span class="tag-count">{{ item._noteCount || 0 }} 篇</span>
          </div>
          <div class="tag-actions" @click.stop>
            <el-icon class="action-icon" @click="openDialog(item)"><Edit /></el-icon>
            <el-icon class="action-icon danger" @click="handleDelete(item)"><Delete /></el-icon>
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
            <div v-else class="note-sub-empty">该标签下暂无笔记</div>
          </div>
        </transition>
      </div>
    </div>
    <div v-else class="empty-tip">暂无标签，点击"新增标签"创建</div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑标签' : '新增标签'" width="380px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
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
import { getTagList, createTag, updateTag, deleteTag } from '../../api/tag'
import { pageNotes } from '../../api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref(null)
const form = reactive({ name: '', color: '#409EFF' })

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await getTagList()
    const tags = res.data || []
    tags.forEach(tag => { tag._expanded = false; tag._notes = []; tag._noteCount = 0 })
    list.value = tags
    await Promise.all(tags.map(tag =>
      pageNotes({ page: 1, size: 1, tagId: tag.id })
        .then(noteRes => { tag._noteCount = noteRes.data?.total || 0 })
        .catch(() => { tag._noteCount = 0 })
    ))
  } catch {
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

async function toggleExpand(item) {
  if (item._expanded) {
    item._expanded = false
    return
  }
  const res = await pageNotes({ page: 1, size: 50, tagId: item.id })
  item._notes = res.data?.records || []
  item._expanded = true
}

function openDialog(item) {
  editItem.value = item || null
  form.name = item ? item.name : ''
  form.color = item ? item.color : '#409EFF'
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name.trim()) return ElMessage.warning('请输入标签名称')
  saving.value = true
  try {
    if (editItem.value) {
      await updateTag({ id: editItem.value.id, name: form.name, color: form.color })
      ElMessage.success('更新成功')
    } else {
      await createTag({ name: form.name, color: form.color })
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
  await ElMessageBox.confirm(`确定删除标签"${item.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteTag(item.id)
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

.tag-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-block {
  animation: listItemIn 0.3s ease backwards;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.tag-info {
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

.tag-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}

.tag-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.tag-count {
  font-size: 11px;
  color: var(--primary-dark);
  background: var(--primary-bg);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.tag-actions {
  display: flex;
  gap: 8px;
}

.action-icon {
  cursor: pointer;
  font-size: 25px;
  color: var(--text-light);
  transition: all 0.2s;
  padding: 6px;
  border-radius: 6px;
}

.action-icon:hover {
  color: var(--primary);
  background: var(--primary-bg);
}

.action-icon.danger:hover {
  color: var(--danger);
  background: var(--danger-bg);
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
