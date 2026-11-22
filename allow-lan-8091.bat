@echo off
net session >nul 2>&1
if errorlevel 1 (
    powershell -NoProfile -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)
netsh advfirewall firewall delete rule name="Xingyao 8091" >nul 2>&1
netsh advfirewall firewall add rule name="Xingyao 8091" dir=in action=allow protocol=TCP localport=8091
echo.
echo Firewall rule for port 8091 has been added.
pause
