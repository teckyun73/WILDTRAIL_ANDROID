param(
    [Parameter(Mandatory = $true)]
    [ValidateRange(1, 2100000000)]
    [int] $VersionCode,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z]+)*$')]
    [string] $VersionName,

    [string] $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'

function Resolve-FromRoot([string] $Path) {
    Join-Path $ProjectRoot $Path
}

function Fail([string] $Message) {
    throw "Release version update failed: $Message"
}

$workflowPath = Resolve-FromRoot '.github/workflows/android-ci.yml'
if (-not (Test-Path $workflowPath)) {
    Fail "Workflow file was not found at $workflowPath"
}

$workflow = [System.IO.File]::ReadAllText($workflowPath)
$codePattern = "(?m)^(\s*VERSION_CODE:\s*)'[^']+'\s*$"
$namePattern = "(?m)^(\s*VERSION_NAME:\s*)'[^']+'\s*$"

if ($workflow -notmatch $codePattern) {
    Fail "VERSION_CODE was not found in $workflowPath"
}
if ($workflow -notmatch $namePattern) {
    Fail "VERSION_NAME was not found in $workflowPath"
}

$currentCode = [regex]::Match($workflow, $codePattern).Groups[0].Value.Trim()
$currentName = [regex]::Match($workflow, $namePattern).Groups[0].Value.Trim()

$updated = [regex]::Replace($workflow, $codePattern, "`${1}'$VersionCode'")
$updated = [regex]::Replace($updated, $namePattern, "`${1}'$VersionName'")

[System.IO.File]::WriteAllText($workflowPath, $updated.TrimEnd() + "`n", [System.Text.UTF8Encoding]::new($false))

Write-Host "Release version inputs updated."
Write-Host "Workflow: $workflowPath"
Write-Host "Previous: $currentCode, $currentName"
Write-Host "Current: VERSION_CODE: '$VersionCode', VERSION_NAME: '$VersionName'"
Write-Host "Next verification command:"
Write-Host ".\gradlew.bat ktlintCheck testDebugUnitTest assembleRelease bundleRelease '-PVERSION_CODE=$VersionCode' '-PVERSION_NAME=$VersionName' --stacktrace"
Write-Host ".\scripts\verify-release-candidate.ps1 -VersionCode $VersionCode -VersionName $VersionName"
