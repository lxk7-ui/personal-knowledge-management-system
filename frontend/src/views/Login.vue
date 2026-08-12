<template>
  <div class="login-page desk-background">
    <!-- 登录/注册书本容器 -->
    <div class="login-book animate-in">
      <div class="login-book-inner">
        <!-- 左侧装饰 -->
        <div class="login-decor">
          <!-- 浮动装饰元素 -->
          <div class="float-circle c1"></div>
          <div class="float-circle c2"></div>
          <div class="float-circle c3"></div>
          <div class="decor-content">
            <div class="decor-icon-group">
              <span class="decor-icon i1">📖</span>
              <span class="decor-icon i2">✏️</span>
              <span class="decor-icon i3">💡</span>
            </div>
            <h2>个人知识管理系统</h2>
            <div class="decor-divider"></div>
            <p>记录知识，整理思绪</p>
            <p class="decor-sub">让学习更有条理</p>
            <div class="decor-tags">
              <span class="decor-tag">笔记</span>
              <span class="decor-tag">分类</span>
              <span class="decor-tag">标签</span>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域（滚动切换） -->
        <div class="login-form-area">
          <div class="form-slider" :class="{ 'show-register': isRegister }">
            <!-- 登录表单 -->
            <div class="form-panel">
              <h3 class="form-title">欢迎回来</h3>
              <p class="form-subtitle">登录你的知识小屋</p>
              <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
                <el-form-item prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入用户名"
                    prefix-icon="User"
                    size="large"
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    prefix-icon="Lock"
                    size="large"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    class="form-btn btn-click"
                    :loading="loginLoading"
                    @click="handleLogin"
                  >
                    登 录
                  </el-button>
                </el-form-item>
              </el-form>
              <div class="form-switch">
                还没有账号？
                <span class="switch-link" @click="isRegister = true">去注册</span>
              </div>
            </div>

            <!-- 注册表单 -->
            <div class="form-panel">
              <h3 class="form-title">加入我们</h3>
              <p class="form-subtitle">创建你的知识小屋</p>
              <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @keyup.enter="handleRegister">
                <el-form-item prop="username">
                  <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名（3-20位）"
                    prefix-icon="User"
                    size="large"
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请输入密码（6-20位）"
                    prefix-icon="Lock"
                    size="large"
                    show-password
                  />
                </el-form-item>
                <el-form-item prop="nickname">
                  <el-input
                    v-model="registerForm.nickname"
                    placeholder="请输入昵称（选填）"
                    prefix-icon="Star"
                    size="large"
                  />
                </el-form-item>
                <el-form-item prop="email">
                  <el-input
                    v-model="registerForm.email"
                    placeholder="请输入邮箱（选填）"
                    prefix-icon="Message"
                    size="large"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    class="form-btn btn-click"
                    :loading="registerLoading"
                    @click="handleRegister"
                  >
                    注 册
                  </el-button>
                </el-form-item>
              </el-form>
              <div class="form-switch">
                已有账号？
                <span class="switch-link" @click="isRegister = false">去登录</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login, register } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', email: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    const res = await login(loginForm)
    userStore.login(res.data)
    ElMessage.success('登录成功')
    router.push(res.data.role === 'ADMIN' ? '/admin' : '/user')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await register(registerForm)
    ElMessage.success('注册成功，请登录')
    isRegister.value = false
    loginForm.username = registerForm.username
    loginForm.password = ''
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 入场动画 */
.animate-in {
  animation: bookOpen 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes bookOpen {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.login-book {
  width: 780px;
  min-height: 490px;
  background: var(--page-color);
  border-radius: 6px 20px 20px 6px;
  box-shadow:
    -6px 0 18px rgba(74, 60, 46, 0.2),
    0 20px 60px rgba(74, 60, 46, 0.25),
    0 0 0 1px rgba(74, 60, 46, 0.05);
  overflow: hidden;
  position: relative;
}

.login-book::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 14px;
  background: linear-gradient(90deg,
    rgba(74, 60, 46, 0.12) 0%,
    rgba(74, 60, 46, 0.04) 50%,
    transparent 100%
  );
  z-index: 2;
}

.login-book-inner {
  display: flex;
  height: 100%;
  min-height: 490px;
}

/* 左侧装饰区 */
.login-decor {
  width: 300px;
  background: linear-gradient(160deg, #5b9fc0 0%, #4a8fb5 40%, #3a7a9e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 28px;
  position: relative;
  overflow: hidden;
}

/* 浮动圆圈装饰 */
.float-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: white;
}

.c1 {
  width: 180px;
  height: 180px;
  top: -40px;
  right: -40px;
  animation: floatSlow 8s ease-in-out infinite;
}

.c2 {
  width: 120px;
  height: 120px;
  bottom: -20px;
  left: -30px;
  animation: floatSlow 6s ease-in-out infinite reverse;
}

.c3 {
  width: 60px;
  height: 60px;
  top: 50%;
  right: 20px;
  opacity: 0.05;
  animation: floatSlow 10s ease-in-out infinite;
}

@keyframes floatSlow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.decor-content {
  text-align: center;
  color: var(--text-white);
  position: relative;
  z-index: 1;
}

.decor-icon-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 22px;
}

.decor-icon {
  font-size: 32px;
  display: inline-block;
  filter: drop-shadow(0 3px 6px rgba(0,0,0,0.15));
}

.i1 { animation: iconFloat 3s ease-in-out infinite; }
.i2 { animation: iconFloat 3s ease-in-out 0.4s infinite; }
.i3 { animation: iconFloat 3s ease-in-out 0.8s infinite; }

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.decor-content h2 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 1.5px;
}

.decor-divider {
  width: 36px;
  height: 3px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
  margin: 0 auto 14px;
}

.decor-content p {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
  line-height: 1.6;
}

.decor-sub {
  font-size: 12px;
  opacity: 0.6;
}

.decor-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 22px;
}

.decor-tag {
  font-size: 11px;
  padding: 4px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  letter-spacing: 0.5px;
}

/* 右侧表单区域 */
.login-form-area {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.form-slider {
  display: flex;
  width: 200%;
  height: 100%;
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-slider.show-register {
  transform: translateX(-50%);
}

.form-panel {
  width: 50%;
  padding: 48px 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 32px;
}

.form-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border: none;
  letter-spacing: 4px;
  transition: all 0.3s ease;
}

.form-btn:hover {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  box-shadow: 0 6px 20px rgba(106, 173, 207, 0.35);
  transform: translateY(-1px);
}

.form-switch {
  text-align: center;
  font-size: 13px;
  color: var(--text-light);
  margin-top: 18px;
}

.switch-link {
  color: var(--primary);
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  border-bottom: 1px solid transparent;
}

.switch-link:hover {
  color: var(--primary-dark);
  border-bottom-color: var(--primary-dark);
}

/* 表单输入框样式 */
.form-panel :deep(.el-form-item) {
  margin-bottom: 22px;
}

.form-panel :deep(.el-input__wrapper) {
  background: rgba(74, 60, 46, 0.025);
  border: 1.5px solid rgba(74, 60, 46, 0.08);
  border-radius: var(--radius-md);
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.form-panel :deep(.el-input__wrapper:hover) {
  background: white;
  border-color: rgba(106, 173, 207, 0.3);
}

.form-panel :deep(.el-input__wrapper.is-focus) {
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(106, 173, 207, 0.12);
}

.form-panel :deep(.el-input__prefix) {
  color: var(--text-light);
}
</style>
