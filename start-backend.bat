@echo off
chcp 65001 >nul
title 星曜数字化平台 - 后端 (8090)
cd /d %~dp0backend
java -jar target\xingyao-platform-1.0.0.jar
pause
