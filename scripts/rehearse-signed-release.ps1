param(
    [Parameter(Mandatory = $true)]
    [ValidateRange(1, 2100000000)]
    [int] $VersionCode,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z]+)*$')]
    [string] $VersionName,

    [string] $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,

    [string] $TempDirectory = $env:TEMP
)

$ErrorActionPreference = 'Stop'

function Fail([string] $Message) {
    throw "Signed release rehearsal failed: $Message"
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

if (-not $TempDirectory) {
    $TempDirectory = Join-Path $ProjectRoot 'build/tmp'
}
New-Item -ItemType Directory -Force $TempDirectory | Out-Null

$keytool = Find-CommandPath @('keytool.exe', 'keytool')
if (-not $keytool) {
    $androidStudioKeytool = 'C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe'
    if (Test-Path $androidStudioKeytool) {
        $keytool = $androidStudioKeytool
    }
}
if (-not $keytool) {
    Fail 'keytool was not found. Install JDK 17+ or use Android Studio bundled JBR.'
}

$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$keystorePath = Join-Path $TempDirectory "wildtrail-upload-rehearsal-$stamp.p12"
$alias = 'wildtrail-upload-rehearsal'
$password = "WildTrailRehearsal-$stamp!"

try {
    Write-Host "Creating temporary PKCS12 upload key: $keystorePath"
    & $keytool -genkeypair `
        -v `
        -keystore $keystorePath `
        -storetype PKCS12 `
        -storepass $password `
        -keypass $password `
        -keyalg RSA `
        -keysize 2048 `
        -validity 30 `
        -alias $alias `
        -dname 'CN=WildTrail Rehearsal, OU=Release, O=WildTrail, L=Seoul, S=Seoul, C=KR'

    if ($LASTEXITCODE -ne 0) {
        Fail 'keytool failed to create the rehearsal keystore.'
    }

    Push-Location $ProjectRoot
    try {
        Write-Host 'Building signed release rehearsal artifacts.'
        & .\gradlew.bat `
            ktlintCheck `
            testDebugUnitTest `
            assembleRelease `
            bundleRelease `
            "-PVERSION_CODE=$VersionCode" `
            "-PVERSION_NAME=$VersionName" `
            "-PRELEASE_STORE_FILE=$keystorePath" `
            "-PRELEASE_STORE_PASSWORD=$password" `
            "-PRELEASE_KEY_ALIAS=$alias" `
            "-PRELEASE_KEY_PASSWORD=$password" `
            --stacktrace

        if ($LASTEXITCODE -ne 0) {
            Fail 'Gradle signed release rehearsal build failed.'
        }

        & .\scripts\verify-release-candidate.ps1 -VersionCode $VersionCode -VersionName $VersionName -RequireSigned
        if ($LASTEXITCODE -ne 0) {
            Fail 'Signed release candidate verification failed.'
        }
    } finally {
        Pop-Location
    }

    Write-Host 'Signed release rehearsal passed.'
} finally {
    if (Test-Path $keystorePath) {
        Remove-Item -LiteralPath $keystorePath -Force
        Write-Host "Deleted temporary rehearsal key: $keystorePath"
    }
}
