$ErrorActionPreference = 'Stop'
$root = 'D:\xingyao-platform'
$jar = Join-Path $root 'backend\target\xingyao-platform-1.0.0.jar'
$java = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java.exe'
$cpolar = 'C:\Users\28467\cpolar\cpolar.exe'
$log = Join-Path $env:TEMP 'xingyao-tunnel.log'

Write-Host '=== Xingyao Platform ===' -ForegroundColor Cyan

$listening = netstat -ano | Select-String ':8091\s+.*LISTENING'
if (-not $listening) {
    Write-Host 'Starting backend on port 8091 ...'
    Start-Process -FilePath $java -ArgumentList '-jar', "`"$jar`"" -WindowStyle Minimized
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        if (netstat -ano | Select-String ':8091\s+.*LISTENING') { break }
    }
}
if (-not (netstat -ano | Select-String ':8091\s+.*LISTENING')) {
    Write-Host 'Backend failed to start. Check Java and the jar file.' -ForegroundColor Red
    exit 1
}
Write-Host 'Backend ready: http://localhost:8091' -ForegroundColor Green

if (Test-Path $log) { Remove-Item $log -Force }
Write-Host 'Creating public tunnel ...'
Start-Process -FilePath $cpolar -ArgumentList 'http', '8091', '-log', 'stdout' -RedirectStandardOutput $log -WindowStyle Hidden

$url = $null
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Milliseconds 800
    if (Test-Path $log) {
        $m = Select-String -Path $log -Pattern 'Tunnel established at (https://[^\s"\\]+)' -AllMatches | Select-Object -First 1
        if ($m) { $url = $m.Matches[0].Groups[1].Value; break }
    }
}

Write-Host ''
if ($url) {
    Write-Host 'Public URL: ' -NoNewline
    Write-Host $url -ForegroundColor Yellow
    Set-Content -Path (Join-Path $root 'current-public-url.txt') -Value $url -Encoding ASCII
    Write-Host 'Share this link. It works on any network.' -ForegroundColor Gray
} else {
    Write-Host 'Could not read the public URL. Check the cpolar window for https://xxxx.r49.cpolar.top' -ForegroundColor Red
}
Write-Host ''
Write-Host 'Keep this window open. Closing it stops the public link.' -ForegroundColor DarkGray
