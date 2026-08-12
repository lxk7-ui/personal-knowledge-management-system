<template>
  <div class="statistics-page" v-loading="loading">
    <h3 class="page-heading">📊 数据统计</h3>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card card-hover" style="--accent:#7eb8da">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalUsers || 0 }}</span>
          <span class="stat-label">用户总数</span>
        </div>
      </div>
      <div class="stat-card card-hover" style="--accent:#8cc7a1">
        <div class="stat-icon">📝</div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalNotes || 0 }}</span>
          <span class="stat-label">笔记总数</span>
        </div>
      </div>
      <div class="stat-card card-hover" style="--accent:#f0c78a">
        <div class="stat-icon">📁</div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalCategories || 0 }}</span>
          <span class="stat-label">分类总数</span>
        </div>
      </div>
      <div class="stat-card card-hover" style="--accent:#e8a0a0">
        <div class="stat-icon">🏷️</div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalTags || 0 }}</span>
          <span class="stat-label">标签总数</span>
        </div>
      </div>
    </div>

    <!-- 最近7天笔记趋势（纯CSS柱状图） -->
    <div class="chart-section">
      <h4>📈 最近7天新增笔记</h4>
      <div class="bar-chart">
        <div v-for="day in dailyNotes" :key="day.date" class="bar-item">
          <div class="bar-value">{{ day.count }}</div>
          <div class="bar-fill" :style="{ height: getBarHeight(day.count) + 'px' }"></div>
          <div class="bar-label">{{ day.date.substring(5) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatistics } from '../../api/admin'
import { ElMessage } from 'element-plus'

const stats = ref({})
const dailyNotes = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    stats.value = res.data || {}
    dailyNotes.value = res.data?.dailyNotes || []
  } catch {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})

function getBarHeight(count) {
  const max = Math.max(...dailyNotes.value.map(d => d.count), 1)
  return Math.max((count / max) * 140, 4)
}
</script>

<style scoped>
.page-heading {
  font-size: 15px;
  color: var(--text-primary);
  margin-bottom: 20px;
  font-weight: 600;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(74, 60, 46, 0.04);
  border-left: 3px solid var(--accent);
}

.stat-icon {
  font-size: 30px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.08));
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 11px;
  color: var(--text-light);
  letter-spacing: 0.5px;
}

.chart-section {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(74, 60, 46, 0.04);
}

.chart-section h4 {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 20px;
  font-weight: 600;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding-top: 20px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}

.bar-fill {
  width: 32px;
  background: linear-gradient(180deg, var(--primary-light), var(--primary));
  border-radius: 6px 6px 2px 2px;
  transition: height 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 4px;
}

.bar-label {
  font-size: 11px;
  color: var(--text-light);
}
</style>
