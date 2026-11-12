[CmdletBinding()]
param(
    [ValidateSet('test', 'build', 'test-build')]
    [string]$Task = 'test-build'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $projectRoot 'build/validation'
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
$logPath = Join-Path $reportDir 'gradle.log'
$summaryPath = Join-Path $reportDir 'summary.txt'
$gradleTasks = switch ($Task) {
    'test' { @('test') }
    'build' { @('build') }
    'test-build' { @('test', 'build') }
}
$gradleArgs = @('--console=plain', '--warning-mode=summary') + $gradleTasks
# gradlew.bat respects DEBUG to suppress command echoing. Scope it to this process.
$previousDebug = $env:DEBUG
$env:DEBUG = ''
$exitCode = 1
Push-Location $projectRoot
try {
    # Windows PowerShell can promote native stderr to errors; preserve the native exit code.
    $ErrorActionPreference = 'Continue'
    & (Join-Path $projectRoot 'gradlew.bat') @gradleArgs > $logPath 2>&1
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    $lines = @(Get-Content -LiteralPath $logPath)
    $summary = @("Gradle $($gradleTasks -join ' '): exit $exitCode", "Log: $logPath")
    if ($exitCode -eq 0) {
        $summary += 'Test reports: build/reports/tests/test/index.html'
        $jars = @(Get-ChildItem (Join-Path $projectRoot 'build/libs') -Filter '*.jar' -ErrorAction SilentlyContinue)
        $summary += $jars | ForEach-Object { "JAR: $($_.Name) ($($_.Length) bytes)" }
    } else {
        $failure = $lines | Select-String -Pattern 'FAILURE:|What went wrong|Verification of Gradle|Expected checksum|Actual checksum|^> |^\* ' | Select-Object -First 12
        if ($failure) {
            $summary += $failure | ForEach-Object { $_.Line }
        } else {
            $summary += $lines | Select-Object -Last 15
        }
    }
    $summary | Set-Content -LiteralPath $summaryPath -Encoding UTF8
    $summary | Write-Output
} finally {
    $env:DEBUG = $previousDebug
    Pop-Location
}
exit $exitCode
