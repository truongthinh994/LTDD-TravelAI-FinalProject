<#
.SYNOPSIS
    Generate 10 brand illustrations for TravelAI via Pollinations.ai (free, no API key),
    convert to WebP using cwebp (auto-downloads if missing), and drop into
    app/src/main/res/drawable-nodpi/.

.PARAMETER Force
    Re-generate even if the target .webp already exists.

.PARAMETER KeepPng
    Keep the intermediate .png files alongside the .webp output.

.PARAMETER NoConvert
    Skip WebP conversion entirely; keep .png in drawable-nodpi/.

.PARAMETER Only
    Generate only the listed IDs (comma-separated). Example: -Only hero_planner_main,empty_chat

.EXAMPLE
    .\generate-illustrations.ps1
    .\generate-illustrations.ps1 -Force -Only hero_planner_main
#>

[CmdletBinding()]
param(
    [switch]$Force,
    [switch]$KeepPng,
    [switch]$NoConvert,
    [string]$Only = ''
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Web

# -- Paths ------------------------------------------------------------------
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$OutDir      = Join-Path $ProjectRoot 'app\src\main\res\drawable-nodpi'
$LegacyDir   = Join-Path $ProjectRoot 'app\src\main\res\drawable'
$ToolsDir    = Join-Path $PSScriptRoot '.webp-tools'

if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

# -- Brand style stems (shared across all prompts for consistency) ----------
$Palette = "palette soft purple #9B4DFF, vivid purple #7B2FFF, magenta pink #E160FF, sky blue #28A8FF, warm amber #FFB547, coral #FF7B6B"
$Style   = "flat illustration vector, geometric shapes, soft Notion Airbnb editorial feel, clean composition, subtle paper-grain texture, no people faces, no text labels, generous negative space, soft drop shadows, isometric perspective hints"
$Avoid   = "photorealistic, 3D render, dark moody, busy details, watermarks, signature, text, faces"

# -- Image catalog (D.1-D.10 from plan) -------------------------------------
$Images = @(
    [pscustomobject]@{ Id='hero_planner_main'; Width=1200; Height=800; Seed=42;
        Prompt = "$Style. Idyllic Vietnamese travel scene at golden hour, silhouette of Ha Long Bay limestone karsts mid-ground, stylized geometric junk boat with red sail on calm water, curving wooden bridge connecting two karsts, soft pastel sunrise sky with three abstract clouds, foreground lotus flower floating on water, $Palette, soft drop shadows beneath karsts, generous sky negative space top-left. Avoid: $Avoid" }

    [pscustomobject]@{ Id='tile_camera_ai'; Width=1280; Height=720; Seed=84;
        Prompt = "$Style. Smartphone held in left hand scanning the entrance gate of Van Mieu Temple of Literature in Hanoi, purple AI scanning beam with concentric arcs emanating from phone, geometric stylized red-tile pagoda roof in background, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='empty_chat'; Width=800; Height=800; Seed=126;
        Prompt = "$Style. Travel journal flat illustration centered top-down view, vintage compass top-left, open notebook with curving page, folded paper map, traditional Vietnamese non la conical bamboo hat, scattered on a desk, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='empty_history'; Width=800; Height=800; Seed=168;
        Prompt = "$Style. Treasure-map folder opening to reveal a dashed travel route, three geometric location pin icons placed along the route, soft paper texture, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='empty_landmark'; Width=800; Height=800; Seed=210;
        Prompt = "$Style. Silhouette of Chua Mot Cot One Pillar Pagoda viewed through a stylized camera viewfinder rectangle frame, lotus pond around its single pillar base, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='empty_map'; Width=1024; Height=768; Seed=252;
        Prompt = "$Style. Stylized geometric outline of Vietnam S-shape country silhouette centered, three glowing aurora pin markers placed on north central and south regions, dashed route lines connecting them, abstract clouds floating around, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='scanner_placeholder'; Width=1024; Height=768; Seed=294;
        Prompt = "$Style. Phone held in left hand pointing at an ancient pagoda gate, purple AI scanning beam with floating circular icons emanating, geometric lantern hanging from the arch, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='header_itinerary'; Width=1500; Height=600; Seed=336;
        Prompt = "$Style. Panoramic horizontal banner of Vietnamese landscapes left to right, Sapa rice terraces, winding mountain road, coastal beach with palm trees, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='decorative_lotus'; Width=512; Height=512; Seed=378;
        Prompt = "$Style. Single geometric lotus flower top-down view centered, soft radial gradient pink to purple petals, $Palette. Avoid: $Avoid" }

    [pscustomobject]@{ Id='decorative_lantern'; Width=512; Height=512; Seed=420;
        Prompt = "$Style. Hoi An silk lantern centered with tassels hanging below, warm amber and coral colors, $Palette. Avoid: $Avoid" }
)

# Filter by -Only if provided
if ($Only) {
    $allowed = $Only -split '[,\s]+' | Where-Object { $_ }
    $Images = $Images | Where-Object { $allowed -contains $_.Id }
    if (-not $Images) { throw "No images matched -Only filter: $Only" }
}

# -- Auto-install cwebp.exe on first run ------------------------------------
function Get-CwebpPath {
    $inPath = Get-Command cwebp -ErrorAction SilentlyContinue
    if ($inPath) { return $inPath.Source }

    $local = Join-Path $ToolsDir 'cwebp.exe'
    if (Test-Path $local) { return $local }

    Write-Host "[setup] Downloading cwebp.exe (one-time, ~2 MB)..." -ForegroundColor Cyan
    if (-not (Test-Path $ToolsDir)) {
        New-Item -ItemType Directory -Path $ToolsDir -Force | Out-Null
    }

    $zipUrl  = 'https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-1.5.0-windows-x64.zip'
    $zipPath = Join-Path $ToolsDir 'libwebp.zip'
    $extract = Join-Path $ToolsDir 'libwebp-extract'

    Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath -UseBasicParsing
    Expand-Archive -Path $zipPath -DestinationPath $extract -Force

    $found = Get-ChildItem -Path $extract -Filter 'cwebp.exe' -Recurse -ErrorAction SilentlyContinue |
             Select-Object -First 1
    if (-not $found) {
        throw "cwebp.exe not found inside extracted libwebp archive."
    }
    Copy-Item -Path $found.FullName -Destination $local -Force
    Remove-Item -Path $zipPath -Force
    Remove-Item -Path $extract -Recurse -Force
    return $local
}

# -- Main loop --------------------------------------------------------------
Write-Host ''
Write-Host 'TravelAI illustration pipeline' -ForegroundColor Cyan
Write-Host ('  Target:   {0}' -f $OutDir)
Write-Host '  Provider: Pollinations.ai (FLUX, free)'
Write-Host ('  Images:   {0}' -f $Images.Count)
Write-Host ''

$cwebp = $null
if (-not $NoConvert) {
    try { $cwebp = Get-CwebpPath } catch {
        Write-Host "[warn] cwebp setup failed - will keep PNGs. Reason: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

$totalSize = 0L
$success   = 0
$skipped   = 0
$failed    = 0

foreach ($img in $Images) {
    $pngPath  = Join-Path $OutDir ($img.Id + '.png')
    $webpPath = Join-Path $OutDir ($img.Id + '.webp')
    $finalExt = if ($NoConvert -or -not $cwebp) { 'png' } else { 'webp' }
    $finalPath = Join-Path $OutDir ($img.Id + '.' + $finalExt)

    if ((Test-Path $finalPath) -and -not $Force) {
        Write-Host "[$($img.Id)] Skip (exists). Use -Force to regenerate." -ForegroundColor DarkGray
        $skipped++
        continue
    }

    # 1. Download from Pollinations
    $encoded = [Uri]::EscapeDataString($img.Prompt)
    $url     = 'https://image.pollinations.ai/prompt/{0}?width={1}&height={2}&model=flux&nologo=true&seed={3}&private=true' -f $encoded, $img.Width, $img.Height, $img.Seed

    $label = '[{0}] Generating {1}x{2} ...' -f $img.Id, $img.Width, $img.Height
    Write-Host $label -NoNewline
    try {
        Invoke-WebRequest -Uri $url -OutFile $pngPath -UseBasicParsing -TimeoutSec 180
    } catch {
        Write-Host (' FAILED ({0})' -f $_.Exception.Message) -ForegroundColor Red
        $failed++
        continue
    }

    $pngSize = (Get-Item $pngPath).Length
    if ($pngSize -lt 5KB) {
        Write-Host (' FAILED (response {0} bytes - likely upstream error)' -f $pngSize) -ForegroundColor Red
        Remove-Item -Path $pngPath -Force
        $failed++
        continue
    }
    Write-Host (' PNG {0}KB' -f [Math]::Round($pngSize/1KB, 0)) -NoNewline

    # 2. Convert to WebP
    if ($cwebp -and -not $NoConvert) {
        try {
            & $cwebp -q 88 -quiet $pngPath -o $webpPath
            if ($LASTEXITCODE -ne 0) { throw ('cwebp exit {0}' -f $LASTEXITCODE) }

            $webpSize = (Get-Item $webpPath).Length
            $saving   = [Math]::Round((1 - $webpSize / $pngSize) * 100, 0)
            $msg = '  -> WebP {0}KB (-{1}%)' -f [Math]::Round($webpSize/1KB, 0), $saving
            Write-Host $msg -ForegroundColor Green

            if (-not $KeepPng) { Remove-Item -Path $pngPath -Force }
            $totalSize += $webpSize
            $success++
        } catch {
            Write-Host ('  WebP failed: {0}. Keeping PNG.' -f $_.Exception.Message) -ForegroundColor Yellow
            $totalSize += $pngSize
            $success++
        }
    } else {
        Write-Host '' -ForegroundColor Green
        $totalSize += $pngSize
        $success++
    }

    # 3. Retire legacy XML placeholder if same name exists in drawable/.
    # We MOVE it OUT of res/ entirely because AAPT2 rejects unknown extensions
    # (.bak) inside the resource tree. Backup goes next to the script.
    $legacyXml = Join-Path $LegacyDir ($img.Id + '.xml')
    if (Test-Path $legacyXml) {
        $backupDir = Join-Path $PSScriptRoot '.legacy-drawable-backup'
        if (-not (Test-Path $backupDir)) { New-Item -ItemType Directory -Path $backupDir -Force | Out-Null }
        $backupPath = Join-Path $backupDir ($img.Id + '.xml')
        if (Test-Path $backupPath) { Remove-Item -Path $backupPath -Force }
        Move-Item -Path $legacyXml -Destination $backupPath
        Write-Host ('       (moved drawable/{0}.xml -> scripts/.legacy-drawable-backup/)' -f $img.Id) -ForegroundColor DarkGray
    }
}

# -- Summary ----------------------------------------------------------------
Write-Host ''
Write-Host 'Done.' -ForegroundColor Cyan
Write-Host ('  Generated:  {0}' -f $success)
Write-Host ('  Skipped:    {0}' -f $skipped)
Write-Host ('  Failed:     {0}' -f $failed)
Write-Host ('  Total size: {0} KB' -f [Math]::Round($totalSize/1KB, 0))
Write-Host ''
if ($failed -gt 0) {
    Write-Host 'Tip: re-run failed images with -Force -Only <id1>,<id2>' -ForegroundColor Yellow
}
