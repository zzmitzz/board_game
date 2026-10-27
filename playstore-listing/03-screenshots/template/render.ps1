# Renders framed Play Store screenshots (1080x1920) from captions.json with headless Chrome.
#
#   .\render.ps1                         # uses captions.json, writes ..\output-phone\*.png
#   .\render.ps1 -Captions samples.json  # any other caption list
#   .\render.ps1 -Crop 120               # trim the status bar off every raw screenshot
#
# Entries whose raw screenshot is missing are skipped with a warning.
param(
    [string]$Captions = "captions.json",
    [string]$OutDir = "..\output-phone",
    [int]$Crop = 0
)

$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$chrome = @(
    "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
    "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $chrome) { throw "Chrome or Edge not found." }

$out = Join-Path $here $OutDir
New-Item -ItemType Directory -Force $out | Out-Null
$frameUrl = "file:///" + (Join-Path $here "frame.html").Replace([char]92, [char]47)
$shotJs = Join-Path $here "_shot.js"
$items = Get-Content (Join-Path $here $Captions) -Raw -Encoding UTF8 | ConvertFrom-Json

foreach ($it in $items) {
    if (-not (Test-Path (Join-Path $here $it.shot))) {
        Write-Warning "skip $($it.out): missing $($it.shot)"
        continue
    }
    if ($null -eq $it.crop) { $it | Add-Member crop $Crop }
    "window.SHOT = " + ($it | ConvertTo-Json -Compress) + ";" |
        Out-File -FilePath $shotJs -Encoding utf8
    $png = Join-Path $out "$($it.out).png"
    & $chrome --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 `
        --allow-file-access-from-files --window-size=1080,1920 --virtual-time-budget=4000 `
        "--screenshot=$png" $frameUrl 2>$null | Out-Null
    Write-Host "rendered $png"
}
Remove-Item $shotJs -ErrorAction SilentlyContinue
