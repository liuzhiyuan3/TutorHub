function getFileSize(filePath) {
  return new Promise((resolve, reject) => {
    wx.getFileInfo({
      filePath,
      success: (res) => resolve(Number((res && res.size) || 0)),
      fail: (e) => reject(e)
    })
  })
}

function compressImage(filePath, quality) {
  return new Promise((resolve, reject) => {
    wx.compressImage({
      src: filePath,
      quality,
      success: (res) => resolve((res && res.tempFilePath) || filePath),
      fail: (e) => reject(e)
    })
  })
}

async function prepareImageForUpload(filePath, options) {
  const opts = options || {}
  const maxBytes = Number(opts.maxBytes || 700 * 1024)
  const qualitySteps = Array.isArray(opts.qualitySteps) && opts.qualitySteps.length
    ? opts.qualitySteps
    : [80, 70, 60, 50, 40, 30]

  let currentPath = filePath
  let currentSize = await getFileSize(currentPath)
  if (currentSize <= maxBytes) {
    return { filePath: currentPath, size: currentSize }
  }

  for (let i = 0; i < qualitySteps.length; i += 1) {
    const quality = Number(qualitySteps[i])
    try {
      currentPath = await compressImage(currentPath, quality)
      currentSize = await getFileSize(currentPath)
      if (currentSize <= maxBytes) {
        return { filePath: currentPath, size: currentSize }
      }
    } catch (e) {
      // continue trying next quality, or fail at end
    }
  }

  throw new Error('图片过大，请换一张更小的图片后重试')
}

module.exports = {
  prepareImageForUpload,
  getFileSize
}
