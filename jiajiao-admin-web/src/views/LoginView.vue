<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminLogin } from '../api'
import { bootstrapAdminSession, clearAdminSession } from '../stores/adminSession'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const error = ref('')
const form = ref({
  account: 'admin',
  password: '123456'
})

const canSubmit = computed(() => {
  return !loading.value && form.value.account.trim() && form.value.password.trim()
})

async function submit() {
  if (!canSubmit.value) return

  error.value = ''
  loading.value = true
  try {
    const data = await adminLogin({
      account: form.value.account.trim(),
      password: form.value.password
    })
    localStorage.setItem('admin_token', data.token)
    await bootstrapAdminSession(true)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect)
  } catch (e) {
    clearAdminSession()
    error.value = e.message || '登录失败，请检查账号与密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-brand">
      <div class="brand-badge">Teacher Console</div>
      <h1>家教平台管理端</h1>
      <p>面向运营、教务和内容管理的一体化后台，支持稳定鉴权与实时业务看板。</p>
      <ul>
        <li>统一业务面板与图表看板</li>
        <li>完整登录态校验与会话失效回收</li>
        <li>支持多终端响应式访问</li>
      </ul>
    </div>

    <div class="login-card card">
      <h2>欢迎登录</h2>
      <p class="tip">请输入管理员账号信息继续访问后台系统</p>

      <label class="field-label" for="account">账号</label>
      <input
        id="account"
        v-model="form.account"
        class="input"
        autocomplete="username"
        placeholder="请输入管理员账号"
        @keydown.enter="submit"
      />

      <label class="field-label" for="password">密码</label>
      <input
        id="password"
        v-model="form.password"
        type="password"
        class="input"
        autocomplete="current-password"
        placeholder="请输入登录密码"
        @keydown.enter="submit"
      />

      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn submit" :disabled="!canSubmit" @click="submit">
        {{ loading ? '登录中...' : '登录管理端' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: center;
  gap: 36px;
  padding: 36px;
  background:
    radial-gradient(1200px 600px at -10% 110%, rgba(34, 197, 94, 0.18), transparent 70%),
    radial-gradient(1000px 600px at 100% -10%, rgba(59, 130, 246, 0.22), transparent 62%),
    linear-gradient(135deg, #f4f8ff 0%, #eef5ff 45%, #f7fbff 100%);
}

.login-brand {
  color: #123056;
  animation: rise-in 420ms ease-out;
}

.login-brand h1 {
  margin: 14px 0 10px;
  font-size: 46px;
  line-height: 1.1;
  letter-spacing: 0.4px;
}

.login-brand p {
  margin: 0 0 18px;
  max-width: 620px;
  color: #4d6381;
  font-size: 16px;
}

.login-brand ul {
  margin: 0;
  padding-left: 20px;
  color: #385477;
  line-height: 1.9;
}

.brand-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(59, 130, 246, 0.25);
  background: rgba(255, 255, 255, 0.72);
  color: #1f4f8e;
  font-size: 12px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.login-card {
  width: min(460px, 100%);
  padding: 30px;
  border-radius: 24px;
  animation: rise-in 420ms ease-out;
}

.login-card h2 {
  margin: 0;
  font-size: 28px;
  color: #0f2d53;
}

.tip {
  margin: 8px 0 18px;
  color: #6a819f;
}

.field-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #2c4668;
  font-weight: 700;
}

.input {
  margin-bottom: 14px;
}

.submit {
  width: 100%;
  margin-top: 6px;
  min-height: 42px;
}

.error {
  margin: 4px 0 0;
  color: #b91c1c;
  font-size: 13px;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
  }

  .login-brand h1 {
    font-size: 34px;
  }

  .login-card {
    margin: 0 auto;
  }
}
</style>



