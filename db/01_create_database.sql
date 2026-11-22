/* 星曜数字化管理平台 - 数据库初始化脚本（SQL Server）*/
USE master;
GO
IF DB_ID('xingyao') IS NULL
BEGIN
    CREATE DATABASE xingyao;
END
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'xingyao_app')
BEGIN
    CREATE LOGIN xingyao_app WITH PASSWORD = 'ChangeMe@2026!', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;
END
GO
USE xingyao;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'xingyao_app')
BEGIN
    CREATE USER xingyao_app FOR LOGIN xingyao_app;
    ALTER ROLE db_owner ADD MEMBER xingyao_app;
END
GO
