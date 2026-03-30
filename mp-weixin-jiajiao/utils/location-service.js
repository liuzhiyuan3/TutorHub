const { request } = require('./request')
const globalStore = require('./global-store')
const { getCurrentLocationWithAuth } = require('./location')

const REGION_CACHE_TTL = 30 * 60 * 1000
const REGION_SMART_REFRESH_TTL = 20 * 60 * 1000
let latestTaskId = 0
let regionResolvePromise = null

function logLocationTrace(event, reason) {
  try {
    const text = String(reason || 'unknown')
    console.info(`[location-trace] ${event}: ${text}`)
  } catch (e) {}
}

async function getSafeLocation() {
  const exact = await getCurrentLocationWithAuth()
  const latitude = Number(exact.latitude)
  const longitude = Number(exact.longitude)
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude) || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
    const err = new Error('invalid coordinate')
    err.reason = 'invalid_coordinate'
    throw err
  }
  return {
    latitude,
    longitude,
    accuracy: Number(exact.accuracy || 0),
    source: 'gps'
  }
}

function classifyLocationError(error) {
  if (error && error.reason) return String(error.reason)
  const text = String((error && (error.errMsg || error.message)) || '').toLowerCase()
  if (!text) return 'unknown'
  if (text.includes('cancel')) return 'cancel'
  if (text.includes('auth deny') || text.includes('auth denied') || text.includes('permission') || text.includes('authorize')) return 'auth'
  if (text.includes('timeout')) return 'timeout'
  if (text.includes('invalid coordinate')) return 'invalid_coordinate'
  if (text.includes('chooselocation')) return 'choose'
  if (text.includes('geocoder') || text.includes('reverse') || text.includes('region')) return 'geocode'
  if (text.includes('network') || text.includes('fail')) return 'network'
  return 'unknown'
}

function toLocationErrorMessage(error) {
  const reason = classifyLocationError(error)
  if (reason === 'auth') return '未授权定位，请在设置中开启后重试'
  if (reason === 'timeout') return '定位超时，请稍后重试'
  if (reason === 'invalid_coordinate') return '定位坐标异常，请重试或手动选点'
  if (reason === 'choose') return '地图选点失败，请稍后重试'
  if (reason === 'network') return '网络异常，请检查后重试'
  if (reason === 'geocode') return '地址解析失败，已记录坐标'
  return '定位失败，请稍后重试'
}

function reverseGeocode(location) {
  const app = getApp && getApp()
  const amapKey = String(
    (app && app.globalData && app.globalData.amapKey) || ''
  ).trim()
  const payload = {
    latitude: Number(location.latitude),
    longitude: Number(location.longitude)
  }
  if (amapKey) {
    payload.amapKey = amapKey
  }

  return request({
    url: '/api/location/reverse',
    method: 'POST',
    authMode: 'optional',
    data: payload
  }).then((data) => ({
    address: (data && typeof data === 'object' && data.address) || '',
    province: (data && typeof data === 'object' && data.province) || '',
    city: (data && typeof data === 'object' && data.city) || '',
    district: (data && typeof data === 'object' && data.district) || '',
    source: 'backend'
  })).catch(() => {
    const err = new Error('reverse geocode failed')
    err.reason = 'geocode'
    throw err
  })
}

function toFallbackMessage(reason) {
  const text = String(reason || '')
  if (!text) return '定位失败，已使用默认地区'
  if (text.includes('auth deny') || text.includes('authorize') || text.includes('permission') || text.includes('未授权')) {
    return '未授权定位，已使用默认地区'
  }
  if (text.includes('timeout')) return '定位超时，已使用默认地区'
  if (text.includes('地理') || text.includes('逆地理') || text.includes('geocode')) return '地区解析失败，已使用默认地区'
  return '定位失败，已使用默认地区'
}

async function loadRegions() {
  const data = await request({ url: '/api/home/filters', authMode: 'optional' })
  return (data && data.regions) || []
}

function normalizeText(text) {
  return String(text || '').replace(/市|区|县|自治州|自治县|特别行政区/g, '').trim()
}

function scoreRegionMatch(region, geo) {
  const regionName = normalizeText(region.regionName || region.name || '')
  const cityText = normalizeText(region.regionCity || '')
  const geoCity = normalizeText(geo.city || '')
  const geoDistrict = normalizeText(geo.district || '')
  let score = 0
  if (regionName && geoDistrict && geoDistrict.includes(regionName)) score += 4
  if (regionName && geoCity && geoCity.includes(regionName)) score += 2
  if (cityText && geoCity && geoCity.includes(cityText)) score += 3
  return score
}

