@echo off
chcp 65001 >nul
echo 正在为 8090 端口添加防火墙入站规则（需管理员权限）...
netsh advfirewall firewall delete rule name="Xingyao Platform 8090" >nul 2>&1
netsh advfirewall firewall add rule name="Xingyao Platform 8090" dir=in action=allow protocol=TCP localport=8090
echo.
echo 完成。局域网内其他设备可用 http://本机IP:8090 访问。
pause
