<template>
  <div class="admin-tag-page" v-loading="loading">
    <div class="top-bar">
      <h3>🏷️ 标签管理</h3>
    </div>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="标签名称" />
      <el-table-column prop="color" label="颜色" width="100">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <span class="color-dot" :style="{ background: row.color }"></span>
            {{ row.color }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="所属用户ID" width="110" />
      <el-table-column prop="createTime" label="创建时间" width="120">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="danger" class="btn-click" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
import { adminTagPage, adminDeleteTag } from '../../api/admin'
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
    const res = await adminTagPage({ page: currentPage.value, size: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除标签"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await adminDeleteTag(row.id)
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
  font-size: 16px;
  color: var(--text-primary);
}

.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  display: inline-block;
}
</style>
