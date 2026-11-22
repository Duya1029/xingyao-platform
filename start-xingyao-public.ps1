$ErrorActionPreference = 'Stop'
$root = 'D:\xingyao-platform'
$jar = Join-Path $root 'backend\target\xingyao-platform-1.0.0.jar'
$java = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java.exe'
$cpolar = 'C:\Users\28467\cpolar\cpolar.exe'
$log = Join-Path $env:TEMP 'xingyao-tunnel.log'

Write-Host '=== 星曜数字化平台 ===' -ForegroundColor Cyan

# 1. 启动后端
$listening = netstat -ano | Select-String ':8091\s+.*LISTENING'
if (-not $listening) {
    Write-Host '正在启动后端（8091）...'
    Start-Process -FilePath $java -ArgumentList '-jar', "`"$jar`"" -WindowStyle Minimized
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        if (netstat -ano | Select-String ':8091\s+.*LISTENING') { break }
    }
}
if (-not (netstat -ano | Select-String ':8091\s+.*LISTENING')) {
    Write-Host '后端启动失败，请检查 Java 和 jar 包。' -ForegroundColor Red
    exit 1
}
Write-Host '后端已就绪：http://localhost:8091' -ForegroundColor Green

# 2. 启动公网隧道
if (Test-Path $log) { Remove-Item $log -Force }
Write-Host '正在创建公网链接...'
Start-Process -FilePath $cpolar -ArgumentList 'http', '8091', '-log', 'stdout' -RedirectStandardOutput $log -WindowStyle Hidden

$url = $null
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Milliseconds 800
    if (Test-Path $log) {
        $m = Select-String -Path $log -Pattern '"PublicUrl":"(https://[^"]+)"' -AllMatches | Select-Object -First 1
        if ($m) { $url = $m.Matches[0].Groups[1].Value; break }
    }
}

Write-Host ''
if ($url) {
    Write-Host '公网访问地址：' -NoNewline
    Write-Host $url -ForegroundColor Yellow
    Write-Host '把上面的链接发给别人，任何网络都能打开。' -ForegroundColor Gray
    Set-Content -Path (Join-Path $root '当前公网地址.txt') -Value $url -Encoding UTF8
} else {
    Write-Host '未能自动获取链接，请查看 cpolar 窗口中的 https://xxxx.r49.cpolar.top 地址。' -ForegroundColor Red
}
Write-Host ''
Write-Host '提示：关闭本窗口或电脑关机后链接失效；隧道重启后地址会变化。' -ForegroundColor DarkGray
