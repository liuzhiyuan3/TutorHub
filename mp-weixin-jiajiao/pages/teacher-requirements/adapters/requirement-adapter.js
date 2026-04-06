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

function parseRequirementStatus(item) {
  const numeric = toNumberOrNull(item && item.requirementStatus)
  if (numeric !== null) return numeric
  const text = String(
    (item && (
      item.requirementStatusText ||
      item.requirementStatus ||
      item.statusText ||
      item.status ||
      item.parentOrderStageText ||
      item.parentOrderStage
    )) || ''
  ).trim().toUpperCase()
  if (text === 'WAITING' || text === '待接单' || text === 'PUBLISHED_WAITING') return 0
  if (text === 'RECEIVED' || text === '已接单' || text === 'MATCHED' || text === 'WAITING_CONFIRM') return 1
  if (text === 'IN_PROGRESS' || text === '进行中' || text === 'IN_SERVICE') return 1
  if (text === 'FINISHED' || text === '已完成' || text === 'DONE') return 2
  if (text === 'CANCELED' || text === '已取消' || text === 'CLOSED') return 3
  return 0
}

function parseTutoringMethod(item) {
  const numeric = toNumberOrNull(item && item.requirementTutoringMethod)
  if (numeric !== null) return numeric
  const text = String(
    (item && (item.requirementTutoringMethodText || item.tutoringMethodText || item.tutoringMethod || item.method)) || ''
  ).trim().toUpperCase()
  if (text === 'ONLINE' || text === '线上') return 0
  if (text === 'OFFLINE' || text === '线下') return 1
  if (text === 'BOTH' || text === 'ONLINE_OFFLINE' || text === '线上线下') return 2
  if (text.includes('线上') && text.includes('线下')) return 2
  if (text.includes('线上')) return 0
  if (text.includes('线下')) return 1
  return 2
}

function parseVisibility(rawVisibility) {
  const text = String(rawVisibility || '').trim().toUpperCase()
  if (!text) return 'VISIBLE'
  if (text === 'VISIBLE') return 'VISIBLE'
  if (text === 'WAITING' || text === 'PENDING' || text === 'IN_REVIEW' || text === 'AUDITING') return 'PENDING'
  if (text === 'REJECTED' || text === 'DENIED') return 'REJECTED'
  if (text === 'HIDDEN' || text === 'INVISIBLE') return 'HIDDEN'
  return 'VISIBLE'
}

function parseUrgencyText(item) {
  const text = String((item && item.requirementUrgencyText) || '').trim().toUpperCase()
  if (text === 'URGENT') return '紧急'
  if (text === 'FAST') return '加急'
  return '普通'
}

function buildReceiveActionText(item) {
  const status = item && item.requirementStatus
  if (status !== 0) return '不可接单'
  const visibility = String((item && item.teacherProfileVisibility) || 'HIDDEN').toUpperCase()
  if (visibility === 'VISIBLE') return '我要接单'
  if (visibility === 'WAITING' || visibility === 'PENDING' || visibility === 'IN_REVIEW' || visibility === 'AUDITING') {
    return '资料审核中'
  }
  if (visibility === 'REJECTED' || visibility === 'DENIED') {
    return '资料被驳回，去完善'
  }
  return '完善资料后可接单'
}

function normalizeRequirementItem(item) {
  const status = parseRequirementStatus(item)
  const salary = toNumberOrNull(item && (item.requirementSalary ?? item.orderAmount ?? item.unitPrice))
  const budgetMin = toNumberOrNull(item && item.requirementBudgetMin)
  const budgetMax = toNumberOrNull(item && item.requirementBudgetMax)
  const visibility = parseVisibility(item && item.teacherProfileVisibility)
  const resolvedTitle = decodeMaybeMojibake(
    (item && (item.requirementTitle || item.title || item.subjectName || item.orderNumber)) || ''
  )
  const resolvedGrade = decodeMaybeMojibake(
    (item && (item.requirementGrade || item.grade || item.subjectName)) || ''
  )
  const resolvedAddress = decodeMaybeMojibake(
    (item && (item.requirementAddress || item.address || item.regionName)) || ''
  )
  const statusTextSource = decodeMaybeMojibake(
    (item && (item.requirementStatusText || item.statusText || item.parentOrderStageText)) || ''
  )
  const normalized = {
    ...item,
    id: (item && (item.id || item.requirementId || item.bizId)) || '',
    requirementTitle: resolvedTitle || '未命名需求',
    requirementGrade: resolvedGrade || '未填写',
    requirementAddress: resolvedAddress || '-',
    requirementStatus: status,
    requirementStatusText: statusTextSource || '',
    requirementTutoringMethod: parseTutoringMethod(item),
    requirementUrgencyText: parseUrgencyText(item),
    requirementSalary: salary,
    requirementBudgetMin: budgetMin !== null ? budgetMin : (salary !== null ? salary : 0),
    requirementBudgetMax: budgetMax !== null ? budgetMax : (salary !== null ? salary : 0),
    teacherProfileVisibility: visibility,
    expectedTimeSlotsText: Array.isArray(item && item.requirementExpectedTimeSlots)
      ? item.requirementExpectedTimeSlots.map((slot) => decodeMaybeMojibake(slot)).join(' / ')
      : ''
  }
  normalized.receiveActionText = buildReceiveActionText(normalized)
  return normalized
}

module.exports = {
  normalizeRequirementItem
}

