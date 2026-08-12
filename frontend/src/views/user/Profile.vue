<template>
  <div class="profile-page">
    <!-- 头像选择区域 -->
    <div class="profile-card card-hover">
      <div class="avatar-area">
        <div class="avatar-display" @click="showAvatarPicker = true">
          <span class="avatar-emoji-big">{{ currentAvatar }}</span>
          <div class="avatar-edit-hint">点击更换</div>
        </div>
        <h3>{{ userStore.userInfo.nickname }}</h3>
        <p class="username">@{{ userStore.userInfo.username }}</p>
      </div>
    </div>

    <!-- 头像选择弹窗 -->
    <el-dialog v-model="showAvatarPicker" title="选择头像" width="420px">
      <div class="avatar-section">
        <p class="avatar-label">👦 男生头像</p>
        <div class="avatar-grid">
          <div
            v-for="a in boyAvatars"
            :key="a"
            class="avatar-option btn-click"
            :class="{ selected: selectedAvatar === a }"
            @click="selectedAvatar = a"
          >
            {{ a }}
          </div>
        </div>
      </div>
      <div class="avatar-section">
        <p class="avatar-label">👧 女生头像</p>
        <div class="avatar-grid">
          <div
            v-for="a in girlAvatars"
            :key="a"
            class="avatar-option btn-click"
            :class="{ selected: selectedAvatar === a }"
            @click="selectedAvatar = a"
          >
            {{ a }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAvatarPicker = false" class="btn-click">取消</el-button>
        <el-button type="primary" @click="confirmAvatar" class="btn-click">确定</el-button>
      </template>
    </el-dialog>

    <!-- 个人信息编辑 -->
    <div class="section">
      <h4 class="section-title">📝 个人信息</h4>
      <el-form :model="infoForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="infoForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-click" :loading="infoSaving" @click="handleUpdateInfo">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 修改密码 -->
    <div class="section">
      <h4 class="section-title">🔒 修改密码</h4>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20位）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-click" :loading="pwdSaving" @click="handleUpdatePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getUserInfo, updateUserInfo, updatePassword } from '../../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const pwdFormRef = ref(null)
const infoSaving = ref(false)
const pwdSaving = ref(false)
const showAvatarPicker = ref(false)
const selectedAvatar = ref('')

const infoForm = reactive({ username: '', nickname: '', email: '', avatar: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

// 可爱风男生头像
const boyAvatars = ['👦', '🧑‍🎓', '👨‍💻', '🤴', '🧑‍🚀', '🧑‍🎨', '🐻', '🐼']
// 可爱风女生头像
const girlAvatars = ['👧', '👩‍🎓', '👩‍💻', '👸', '🧚‍♀️', '👩‍🎨', '🐰', '🐱']

const currentAvatar = computed(() => {
  return infoForm.avatar || '🧑‍🎓'
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

onMounted(async () => {
  const res = await getUserInfo()
  const user = res.data
  infoForm.username = user.username || ''
  infoForm.nickname = user.nickname || ''
  infoForm.email = user.email || ''
  infoForm.avatar = user.avatar || ''
  selectedAvatar.value = user.avatar || '🧑‍🎓'
})

function confirmAvatar() {
  infoForm.avatar = selectedAvatar.value
  showAvatarPicker.value = false
}

async function handleUpdateInfo() {
  if (!infoForm.username || infoForm.username.trim().length < 3) {
    return ElMessage.warning('用户名至少3个字符')
  }
  infoSaving.value = true
  try {
    const res = await updateUserInfo(infoForm)
    userStore.updateInfo({
      username: res.data.username,
      nickname: res.data.nickname,
      email: res.data.email,
      avatar: res.data.avatar
    })
    ElMessage.success('保存成功')
  } finally {
    infoSaving.value = false
  }
}

async function handleUpdatePwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdSaving.value = true
  try {
    await updatePassword(pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border-radius: 16px;
  padding: 28px;
  text-align: center;
  color: white;
}

.avatar-display {
  cursor: pointer;
  display: inline-block;
  position: relative;
  transition: transform 0.2s;
}

.avatar-display:hover {
  transform: scale(1.08);
}

.avatar-emoji-big {
  font-size: 56px;
  display: block;
  margin-bottom: 4px;
}

.avatar-edit-hint {
  font-size: 11px;
  opacity: 0.7;
  margin-bottom: 8px;
}

.profile-card h3 {
  font-size: 20px;
  margin-bottom: 4px;
}

.username {
  font-size: 13px;
  opacity: 0.8;
}

/* 头像选择弹窗 */
.avatar-section {
  margin-bottom: 16px;
}

.avatar-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.avatar-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.avatar-option {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  border-radius: 14px;
  border: 2px solid transparent;
  background: rgba(90, 74, 58, 0.04);
  cursor: pointer;
  transition: all 0.2s;
}

.avatar-option:hover {
  background: rgba(126, 184, 218, 0.12);
  border-color: var(--primary-light);
}

.avatar-option.selected {
  border-color: var(--primary);
  background: rgba(126, 184, 218, 0.18);
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.2);
}

.section {
  background: white;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}
</style>
