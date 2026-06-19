param(
    [Parameter(Mandatory = $true)]
    [int] $VersionCode,

    [Parameter(Mandatory = $true)]
    [string] $VersionName,

    [string] $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,

    [switch] $RequireSigned
)

$ErrorActionPreference = 'Stop'

function Resolve-FromRoot([string] $Path) {
    Join-Path $ProjectRoot $Path
}

function Fail([string] $Message) {
    throw "Release candidate verification failed: $Message"
}

function Find-CommandPath([string[]] $Names) {
    foreach ($name in $Names) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            return $command.Source
        }
    }
    return $null
}

$apkMetadataPath = Resolve-FromRoot 'app/build/outputs/apk/release/output-metadata.json'
$aabPath = Resolve-FromRoot 'app/build/outputs/bundle/release/app-release.aab'

if (-not (Test-Path $apkMetadataPath)) {
    Fail "APK metadata was not found at $apkMetadataPath. Run assembleRelease first."
}

if (-not (Test-Path $aabPath)) {
    Fail "AAB was not found at $aabPath. Run bundleRelease first."
}

$metadata = Get-Content -Path $apkMetadataPath -Raw | ConvertFrom-Json
$apkElement = @($metadata.elements)[0]
if (-not $apkElement) {
    Fail "APK metadata does not contain an output element."
}

if ([int] $apkElement.versionCode -ne $VersionCode) {
    Fail "Expected versionCode $VersionCode but APK metadata has $($apkElement.versionCode)."
}

if ([string] $apkElement.versionName -ne $VersionName) {
    Fail "Expected versionName $VersionName but APK metadata has $($apkElement.versionName)."
}

$apkPath = Resolve-FromRoot (Join-Path 'app/build/outputs/apk/release' $apkElement.outputFile)
if (-not (Test-Path $apkPath)) {
    Fail "APK listed in metadata was not found at $apkPath."
}

$apkFile = Get-Item $apkPath
$aabFile = Get-Item $aabPath
if ($apkFile.Length -le 0) {
    Fail "APK is empty: $apkPath"
}
if ($aabFile.Length -le 0) {
    Fail "AAB is empty: $aabPath"
}

$apkSigned = $apkFile.Name -notmatch 'unsigned'
$signatureNotes = New-Object System.Collections.Generic.List[string]

$apksigner = Find-CommandPath @('apksigner.bat', 'apksigner')
if ($apksigner) {
    & $apksigner verify $apkPath *> $null
    if ($LASTEXITCODE -eq 0) {
        $apkSigned = $true
        $signatureNotes.Add('APK signature verified with apksigner.')
    } else {
        $signatureNotes.Add('APK signature verification failed with apksigner.')
    }
} else {
    $signatureNotes.Add('apksigner was not found on PATH; APK signature check used filename heuristic only.')
}

if ($RequireSigned -and -not $apkSigned) {
    Fail "RequireSigned was set, but APK appears unsigned: $($apkFile.Name)"
}

$jarsigner = Find-CommandPath @('jarsigner.exe', 'jarsigner')
$aabSignatureState = 'not checked'
if ($jarsigner) {
    & $jarsigner -verify $aabPath *> $null
    if ($LASTEXITCODE -eq 0) {
        $aabSignatureState = 'verified with jarsigner'
    } else {
        $aabSignatureState = 'not verified by jarsigner'
        if ($RequireSigned) {
            Fail "RequireSigned was set, but AAB signature verification failed."
        }
    }
} elseif ($RequireSigned) {
    Fail "RequireSigned was set, but jarsigner was not found on PATH."
}

$artifacts = @($apkFile, $aabFile)
$artifactRows = foreach ($artifact in $artifacts) {
    $hash = Get-FileHash -Path $artifact.FullName -Algorithm SHA256
    [PSCustomObject]@{
        File = $artifact.FullName
        SizeBytes = $artifact.Length
        Sha256 = $hash.Hash
    }
}

$report = [PSCustomObject]@{
    ApplicationId = $metadata.applicationId
    Variant = $metadata.variantName
    VersionCode = [int] $apkElement.versionCode
    VersionName = [string] $apkElement.versionName
    Apk = $apkFile.FullName
    Aab = $aabFile.FullName
    ApkSigned = $apkSigned
    AabSignature = $aabSignatureState
    RequireSigned = [bool] $RequireSigned
    GeneratedAt = (Get-Date).ToString('s')
    Artifacts = $artifactRows
    SignatureNotes = @($signatureNotes)
}

$reportDir = Resolve-FromRoot 'build/reports/release-candidate'
New-Item -ItemType Directory -Force $reportDir | Out-Null
$reportPath = Join-Path $reportDir "release-candidate-$VersionName-$VersionCode.json"
$report | ConvertTo-Json -Depth 5 | Set-Content -Path $reportPath -Encoding UTF8

Write-Host "Release candidate verification passed."
Write-Host "Application: $($report.ApplicationId)"
Write-Host "Version: $($report.VersionName) ($($report.VersionCode))"
Write-Host "APK: $($report.Apk)"
Write-Host "AAB: $($report.Aab)"
Write-Host "APK signed: $($report.ApkSigned)"
Write-Host "AAB signature: $($report.AabSignature)"
Write-Host "Report: $reportPath"
Write-Host "SHA-256:"
foreach ($row in $artifactRows) {
    Write-Host "- $($row.Sha256)  $($row.File)"
}
