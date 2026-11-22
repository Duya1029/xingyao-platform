/* 星曜数字化管理平台 - 表结构（SQL Server） */
USE xingyao;
GO

/* ============ 系统 ============ */
IF OBJECT_ID('dbo.sys_user','U') IS NULL
CREATE TABLE dbo.sys_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(120) NOT NULL,
    real_name NVARCHAR(50) NOT NULL,
    phone NVARCHAR(30) NULL,
    dept_name NVARCHAR(60) NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    update_time DATETIME2 NULL,
    deleted INT NOT NULL DEFAULT 0
);
GO

IF OBJECT_ID('dbo.sys_role','U') IS NULL
CREATE TABLE dbo.sys_role (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_code NVARCHAR(50) NOT NULL UNIQUE,
    role_name NVARCHAR(50) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    remark NVARCHAR(200) NULL
);
GO

IF OBJECT_ID('dbo.sys_user_role','U') IS NULL
CREATE TABLE dbo.sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
GO

IF OBJECT_ID('dbo.sys_menu','U') IS NULL
CREATE TABLE dbo.sys_menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name NVARCHAR(50) NOT NULL,
    path NVARCHAR(120) NULL,
    component NVARCHAR(120) NULL,
    icon NVARCHAR(50) NULL,
    type NVARCHAR(10) NOT NULL DEFAULT 'MENU',
    perms NVARCHAR(100) NULL,
    sort INT NOT NULL DEFAULT 0,
    visible INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.sys_role_menu','U') IS NULL
CREATE TABLE dbo.sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
GO

IF OBJECT_ID('dbo.sys_login_log','U') IS NULL
CREATE TABLE dbo.sys_login_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NULL,
    ip NVARCHAR(50) NULL,
    success INT NOT NULL DEFAULT 1,
    message NVARCHAR(200) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

