<template>
  <div class="system-log-page" v-loading="loading">
    <div class="top-bar">
      <h3>📋 系统日志</h3>
      <div class="top-actions">
        <el-input v-model="keyword" placeholder="搜索操作/用户名" prefix-icon="Search" clearable style="width:200px" @keyup.enter="loadList" @clear="loadList" />
        <el-button type="danger" class="btn-click" @click="handleClear">清空日志</el-button>
      </div>
    </div>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="操作用户" width="100" />
      <el-table-column prop="operation" label="操作描述" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="120" />
      <el-table-column prop="status" label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="150">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
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
import { getLogPage, clearLogs } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)
const loading = ref(false)

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await getLogPage({ page: currentPage.value, size: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

async function handleClear() {
  await ElMessageBox.confirm('确定清空所有日志吗？此操作不可恢复', '提示', { type: 'warning' })
  try {
    await clearLogs()
    ElMessage.success('清空成功')
    loadList()
  } catch {
    ElMessage.error('清空失败')
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
  margin-bottom: 18px;
}

.top-bar h3 {
  font-size: 16px;
  color: var(--text-primary);
}

.top-actions {
  display: flex;
  gap: 10px;
}
</style>
