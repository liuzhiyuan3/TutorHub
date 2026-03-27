import { ref } from 'vue'
import { adminMe } from '../api'

export const adminProfile = ref(null)
const bootstrapped = ref(false)
let bootstrapTask = null

export function clearAdminSession() {
  localStorage.removeItem('admin_token')
  adminProfile.value = null
  bootstrapped.value = false
}

export async function fetchAdminProfile() {
  const data = await adminMe()
  adminProfile.value = data
  return data
}

export async function bootstrapAdminSession(force = false) {
  if (bootstrapTask) return bootstrapTask
  if (!force && bootstrapped.value) return adminProfile.value

  const token = localStorage.getItem('admin_token')
  if (!token) {
    bootstrapped.value = false
    adminProfile.value = null
    return null
  }

  bootstrapTask = fetchAdminProfile()
    .catch((error) => {
      clearAdminSession()
      throw error
    })
    .finally(() => {
      bootstrapped.value = true
      bootstrapTask = null
    })

  return bootstrapTask
}
