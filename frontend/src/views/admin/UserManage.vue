<template>
  <div class="admin-user-page" v-loading="loading">
    <div class="top-bar">
      <h3>👥 用户管理</h3>
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" prefix-icon="Search" clearable style="width:220px" @keyup.enter="loadList" @clear="loadList" />
    </div>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            class="btn-click"
            @click="handleToggle(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            type="danger"
            class="btn-click"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
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
import { adminUserPage, toggleUserStatus, deleteUser } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await adminUserPage({ page: currentPage.value, size: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function handleToggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await toggleUserStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadList()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch {
    ElMessage.error('删除失败')
  }
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
</style>
