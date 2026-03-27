param(
  [string]$Root = ".",
  [switch]$Fix
)

$ErrorActionPreference = "Stop"

$textExt = @(
  ".md", ".txt", ".json", ".yaml", ".yml",
  ".js", ".ts", ".tsx", ".vue", ".css", ".scss",
  ".html", ".xml", ".java", ".properties", ".sh", ".ps1"
)

$utf8Strict = [System.Text.UTF8Encoding]::new($false, $true)
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$gb = [System.Text.Encoding]::GetEncoding(936)

$files = Get-ChildItem -LiteralPath $Root -Recurse -File |
  Where-Object { $textExt -contains $_.Extension.ToLowerInvariant() }

$bad = @()

foreach ($f in $files) {
  $bytes = [System.IO.File]::ReadAllBytes($f.FullName)
  try {
    [void]$utf8Strict.GetString($bytes)
  } catch {
    $bad += $f.FullName
    if ($Fix) {
      $text = $gb.GetString($bytes)
      [System.IO.File]::WriteAllText($f.FullName, $text, $utf8NoBom)
    }
  }
}

if ($bad.Count -eq 0) {
  Write-Output "OK: all checked text files are UTF-8."
  exit 0
}

if ($Fix) {
  Write-Output "FIXED: converted non-UTF-8 files to UTF-8:"
  $bad | ForEach-Object { Write-Output " - $_" }
  exit 0
}

Write-Output "FOUND: non-UTF-8 files:"
$bad | ForEach-Object { Write-Output " - $_" }
exit 1