function matchRegionCode(regions, geo) {
  if (!Array.isArray(regions) || !regions.length) return null
  let best = null
  let bestScore = -1
  regions.forEach((region) => {
    const score = scoreRegionMatch(region, geo)
    if (score > bestScore) {
      bestScore = score
      best = region
    }
  })
  if (!best) return null
  if (bestScore <= 0) return null
  return best
}

function buildFallbackRegion(reason) {
  return buildFallbackRegionWithLocation(reason, null)
}

function buildFallbackRegionWithLocation(reason, location) {
  const safeLatitude = location && Number.isFinite(Number(location.latitude)) ? Number(location.latitude) : null
  const safeLongitude = location && Number.isFinite(Number(location.longitude)) ? Number(location.longitude) : null
  const hasCoordinate = safeLatitude !== null && safeLongitude !== null
  const cached = globalStore.getRegion()
  if (cached && cached.regionCode && Date.now() - Number(cached.syncAt || 0) < REGION_CACHE_TTL) {
    return {
      ...cached,
      latitude: hasCoordinate ? safeLatitude : (cached.latitude == null ? null : Number(cached.latitude)),
      longitude: hasCoordinate ? safeLongitude : (cached.longitude == null ? null : Number(cached.longitude)),
      source: 'cache',
      fallbackReason: reason || ''
    }
  }
  if (hasCoordinate) {
    return {
      regionCode: '',
      regionName: '当前位置',
      province: '',
      city: '',
      district: '',
      latitude: safeLatitude,
      longitude: safeLongitude,
      source: 'gps_fallback',
      syncAt: Date.now(),
      fallbackReason: reason || ''
    }
  }
  return {
    regionCode: 'DEFAULT_CN',
    regionName: '全国',
    province: '',
    city: '',
    district: '',
    latitude: null,
    longitude: null,
    source: 'default',
    syncAt: Date.now(),
    fallbackReason: reason || ''
  }
}

async function resolveRegionWithFallback() {
  const taskId = ++latestTaskId
  let location = null
  try {
    location = await getSafeLocation()
    const geo = await reverseGeocode(location)
    const regions = await loadRegions()
    const matched = matchRegionCode(regions, geo)
    const resolved = {
      regionCode: matched ? matched.id : '',
      regionName: matched ? (matched.regionName || matched.name || geo.city || '未知区域') : (geo.city || '未知区域'),
      province: geo.province || '',
      city: geo.city || '',
      district: geo.district || '',
      latitude: location.latitude,
      longitude: location.longitude,
      source: 'gps',
      syncAt: Date.now()
    }
    if (taskId !== latestTaskId) {
      return globalStore.getRegion()
    }
    logLocationTrace('resolve_success', 'gps')
    return resolved
  } catch (e) {
    const reason = e && e.message ? e.message : 'region_resolve_failed'
    logLocationTrace('resolve_fallback', reason)
    return buildFallbackRegionWithLocation(reason, location)
  }
}

function hasRegionCacheValue(region) {
  if (!region || typeof region !== 'object') return false
  if (String(region.regionCode || '').trim()) return true
  if (String(region.regionName || '').trim()) return true
  const lat = Number(region.latitude)
  const lng = Number(region.longitude)
  if (Number.isFinite(lat) && Number.isFinite(lng)) return true
  return false
}

function isRegionCacheFresh(region, ttlMs) {
  if (!hasRegionCacheValue(region)) return false
  const syncAt = Number(region.syncAt || 0)
  if (!Number.isFinite(syncAt) || syncAt <= 0) return false
  const safeTtl = Number(ttlMs)
  const effectiveTtl = Number.isFinite(safeTtl) && safeTtl > 0 ? safeTtl : REGION_SMART_REFRESH_TTL
  return Date.now() - syncAt < effectiveTtl
}

async function resolveRegionSmart(options) {
  const config = options || {}
  const forceRefresh = !!config.forceRefresh
  const ttlMs = Number.isFinite(Number(config.ttlMs)) ? Number(config.ttlMs) : REGION_SMART_REFRESH_TTL
  const cached = globalStore.getRegion()

  if (!forceRefresh && isRegionCacheFresh(cached, ttlMs)) {
    return {
      ...cached,
      source: String(cached.source || 'cache')
    }
  }

  if (regionResolvePromise) {
    return regionResolvePromise
  }

  const task = resolveRegionWithFallback().finally(() => {
    if (regionResolvePromise === task) {
      regionResolvePromise = null
    }
  })
  regionResolvePromise = task
  return task
}

module.exports = {
  getSafeLocation,
  reverseGeocode,
  matchRegionCode,
  resolveRegionWithFallback,
  resolveRegionSmart,
  toFallbackMessage,
  classifyLocationError,
  toLocationErrorMessage
}