/* ============ MES ============ */
IF OBJECT_ID('dbo.mes_workshop','U') IS NULL
CREATE TABLE dbo.mes_workshop (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.mes_workstation','U') IS NULL
CREATE TABLE dbo.mes_workstation (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    workshop_id BIGINT NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.mes_station_member','U') IS NULL
CREATE TABLE dbo.mes_station_member (
    station_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (station_id, user_id)
);
GO

IF OBJECT_ID('dbo.mes_route_category','U') IS NULL
CREATE TABLE dbo.mes_route_category (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.mes_route','U') IS NULL
CREATE TABLE dbo.mes_route (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    category_id BIGINT NULL,
    remark NVARCHAR(300) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.mes_work_order','U') IS NULL
CREATE TABLE dbo.mes_work_order (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_no NVARCHAR(50) NOT NULL,
    product_name NVARCHAR(120) NOT NULL,
    plan_qty DECIMAL(18,3) NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 0,   /* 0草稿 1已下达 2已完工 */
    plan_start DATETIME2 NULL,
    plan_end DATETIME2 NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

/* 排产任务：assign_type = PERSON / STATION */
IF OBJECT_ID('dbo.mes_task','U') IS NULL
CREATE TABLE dbo.mes_task (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    task_no NVARCHAR(50) NOT NULL,
    work_order_id BIGINT NULL,
    route_id BIGINT NULL,
    workstation_id BIGINT NULL,
    assign_type NVARCHAR(20) NOT NULL DEFAULT 'STATION',
    assignee_id BIGINT NULL,
    plan_qty DECIMAL(18,3) NOT NULL DEFAULT 0,
    finished_qty DECIMAL(18,3) NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 0,   /* 0待开始 1进行中 2已完成 */
    plan_start DATETIME2 NULL,
    plan_end DATETIME2 NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

/* 订单关联人员（JB-5 判定依据） */
IF OBJECT_ID('dbo.mes_task_member','U') IS NULL
CREATE TABLE dbo.mes_task_member (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    source NVARCHAR(20) NOT NULL DEFAULT 'TEAM'  /* ASSIGNEE/STATION/TEAM */
);
GO

/* 报工单 */
IF OBJECT_ID('dbo.mes_feedback','U') IS NULL
CREATE TABLE dbo.mes_feedback (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    feedback_no NVARCHAR(50) NOT NULL,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    qty DECIMAL(18,3) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',  /* PENDING/APPROVED/REJECTED */
    audit_user_id BIGINT NULL,
    audit_time DATETIME2 NULL,
    audit_remark NVARCHAR(300) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

/* ============ WMS ============ */
IF OBJECT_ID('dbo.wms_warehouse','U') IS NULL
CREATE TABLE dbo.wms_warehouse (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    address NVARCHAR(200) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.wms_item_category','U') IS NULL
CREATE TABLE dbo.wms_item_category (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.wms_brand','U') IS NULL
CREATE TABLE dbo.wms_brand (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(80) NOT NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.wms_partner','U') IS NULL
CREATE TABLE dbo.wms_partner (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    type NVARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',  /* CUSTOMER/SUPPLIER */
    contact NVARCHAR(50) NULL,
    phone NVARCHAR(30) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.wms_item','U') IS NULL
CREATE TABLE dbo.wms_item (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    category_id BIGINT NULL,
    brand_id BIGINT NULL,
    spec NVARCHAR(120) NULL,
    unit NVARCHAR(20) NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.wms_stock','U') IS NULL
CREATE TABLE dbo.wms_stock (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    qty DECIMAL(18,3) NOT NULL DEFAULT 0
);
GO

IF OBJECT_ID('dbo.wms_inout','U') IS NULL
CREATE TABLE dbo.wms_inout (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_no NVARCHAR(50) NOT NULL,
    type NVARCHAR(20) NOT NULL,   /* IN/OUT/MOVE/CHECK */
    warehouse_id BIGINT NULL,
    target_warehouse_id BIGINT NULL,
    status INT NOT NULL DEFAULT 0,  /* 0草稿 1已过账 */
    remark NVARCHAR(300) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.wms_inout_line','U') IS NULL
CREATE TABLE dbo.wms_inout_line (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    qty DECIMAL(18,3) NOT NULL DEFAULT 0
);
GO

/* ============ ERP ============ */
IF OBJECT_ID('dbo.erp_customer','U') IS NULL
CREATE TABLE dbo.erp_customer (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    contact NVARCHAR(50) NULL,
    phone NVARCHAR(30) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.erp_supplier','U') IS NULL
CREATE TABLE dbo.erp_supplier (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    contact NVARCHAR(50) NULL,
    phone NVARCHAR(30) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.erp_product','U') IS NULL
CREATE TABLE dbo.erp_product (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(120) NOT NULL,
    unit NVARCHAR(20) NULL,
    category_name NVARCHAR(80) NULL,
    status INT NOT NULL DEFAULT 1
);
GO

IF OBJECT_ID('dbo.erp_order','U') IS NULL
CREATE TABLE dbo.erp_order (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_no NVARCHAR(50) NOT NULL,
    type NVARCHAR(20) NOT NULL,   /* PURCHASE/SALES */
    partner_name NVARCHAR(120) NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 0,
    order_date DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

/* ============ 导入中心 ============ */
IF OBJECT_ID('dbo.sys_import_batch','U') IS NULL
CREATE TABLE dbo.sys_import_batch (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    batch_no NVARCHAR(50) NOT NULL,
    biz_type NVARCHAR(40) NOT NULL,
    file_name NVARCHAR(200) NULL,
    total_rows INT NOT NULL DEFAULT 0,
    success_rows INT NOT NULL DEFAULT 0,
    fail_rows INT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'DONE',
    create_user NVARCHAR(50) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.sys_import_error','U') IS NULL
CREATE TABLE dbo.sys_import_error (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    row_no INT NOT NULL,
    column_name NVARCHAR(60) NULL,
    message NVARCHAR(300) NOT NULL,
    raw_data NVARCHAR(500) NULL
);
GO

/* ============ 图纸批量识别 ============ */
IF OBJECT_ID('dbo.drw_task','U') IS NULL
CREATE TABLE dbo.drw_task (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    task_no NVARCHAR(50) NOT NULL,
    file_count INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    fail_count INT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_user NVARCHAR(50) NULL,
    create_time DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

IF OBJECT_ID('dbo.drw_file','U') IS NULL
CREATE TABLE dbo.drw_file (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    task_id BIGINT NOT NULL,
    file_name NVARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    recognize_result NVARCHAR(MAX) NULL,
    fail_reason NVARCHAR(300) NULL,
    retry_count INT NOT NULL DEFAULT 0
);
GO
