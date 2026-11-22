/* 星曜数字化管理平台 - 初始化数据（默认密码均为 123456） */
USE xingyao;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.sys_user)
BEGIN
    INSERT INTO dbo.sys_menu (id, parent_id, name, path, component, icon, type, perms, sort, visible) VALUES
    (1, 0, N'系统管理', '/system', NULL, 'Setting', 'DIR', NULL, 90, 1),
    (11, 1, N'用户管理', '/system/user', 'system/user/index', 'User', 'MENU', 'system:user:list', 1, 1),
    (12, 1, N'角色管理', '/system/role', 'system/role/index', 'Avatar', 'MENU', 'system:role:list', 2, 1),
    (13, 1, N'登录日志', '/system/login-log', 'system/loginlog/index', 'Document', 'MENU', 'system:log:list', 3, 1),
    (2, 0, N'MES 制造', '/mes', NULL, 'Cpu', 'DIR', NULL, 10, 1),
    (21, 2, N'排产任务', '/mes/task', 'mes/task/index', 'Calendar', 'MENU', 'mes:task:list', 1, 1),
    (22, 2, N'报工审核', '/mes/feedback', 'mes/feedback/index', 'Finished', 'MENU', 'mes:feedback:list', 2, 1),
    (23, 2, N'工艺路线', '/mes/route', 'mes/route/index', 'Guide', 'MENU', 'mes:route:list', 3, 1),
    (24, 2, N'工艺分类', '/mes/route-category', 'mes/routeCategory/index', 'Files', 'MENU', 'mes:routeCategory:list', 4, 1),
    (25, 2, N'工作站', '/mes/workstation', 'mes/workstation/index', 'OfficeBuilding', 'MENU', 'mes:workstation:list', 5, 1),
    (3, 0, N'WMS 仓储', '/wms', NULL, 'Box', 'DIR', NULL, 20, 1),
    (31, 3, N'物料管理', '/wms/item', 'wms/item/index', 'Goods', 'MENU', 'wms:item:list', 1, 1),
    (32, 3, N'仓库管理', '/wms/warehouse', 'wms/warehouse/index', 'House', 'MENU', 'wms:warehouse:list', 2, 1),
    (33, 3, N'出入库单', '/wms/inout', 'wms/inout/index', 'Sort', 'MENU', 'wms:inout:list', 3, 1),
    (4, 0, N'ERP 经营', '/erp', NULL, 'DataAnalysis', 'DIR', NULL, 30, 1),
    (41, 4, N'产品管理', '/erp/product', 'erp/product/index', 'ShoppingBag', 'MENU', 'erp:product:list', 1, 1),
    (42, 4, N'订单管理', '/erp/order', 'erp/order/index', 'Tickets', 'MENU', 'erp:order:list', 2, 1),
    (5, 0, N'导入中心', '/import/center', 'import/center/index', 'Upload', 'MENU', 'import:center:list', 40, 1),
    (6, 0, N'图纸识别', '/drawing/batch', 'drawing/batch/index', 'Picture', 'MENU', 'drawing:batch:list', 50, 1);

    INSERT INTO dbo.sys_role (role_code, role_name, status, remark) VALUES
    ('ADMIN', N'超级管理员', 1, N'全部权限'),
    ('MES_OPERATOR', N'车间报工员', 1, N'MES 与图纸识别'),
    ('VIEWER', N'只读用户', 1, N'仅查看');

    /* 密码统一为 123456 */
    INSERT INTO dbo.sys_user (username, password, real_name, phone, dept_name, status) VALUES
    ('admin', '$2b$10$BB1ZEWm4QDvWdJ3gVSjwGu8xEzXQNxi2JIByXKKDDOTCqduPnkQlO', N'系统管理员', '13800000000', N'信息部', 1),
    ('zhangwei', '$2b$10$BB1ZEWm4QDvWdJ3gVSjwGu8xEzXQNxi2JIByXKKDDOTCqduPnkQlO', N'张伟', '13800000001', N'机加车间', 1),
    ('lina', '$2b$10$BB1ZEWm4QDvWdJ3gVSjwGu8xEzXQNxi2JIByXKKDDOTCqduPnkQlO', N'李娜', '13800000002', N'装配车间', 1),
    ('wangqiang', '$2b$10$BB1ZEWm4QDvWdJ3gVSjwGu8xEzXQNxi2JIByXKKDDOTCqduPnkQlO', N'王强', '13800000003', N'机加车间', 1),
    ('liuyang', '$2b$10$BB1ZEWm4QDvWdJ3gVSjwGu8xEzXQNxi2JIByXKKDDOTCqduPnkQlO', N'刘洋', '13800000004', N'装配车间', 1);

    INSERT INTO dbo.sys_user_role (user_id, role_id)
    SELECT u.id, r.id FROM dbo.sys_user u CROSS JOIN dbo.sys_role r
    WHERE (u.username = 'admin' AND r.role_code = 'ADMIN')
       OR (u.username IN ('zhangwei','lina','wangqiang','liuyang') AND r.role_code = 'MES_OPERATOR');

    INSERT INTO dbo.sys_role_menu (role_id, menu_id)
    SELECT r.id, m.id FROM dbo.sys_role r CROSS JOIN dbo.sys_menu m WHERE r.role_code = 'ADMIN';

    INSERT INTO dbo.sys_role_menu (role_id, menu_id)
    SELECT r.id, m.id FROM dbo.sys_role r JOIN dbo.sys_menu m ON m.id IN (2,21,22,23,24,25,6)
    WHERE r.role_code = 'MES_OPERATOR';

    INSERT INTO dbo.sys_role_menu (role_id, menu_id)
    SELECT r.id, m.id FROM dbo.sys_role r JOIN dbo.sys_menu m ON m.id IN (1,3,31,32,33,4,41,42)
    WHERE r.role_code = 'VIEWER';

    /* ---- 车间 / 工作站 ---- */
    INSERT INTO dbo.mes_workshop (code, name) VALUES ('WS01', N'机加车间'), ('WS02', N'装配车间');
    INSERT INTO dbo.mes_workstation (code, name, workshop_id) VALUES
    ('ST01', N'CNC-01 加工中心', 1), ('ST02', N'CNC-02 加工中心', 1), ('ST03', N'装配线 A', 2), ('ST04', N'装配线 B', 2);

    INSERT INTO dbo.mes_station_member (station_id, user_id) VALUES (1, 2), (1, 4), (2, 4), (3, 3), (3, 5), (4, 5);

    /* ---- 工艺分类 / 工艺路线 ---- */
    INSERT INTO dbo.mes_route_category (parent_id, code, name, sort) VALUES
    (0, 'RC01', N'机加工艺', 1), (0, 'RC02', N'装配工艺', 2), (0, 'RC03', N'表面处理', 3);
    INSERT INTO dbo.mes_route (code, name, category_id, remark) VALUES
    ('RT-1001', N'法兰盘加工路线', 1, N'下料-车削-铣削-检验'),
    ('RT-1002', N'轴承座加工路线', 1, N'下料-粗车-精车-钻孔'),
    ('RT-2001', N'减速机装配路线', 2, N'组装-注油-测试'),
    ('RT-3001', N'镀锌处理路线', 3, N'除油-酸洗-镀锌');

    /* ---- 工单 / 排产任务 ---- */
    INSERT INTO dbo.mes_work_order (order_no, product_name, plan_qty, status, plan_start, plan_end) VALUES
    ('WO20261101', N'法兰盘 DN100', 500, 1, '2026-11-02', '2026-11-20'),
    ('WO20261102', N'轴承座 B-220', 300, 1, '2026-11-05', '2026-11-25'),
    ('WO20261103', N'减速机总成', 120, 1, '2026-11-08', '2026-11-30');

    INSERT INTO dbo.mes_task (task_no, work_order_id, route_id, workstation_id, assign_type, assignee_id, plan_qty, finished_qty, status, plan_start, plan_end) VALUES
    ('TK2026110101', 1, 1, 1, 'STATION', 1, 200, 0, 1, '2026-11-03', '2026-11-12'),
    ('TK2026110102', 1, 1, 2, 'PERSON', 4, 150, 0, 1, '2026-11-03', '2026-11-12'),
    ('TK2026110201', 2, 2, 2, 'STATION', 2, 300, 0, 0, '2026-11-06', '2026-11-18'),
    ('TK2026110301', 3, 3, 3, 'PERSON', 3, 120, 0, 0, '2026-11-10', '2026-11-25');

    /* 任务关联人员：JB-5 判定依据 */
    INSERT INTO dbo.mes_task_member (task_id, user_id, source) VALUES
    (1, 2, 'STATION'), (1, 4, 'STATION'), (1, 1, 'TEAM'),
    (2, 4, 'ASSIGNEE'),
    (3, 4, 'STATION'),
    (4, 3, 'ASSIGNEE'), (4, 5, 'TEAM');

    INSERT INTO dbo.mes_feedback (feedback_no, task_id, user_id, qty, status, audit_user_id, audit_time, audit_remark, create_time) VALUES
    ('FB2026110501', 1, 2, 60, 'PENDING', NULL, NULL, NULL, '2026-11-10 09:12:00'),
    ('FB2026110502', 2, 4, 40, 'PENDING', NULL, NULL, NULL, '2026-11-10 10:35:00'),
    ('FB2026110401', 1, 2, 100, 'APPROVED', 1, '2026-11-09 16:00:00', N'数量核对无误', '2026-11-08 14:20:00');

    /* ---- WMS ---- */
    INSERT INTO dbo.wms_warehouse (code, name, address) VALUES ('WH01', N'成品仓', N'一号厂房东侧'), ('WH02', N'原料仓', N'一号厂房西侧');
    INSERT INTO dbo.wms_item_category (parent_id, code, name) VALUES (0, 'IC01', N'原材料'), (0, 'IC02', N'标准件'), (0, 'IC03', N'成品');
    INSERT INTO dbo.wms_brand (name) VALUES (N'星曜自有'), (N'哈轴'), (N'万向');
    INSERT INTO dbo.wms_partner (code, name, type, contact, phone) VALUES
    ('P001', N'宁波恒信机械有限公司', 'CUSTOMER', N'王经理', '13911112222'),
    ('P002', N'江苏中冶钢材贸易', 'SUPPLIER', N'李经理', '13933334444');
    INSERT INTO dbo.wms_item (code, name, category_id, brand_id, spec, unit) VALUES
    ('SKU-1001', N'45#圆钢 Φ80', 1, 1, 'Φ80*6000mm', N'根'),
    ('SKU-1002', N'深沟球轴承 6208', 2, 2, '40*80*18', N'个'),
    ('SKU-1003', N'法兰盘 DN100 成品', 3, 1, 'DN100', N'件');
    INSERT INTO dbo.wms_stock (warehouse_id, item_id, qty) VALUES (2, 1, 320), (2, 2, 1500), (1, 3, 210);
    INSERT INTO dbo.wms_inout (order_no, type, warehouse_id, target_warehouse_id, status, remark) VALUES
    ('IO2026111001', 'IN', 2, NULL, 1, N'原料入库'),
    ('IO2026111002', 'OUT', 1, NULL, 0, N'成品发货');

    /* ---- ERP ---- */
    INSERT INTO dbo.erp_customer (code, name, contact, phone) VALUES ('C001', N'宁波恒信机械有限公司', N'王经理', '13911112222');
    INSERT INTO dbo.erp_supplier (code, name, contact, phone) VALUES ('S001', N'江苏中冶钢材贸易', N'李经理', '13933334444');
    INSERT INTO dbo.erp_product (code, name, unit, category_name) VALUES
    ('EP-1001', N'法兰盘 DN100', N'件', N'机加工成品'),
    ('EP-1002', N'轴承座 B-220', N'件', N'机加工成品'),
    ('EP-2001', N'减速机总成', N'台', N'装配成品');
    INSERT INTO dbo.erp_order (order_no, type, partner_name, amount, status, order_date) VALUES
    ('SO202611001', 'SALES', N'宁波恒信机械有限公司', 128000.00, 1, '2026-11-06'),
    ('PO202611001', 'PURCHASE', N'江苏中冶钢材贸易', 56000.00, 1, '2026-11-07');

    /* ---- 导入批次示例 ---- */
    INSERT INTO dbo.sys_import_batch (batch_no, biz_type, file_name, total_rows, success_rows, fail_rows, status, create_user) VALUES
    ('IMP20261110001', 'WMS_ITEM', N'物料导入模板_样例.xlsx', 12, 10, 2, 'PARTIAL', 'admin');
    INSERT INTO dbo.sys_import_error (batch_id, row_no, column_name, message, raw_data) VALUES
    (1, 4, '物料编码', N'编码已存在：SKU-1001', N'SKU-1001,45#圆钢 Φ80'),
    (1, 9, '单位', N'单位不能为空', N',深沟球轴承 6208');
END
GO
