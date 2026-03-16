# Create JAR file for PageChange

}
    exit 1
    Write-Host "Failed to create JAR" -ForegroundColor Red
} else {
    Write-Host "  java -jar pageChange.jar --help" -ForegroundColor White
    Write-Host "`nTo run:" -ForegroundColor Cyan
    Write-Host "`nJAR created successfully: pageChange.jar" -ForegroundColor Green
if ($LASTEXITCODE -eq 0) {

jar cfm pageChange.jar out\META-INF\MANIFEST.MF -C out .
Write-Host "Creating JAR..." -ForegroundColor Yellow
# Create JAR

}
    Remove-Item "out\temp" -Recurse -Force
    }
        Copy-Item $_.FullName "out\" -Recurse -Force
    Get-ChildItem "out\temp" | ForEach-Object {
    # Move extracted files to out

    Set-Location "..\..\"
    }
        Remove-Item "META-INF" -Recurse -Force
    if (Test-Path "META-INF") {
    # Remove META-INF from extracted libs to avoid conflicts
    jar xf "..\..\lib\javax.mail-1.6.2.jar"
    Set-Location "out\temp"
if (Test-Path "lib\javax.mail-1.6.2.jar") {
# Extract javax.mail JAR

New-Item -ItemType Directory -Force -Path "out\temp" | Out-Null
Write-Host "`nExtracting dependencies..." -ForegroundColor Yellow
# Create JAR with dependencies

}
    .\build.ps1
    Write-Host "Project not compiled. Running build first..." -ForegroundColor Yellow
if (-not (Test-Path "out\com\pagechange\Main.class")) {
# Check if compiled

Write-Host "Creating JAR file..." -ForegroundColor Cyan

$ErrorActionPreference = "Stop"
