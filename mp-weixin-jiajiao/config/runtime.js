const BASE_URL_OVERRIDE_KEY = 'runtimeBaseUrlOverride'

const ENV_BASE_URL_MAP = {
  develop: 'http://127.0.0.1:8080',
  trial: 'https://10.107.179.114:8080',
  release: 'https://your-prod-api.example.com'
}

function normalizeBaseUrl(url) {
  const text = String(url || '').trim()
  if (!text) return ''
  return text.replace(/\/+$/, '')
}

function readEnvVersion() {
  try {
    if (typeof wx !== 'undefined' && wx.getAccountInfoSync) {
      const info = wx.getAccountInfoSync()
      const envVersion = info && info.miniProgram && info.miniProgram.envVersion
      if (envVersion) return String(envVersion)
    }
  } catch (e) {}
  return 'develop'
}

function readOverrideBaseUrl() {
  try {
    if (typeof wx === 'undefined' || !wx.getStorageSync) return ''
    return normalizeBaseUrl(wx.getStorageSync(BASE_URL_OVERRIDE_KEY))
  } catch (e) {
    return ''
  }
}

function isLocalLoopbackHost(hostname) {
  const host = String(hostname || '').trim().toLowerCase()
  if (!host) return false
  return host === 'localhost' || host === '127.0.0.1' || host === '::1'
}

function shouldIgnoreOverrideInDevelop(overrideUrl, envVersion) {
  if (envVersion !== 'develop') return false
  const text = normalizeBaseUrl(overrideUrl)
  if (!text) return false
  try {
    const parsed = new URL(text)
    return !isLocalLoopbackHost(parsed.hostname)
  } catch (e) {
    // 非法 URL 在开发环境下也不使用覆盖，回退到默认配置
    return true
  }
}

function resolveBaseUrl() {
  const envVersion = readEnvVersion()
  const overrideUrl = readOverrideBaseUrl()
  if (overrideUrl && !shouldIgnoreOverrideInDevelop(overrideUrl, envVersion)) {
    return {
      baseUrl: overrideUrl,
      source: 'override',
      envVersion
    }
  }
  const fromMap = normalizeBaseUrl(ENV_BASE_URL_MAP[envVersion])
  const fallback = normalizeBaseUrl(ENV_BASE_URL_MAP.develop)
  return {
    baseUrl: fromMap || fallback || 'http://127.0.0.1:8080',
    source: fromMap ? `env:${envVersion}` : 'fallback',
    envVersion
  }
}

function getRuntimeConfig() {
  const resolved = resolveBaseUrl()
  return {
    baseUrl: resolved.baseUrl,
    amapKey: '',
    baseUrlSource: resolved.source,
    envVersion: resolved.envVersion,
    baseUrlOverrideKey: BASE_URL_OVERRIDE_KEY
  }
}

module.exports = getRuntimeConfig()
module.exports.getRuntimeConfig = getRuntimeConfig
module.exports.BASE_URL_OVERRIDE_KEY = BASE_URL_OVERRIDE_KEY
