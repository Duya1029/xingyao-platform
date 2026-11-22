@echo off
chcp 65001 >nul
title 星曜数字化平台 - 一键启动
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-xingyao-public.ps1"
pause
