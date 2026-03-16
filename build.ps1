# Build script for PageChange
$ErrorActionPreference = "Stop"

Write-Host "PageChange - Build Script" -ForegroundColor Cyan
Write-Host "=========================" -ForegroundColor Cyan

# Create directories
Write-Host "`nCreating directories..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "lib" | Out-Null
New-Item -ItemType Directory -Force -Path "out" | Out-Null

# Download javax.mail if not exists
$javaxMailPath = "lib\javax.mail-1.6.2.jar"
if (-not (Test-Path $javaxMailPath)) {
    Write-Host "Downloading javax.mail..." -ForegroundColor Yellow
    $url = "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar"
    try {
        Invoke-WebRequest -Uri $url -OutFile $javaxMailPath
        Write-Host "Downloaded javax.mail successfully" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download javax.mail. Please download manually from:" -ForegroundColor Red
        Write-Host $url -ForegroundColor Red
        Write-Host "And place it in: $javaxMailPath" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "javax.mail already exists" -ForegroundColor Green
}

# Compile
Write-Host "`nCompiling..." -ForegroundColor Yellow
$sourceFiles = Get-ChildItem -Path "src\com\pagechange" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

javac -d out -sourcepath src -cp "lib\*" $sourceFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nCompilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green

# Copy resources
Write-Host "`nCopying resources..." -ForegroundColor Yellow
if (Test-Path "src\resources") {
    Copy-Item -Path "src\resources" -Destination "out\" -Recurse -Force
    Write-Host "Resources copied" -ForegroundColor Green
}

# Copy META-INF
if (Test-Path "src\META-INF") {
    Copy-Item -Path "src\META-INF" -Destination "out\" -Recurse -Force
    Write-Host "META-INF copied" -ForegroundColor Green
}

Write-Host "`n=========================" -ForegroundColor Cyan
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "`nTo run the application:" -ForegroundColor Cyan
Write-Host "  java -cp `"out;lib\javax.mail-1.6.2.jar`" com.pagechange.Main --help" -ForegroundColor White
Write-Host "`nTo create JAR:" -ForegroundColor Cyan
Write-Host "  .\create-jar.ps1" -ForegroundColor White

