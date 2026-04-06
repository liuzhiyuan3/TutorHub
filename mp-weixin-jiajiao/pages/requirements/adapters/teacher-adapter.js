const { normalizeMediaUrl, pickTeacherImage } = require('../../../utils/media-url')

function toNumberOrNull(value) {
  if (value === null || value === undefined || value === '') return null
  const n = Number(value)
  return Number.isNaN(n) ? null : n
}

function decodeMaybeMojibake(value) {
  const raw = value === null || value === undefined ? '' : String(value)
  if (!raw) return ''
  const hasMojibakeHint = /[\u0080-\u00FF]/.test(raw)
  if (!hasMojibakeHint) return raw
  try {
    const bytes = []
    for (let i = 0; i < raw.length; i += 1) {
      const code = raw.charCodeAt(i)
      if (code > 255) return raw
      let hex = code.toString(16).toUpperCase()
      if (hex.length < 2) hex = `0${hex}`
      bytes.push(`%${hex}`)
    }
    const decoded = decodeURIComponent(bytes.join(''))
    return decoded || raw
  } catch (e) {
    return raw
  }
}

function parseTeacherAuditStatus(item) {
  const numeric = toNumberOrNull(item && (item.teacherAuditStatus ?? item.auditStatus))
  if (numeric !== null) return numeric
  const text = String((item && (item.teacherAuditStatusText || item.auditStatusText || item.statusText || item.status)) || '')
    .trim().toUpperCase()
  if (text === 'APPROVED' || text === '通过' || text === '已通过') return 1
  if (text === 'REJECTED' || text === '拒绝' || text === '已拒绝') return 2
  return 1
}

function normalizeTeacherItem(item) {
  const subjectNames = Array.isArray(item && item.subjectNames)
    ? item.subjectNames.map((name) => decodeMaybeMojibake(name)).filter(Boolean)
    : (Array.isArray(item && item.subjects)
      ? item.subjects.map((s) => decodeMaybeMojibake((s && (s.name || s.subjectName)) || '')).filter(Boolean)
      : [])
  const regionNames = Array.isArray(item && item.regionNames)
    ? item.regionNames.map((name) => decodeMaybeMojibake(name)).filter(Boolean)
    : (Array.isArray(item && item.regions)
      ? item.regions.map((r) => decodeMaybeMojibake((r && (r.name || r.regionName)) || '')).filter(Boolean)
      : [])

  const teacherTeachingYears = toNumberOrNull(item && (item.teacherTeachingYears ?? item.teachingYears ?? item.years))
  const historyDealCount = toNumberOrNull(item && (item.historyDealCount ?? item.dealCount ?? item.teacherSuccessCount))
  const hireCount = toNumberOrNull(item && (item.hireCount ?? item.hiredCount ?? item.teacherHireCount))

  return {
    teacherPhoto: normalizeMediaUrl(
      (item && (item.teacherPhoto || item.avatarUrl || item.avatar || item.photo)) || pickTeacherImage(item)
    ).url,
    id: (item && (item.teacherId || item.id || item.userId)) || '',
    cardType: 'teacher',
    teacherName: decodeMaybeMojibake((item && (item.userName || item.teacherName || item.name || item.nickName)) || '教员') || '教员',
    teacherIdentity: decodeMaybeMojibake((item && (item.teacherIdentity || item.identity)) || '-') || '-',
    teacherSchool: decodeMaybeMojibake((item && (item.teacherSchool || item.school)) || '-') || '-',
    teacherMajor: decodeMaybeMojibake((item && (item.teacherMajor || item.major)) || '-') || '-',
    teacherTeachingYears: teacherTeachingYears === null ? 0 : teacherTeachingYears,
    teacherAuditStatus: parseTeacherAuditStatus(item),
    subjectText: subjectNames.length ? subjectNames.join('、') : '-',
    regionText: regionNames.length ? regionNames.join('、') : '-',
    hireCount: hireCount === null ? 0 : hireCount,
    historyDealCount: historyDealCount === null ? 0 : historyDealCount
  }
}

module.exports = {
  normalizeTeacherItem
}

