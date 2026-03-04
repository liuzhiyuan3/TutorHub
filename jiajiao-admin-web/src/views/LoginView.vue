<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../api'

const router = useRouter()
const loading = ref(false)
const form = ref({
  account: 'admin',
  password: '123456'
})
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const data = await adminLogin(form.value)
    localStorage.setItem('admin_token', data.token)
    router.push('/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-card card">
      <h2>家教平台管理端</h2>
      <p class="tip">输入管理员账号登录</p>
      <input v-model="form.account" class="input" placeholder="账号" />
      <input v-model="form.password" type="password" class="input" placeholder="密码" />
      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn submit" :disabled="loading" @click="submit">{{ loading ? '登录中...' : '登录' }}</button>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top right, #bfdbfe, #e2e8f0 70%);
}
.login-card {
  width: 360px;
  padding: 28px;
}
.tip {
  color: #6b7280;
  margin-bottom: 16px;
}
.input {
  margin-bottom: 12px;
}
.submit {
  width: 100%;
  margin-top: 8px;
}
.error {
  color: #dc2626;
}
</style>
