function getBaseUrl() {
  const app = getApp && getApp()
  const base = app && app.globalData ? app.globalData.baseUrl : ''
  return String(base || '').trim().replace(/\/$/, '')
}

function isLocalHost(hostname) {
  const host = String(hostname || '').toLowerCase()
  return host === 'localhost' || host === '127.0.0.1' || host === '::1'
}

function isDevEnv() {
  try {
    const info = wx.getAccountInfoSync && wx.getAccountInfoSync()
    const version = info && info.miniProgram ? info.miniProgram.envVersion : ''
    return version !== 'release'
  } catch (e) {
    return true
  }
}

function sanitizeRawUrl(rawUrl) {
  return String(rawUrl || '').trim().replace(/\\/g, '/')
}

function getAbsoluteUrlOrNull(urlText) {
  try {
    return new URL(urlText)
  } catch (e) {
    return null
  }
}

function rewriteLocalAbsoluteUrl(originalUrl, baseUrl) {
  const current = getAbsoluteUrlOrNull(originalUrl)
  const base = getAbsoluteUrlOrNull(baseUrl)
  if (!current || !base) return originalUrl
  if (!isLocalHost(current.hostname) || isLocalHost(base.hostname)) return originalUrl
  return `${base.origin}${current.pathname}${current.search}${current.hash}`
}

function normalizeMediaUrl(rawUrl) {
  const original = sanitizeRawUrl(rawUrl)
  if (!original) {
    return { url: '', reason: 'empty', warn: false }
  }
  if (original.startsWith('data:image/')) {
    return { url: original, reason: 'data-uri', warn: false }
  }
  if (original.startsWith('https://')) {
    return { url: original, reason: 'https', warn: false }
  }
  if (original.startsWith('//')) {
    return { url: `https:${original}`, reason: 'protocol-relative', warn: false }
  }
  if (original.startsWith('http://')) {
    const baseUrl = getBaseUrl()
    const rewritten = rewriteLocalAbsoluteUrl(original, baseUrl)
    const warn = rewritten.startsWith('http://')
    return { url: rewritten, reason: rewritten === original ? 'http' : 'http-rewritten', warn }
  }

  const baseUrl = getBaseUrl()
  if (!baseUrl) {
    return { url: original, reason: 'raw-no-base', warn: true }
  }
  const normalizedPath = original.startsWith('./') ? original.slice(2) : original
  if (normalizedPath.startsWith('/')) {
    return { url: `${baseUrl}${normalizedPath}`, reason: 'relative-root', warn: false }
  }
  return { url: `${baseUrl}/${normalizedPath}`, reason: 'relative-path', warn: false }
}

function pickTeacherImage(item) {
  if (!item || typeof item !== 'object') return ''
  const keys = [
    'teacherPhoto',
    'userPortrait',
    'teacherAvatar',
    'avatar',
    'avatarUrl',
    'photoUrl',
    'headImgUrl',
    'headImage'
  ]
  for (let i = 0; i < keys.length; i += 1) {
    const val = sanitizeRawUrl(item[keys[i]])
    if (val) return val
  }
  return ''
}

function logMediaDebug(tag, payload) {
  if (!isDevEnv()) return
  try {
    console.info(`[media-debug] ${tag}`, payload)
  } catch (e) {
    // ignore debug log failures
  }
}

module.exports = {
  normalizeMediaUrl,
  logMediaDebug,
  pickTeacherImage
}
