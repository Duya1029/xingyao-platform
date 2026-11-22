@echo off
chcp 65001 >nul
title 星曜数字化平台 - 前端 (5173)
cd /d %~dp0frontend
if not exist node_modules (
  echo 首次运行，正在安装前端依赖...
  call npm install
)
call npm run dev
pause
