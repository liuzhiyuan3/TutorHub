const { request } = require('./request')
const globalStore = require('./global-store')
const { getCurrentLocationWithAuth } = require('./location')

const REGION_CACHE_TTL = 30 * 60 * 1000
let latestTaskId = 0

async function getSafeLocation() {
  const exact = await getCurrentLocationWithAuth()
  return {
    latitude: Number(exact.latitude),
    longitude: Number(exact.longitude),
    accuracy: Number(exact.accuracy || 0),
    source: 'gps'
  }
}

function classifyLocationError(error) {
  if (error && error.reason) return String(error.reason)
  const text = String((error && (error.errMsg || error.message)) || '').toLowerCase()
  if (!text) return 'unknown'
  if (text.includes('cancel')) return 'cancel'
  if (text.includes('auth deny') || text.includes('auth denied') || text.includes('permission') || text.includes('authorize')) {
    return 'auth'
  }
  if (text.includes('timeout')) return 'timeout'
  if (text.includes('chooseLocation'.toLowerCase())) return 'choose'
  if (text.includes('geocoder') || text.includes('reverse') || text.includes('region')) return 'geocode'
  if (text.includes('network') || text.includes('fail')) return 'network'
  return 'unknown'
}

function toLocationErrorMessage(error) {
  const reason = classifyLocationError(error)
  if (reason === 'auth') return '未授权定位，请在设置中开启后重试'
  if (reason === 'timeout') return '定位超时，请稍后重试'
  if (reason === 'choose') return '地图选点失败，请稍后重试'
  if (reason === 'network') return '网络异常，请检查后重试'
  if (reason === 'geocode') return '地址解析失败，已记录坐标'
  return '定位失败，请稍后重试'
}

function reverseGeocode(location) {
  return request({
    url: '/api/location/reverse',
    method: 'POST',
    authMode: 'optional',
    data: {
      latitude: Number(location.latitude),
      longitude: Number(location.longitude)
    }
  }).then((data) => ({
    address: (data && data.address) || '',
    province: (data && data.province) || '',
    city: (data && data.city) || '',
    district: (data && data.district) || '',
    source: 'backend'
  }))
}

function toFallbackMessage(reason) {
  const text = String(reason || '')
  if (!text) return '定位失败，已使用默认地区'
  if (text.includes('auth deny') || text.includes('authorize') || text.includes('permission') || text.includes('未授权')) {
    return '未授权定位，已使用默认地区'
  }
  if (text.includes('timeout')) return '定位超时，已使用默认地区'
  if (text.includes('地理') || text.includes('逆地理')) return '地区解析失败，已使用默认地区'
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
  // 未命中时不要回退首项（常见为“全国”），改为让上层使用逆地理城市名展示
  if (bestScore <= 0) return null
  return best
}

function buildFallbackRegion(reason) {
  const cached = globalStore.getRegion()
  if (cached && cached.regionCode && Date.now() - Number(cached.syncAt || 0) < REGION_CACHE_TTL) {
    return {
      ...cached,
      source: 'cache',
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
  try {
    const location = await getSafeLocation()
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
    return resolved
  } catch (e) {
    return buildFallbackRegion(e && e.message ? e.message : 'region_resolve_failed')
  }
}

module.exports = {
  getSafeLocation,
  reverseGeocode,
  matchRegionCode,
  resolveRegionWithFallback,
  toFallbackMessage,
  classifyLocationError,
  toLocationErrorMessage
}
