# Run PageChange application
param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$Arguments
)

$ErrorActionPreference = "Stop"

# Check if compiled
if (-not (Test-Path "out\com\pagechange\Main.class")) {
    Write-Host "Project not compiled. Running build first..." -ForegroundColor Yellow
    .\build.ps1
    if ($LASTEXITCODE -ne 0) {
        exit 1
    }
}

# Run
Write-Host "Running PageChange..." -ForegroundColor Cyan
Write-Host ""

if ($Arguments) {
    java -cp "out;lib\javax.mail-1.6.2.jar" com.pagechange.Main $Arguments
} else {
    java -cp "out;lib\javax.mail-1.6.2.jar" com.pagechange.Main --help
}

