# 星曜数字化管理平台

面向中小制造企业的一体化数字化平台，覆盖 **WMS 仓储 / MES 制造 / ERP 经营**，并提供 Excel 导入中心与图纸批量识别。

## 在线演示

🔗 https://32d429a3.r49.cpolar.top

> 免费隧道地址每次重启会变化，以启动窗口打印的地址为准；演示期间电脑需保持开机。

| 角色 | 账号 | 密码 | 可见范围 |
| --- | --- | --- | --- |
| 管理员 | admin | 123456 | 全部模块 |
| MES 操作员 | zhangwei | 123456 | 工作台、MES、WMS、ERP |
| 只读用户 | lina / wangqiang / liuyang | 123456 | 工作台、WMS、ERP |

## 技术栈

- 后端：Java 21 + Spring Boot 3.5 + MyBatis-Plus + JWT
- 前端：Vue 3 + Vite + Element Plus + Pinia
- 数据库：SQL Server

## 功能模块

- 系统管理：登录、JWT 鉴权、动态菜单、用户/角色/登录日志
- MES：排产任务（个人/工作站两种指派）、工艺路线及分类、工作站成员、报工与审核
- WMS：物料、仓库、出入库单及过账
- ERP：产品、客户/供应商、订单
- 导入中心：Excel 模板下载、逐行校验、失败明细
- 图纸识别：批量上传（PDF/PNG/JPG/DWG/DXF）、异步识别、失败重试

## 本地启动

1. 执行 `db` 目录下 `01_create_database.sql`、`02_schema.sql`、`03_seed.sql`
2. 复制 `backend/src/main/resources/application.example.yml` 为 `application.yml`，填写数据库账号密码与 JWT 密钥
3. 后端：`cd backend && mvn package && java -jar target/xingyao-platform-1.0.0.jar`（默认 8091）
4. 前端开发：`cd frontend && npm install && npm run dev`
5. 生产模式：`cd frontend && npm run build`，把 `dist` 内容复制到 `backend/src/main/resources/static` 后重新打包，访问后端端口即可

默认管理员：`admin / 123456`（首次部署后请立即修改密码）。

## 业务规则

- 任务指派方式二选一：指派个人 PERSON / 指派工作站 STATION
- 报工累计（待审 + 已通过）不得超过排产数量
- 非本订单关联人员禁止报工
- 报工审核通过后回写排产任务已生产数量

## 说明

图纸识别当前为占位实现，接入真实 OCR/CAD 服务时替换 `DrawingService.recognize()` 即可。
