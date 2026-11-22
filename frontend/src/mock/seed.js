/* 演示模式种子数据：字段名与真实后端返回保持一致 */

export const MENUS = [
  {
    path: '/mes', component: null, name: 'MES 制造', icon: 'Cpu', perms: null, id: 2, type: 'DIR',
    children: [
      { path: '/mes/task', component: 'mes/task/index', name: '排产任务', icon: 'Calendar', perms: 'mes:task:list', id: 21, type: 'MENU' },
      { path: '/mes/feedback', component: 'mes/feedback/index', name: '报工审核', icon: 'Finished', perms: 'mes:feedback:list', id: 22, type: 'MENU' },
      { path: '/mes/route', component: 'mes/route/index', name: '工艺路线', icon: 'Guide', perms: 'mes:route:list', id: 23, type: 'MENU' },
      { path: '/mes/route-category', component: 'mes/routeCategory/index', name: '工艺分类', icon: 'Files', perms: 'mes:routeCategory:list', id: 24, type: 'MENU' },
      { path: '/mes/workstation', component: 'mes/workstation/index', name: '工作站', icon: 'OfficeBuilding', perms: 'mes:workstation:list', id: 25, type: 'MENU' }
    ]
  },
  {
    path: '/wms', component: null, name: 'WMS 仓储', icon: 'Box', perms: null, id: 3, type: 'DIR',
    children: [
      { path: '/wms/item', component: 'wms/item/index', name: '物料管理', icon: 'Goods', perms: 'wms:item:list', id: 31, type: 'MENU' },
      { path: '/wms/warehouse', component: 'wms/warehouse/index', name: '仓库管理', icon: 'House', perms: 'wms:warehouse:list', id: 32, type: 'MENU' },
      { path: '/wms/inout', component: 'wms/inout/index', name: '出入库单', icon: 'Sort', perms: 'wms:inout:list', id: 33, type: 'MENU' }
    ]
  },
  {
    path: '/erp', component: null, name: 'ERP 经营', icon: 'DataAnalysis', perms: null, id: 4, type: 'DIR',
    children: [
      { path: '/erp/product', component: 'erp/product/index', name: '产品管理', icon: 'ShoppingBag', perms: 'erp:product:list', id: 41, type: 'MENU' },
      { path: '/erp/order', component: 'erp/order/index', name: '订单管理', icon: 'Tickets', perms: 'erp:order:list', id: 42, type: 'MENU' }
    ]
  },
  { path: '/import/center', component: 'import/center/index', name: '导入中心', icon: 'Upload', perms: 'import:center:list', id: 5, type: 'MENU' },
  { path: '/drawing/batch', component: 'drawing/batch/index', name: '图纸识别', icon: 'Picture', perms: 'drawing:batch:list', id: 6, type: 'MENU' },
  {
    path: '/system', component: null, name: '系统管理', icon: 'Setting', perms: null, id: 1, type: 'DIR',
    children: [
      { path: '/system/user', component: 'system/user/index', name: '用户管理', icon: 'User', perms: 'system:user:list', id: 11, type: 'MENU' },
      { path: '/system/role', component: 'system/role/index', name: '角色管理', icon: 'Avatar', perms: 'system:role:list', id: 12, type: 'MENU' },
      { path: '/system/login-log', component: 'system/loginlog/index', name: '登录日志', icon: 'Document', perms: 'system:log:list', id: 13, type: 'MENU' }
    ]
  }
]

export const SEED = {
  users: [
    { id: 1, username: 'admin', password: '123456', realName: '系统管理员', phone: '13800000000', deptName: '信息部', status: 1, roleIds: [1], createTime: '2026-11-16T18:56:41' },
    { id: 2, username: 'zhangwei', password: '123456', realName: '张伟', phone: '13800000001', deptName: '机加车间', status: 1, roleIds: [2], createTime: '2026-11-16T18:56:41' },
    { id: 3, username: 'lina', password: '123456', realName: '李娜', phone: '13800000002', deptName: '装配车间', status: 1, roleIds: [2], createTime: '2026-11-16T18:56:41' },
    { id: 4, username: 'wangqiang', password: '123456', realName: '王强', phone: '13800000003', deptName: '机加车间', status: 1, roleIds: [3], createTime: '2026-11-16T18:56:41' },
    { id: 5, username: 'liuyang', password: '123456', realName: '刘洋', phone: '13800000004', deptName: '装配车间', status: 1, roleIds: [3], createTime: '2026-11-16T18:56:41' }
  ],
  roles: [
    { id: 1, roleCode: 'ADMIN', roleName: '超级管理员', status: 1, remark: '全部权限' },
    { id: 2, roleCode: 'MES_OPERATOR', roleName: '车间报工员', status: 1, remark: 'MES 与图纸识别' },
    { id: 3, roleCode: 'VIEWER', roleName: '只读用户', status: 1, remark: '仅查看' }
  ],
  workshops: [
    { id: 1, code: 'WS01', name: '机加车间', status: 1 },
    { id: 2, code: 'WS02', name: '装配车间', status: 1 }
  ],
  workstations: [
    { id: 1, code: 'ST01', name: 'CNC-01 加工中心', workshopId: 1, status: 1 },
    { id: 2, code: 'ST02', name: 'CNC-02 加工中心', workshopId: 1, status: 1 },
    { id: 3, code: 'ST03', name: '装配线 A', workshopId: 2, status: 1 },
    { id: 4, code: 'ST04', name: '装配线 B', workshopId: 2, status: 1 }
  ],
  stationMembers: { 1: [2, 4], 2: [2, 4], 3: [3, 5], 4: [3, 5] },
  routeCategories: [
    { id: 1, parentId: 0, code: 'RC01', name: '机加工艺', sort: 1, status: 1 },
    { id: 2, parentId: 0, code: 'RC02', name: '装配工艺', sort: 2, status: 1 },
    { id: 3, parentId: 0, code: 'RC03', name: '表面处理', sort: 3, status: 1 }
  ],
  routes: [
    { id: 1, code: 'RT-1001', name: '法兰盘加工路线', categoryId: 1, remark: '下料-车削-铣削-检验', status: 1 },
    { id: 2, code: 'RT-1002', name: '轴承座加工路线', categoryId: 1, remark: '下料-粗车-精车-钻孔', status: 1 },
    { id: 3, code: 'RT-2001', name: '减速机装配路线', categoryId: 2, remark: '组装-注油-测试', status: 1 },
    { id: 4, code: 'RT-3001', name: '镀锌处理路线', categoryId: 3, remark: '除油-酸洗-镀锌', status: 1 }
  ],
  workOrders: [
    { id: 1, orderNo: 'WO20261101', productName: '法兰盘 DN100', planQty: 500, status: 1, planStart: '2026-11-02T00:00:00', planEnd: '2026-11-20T00:00:00', createTime: '2026-11-16T18:56:41' },
    { id: 2, orderNo: 'WO20261102', productName: '轴承座 B-220', planQty: 300, status: 1, planStart: '2026-11-05T00:00:00', planEnd: '2026-11-25T00:00:00', createTime: '2026-11-16T18:56:41' },
    { id: 3, orderNo: 'WO20261103', productName: '减速机总成', planQty: 120, status: 1, planStart: '2026-11-08T00:00:00', planEnd: '2026-11-30T00:00:00', createTime: '2026-11-16T18:56:41' }
  ],
  tasks: [
    { id: 1, taskNo: 'TK2026110101', workOrderId: 1, routeId: 1, workstationId: 1, assignType: 'STATION', assigneeId: 1, planQty: 200, finishedQty: 0, status: 1, planStart: '2026-11-03T00:00:00', planEnd: '2026-11-12T00:00:00', createTime: '2026-11-16T18:56:41' },
    { id: 2, taskNo: 'TK2026110102', workOrderId: 1, routeId: 1, workstationId: 2, assignType: 'PERSON', assigneeId: 4, planQty: 150, finishedQty: 40, status: 1, planStart: '2026-11-03T00:00:00', planEnd: '2026-11-12T00:00:00', createTime: '2026-11-16T18:56:41' },
    { id: 3, taskNo: 'TK2026110201', workOrderId: 2, routeId: 2, workstationId: 2, assignType: 'STATION', assigneeId: 2, planQty: 300, finishedQty: 0, status: 0, planStart: '2026-11-06T00:00:00', planEnd: '2026-11-18T00:00:00', createTime: '2026-11-16T18:56:41' },
    { id: 4, taskNo: 'TK2026110301', workOrderId: 3, routeId: 3, workstationId: 3, assignType: 'PERSON', assigneeId: 3, planQty: 120, finishedQty: 0, status: 0, planStart: '2026-11-10T00:00:00', planEnd: '2026-11-25T00:00:00', createTime: '2026-11-16T18:56:41' }
  ],
  feedback: [
    { id: 1, feedbackNo: 'FB2026110401', taskId: 1, userId: 2, qty: 60, status: 'PENDING', auditUserId: null, auditTime: null, auditRemark: null, createTime: '2026-11-08T14:20:00' },
    { id: 2, feedbackNo: 'FB2026110501', taskId: 2, userId: 4, qty: 40, status: 'APPROVED', auditUserId: 1, auditTime: '2026-11-09T16:00:00', auditRemark: '数量核对无误', createTime: '2026-11-08T15:20:00' },
    { id: 3, feedbackNo: 'FB2026110601', taskId: 3, userId: 3, qty: 30, status: 'PENDING', auditUserId: null, auditTime: null, auditRemark: null, createTime: '2026-11-10T09:10:00' }
  ],
  warehouses: [
    { id: 1, code: 'WH01', name: '成品仓', address: '一号厂房东侧', status: 1 },
    { id: 2, code: 'WH02', name: '原料仓', address: '一号厂房西侧', status: 1 }
  ],
  itemCategories: [
    { id: 1, parentId: 0, code: 'IC01', name: '原材料', status: 1 },
    { id: 2, parentId: 0, code: 'IC02', name: '标准件', status: 1 },
    { id: 3, parentId: 0, code: 'IC03', name: '成品', status: 1 }
  ],
  brands: [
    { id: 1, name: '星曜自有', status: 1 },
    { id: 2, name: '哈轴', status: 1 },
    { id: 3, name: '万向', status: 1 }
  ],
  partners: [
    { id: 1, code: 'P001', name: '宁波恒信机械有限公司', type: 'CUSTOMER', contact: '王经理', phone: '13911112222', status: 1 },
    { id: 2, code: 'P002', name: '江苏中冶钢材贸易', type: 'SUPPLIER', contact: '李经理', phone: '13933334444', status: 1 }
  ],
  items: [
    { id: 1, code: 'SKU-1001', name: '45# 圆钢 Φ80', categoryId: 1, brandId: 1, spec: 'Φ80', unit: 'kg', status: 1, createTime: '2026-11-16T18:56:41' },
    { id: 2, code: 'SKU-1002', name: '深沟球轴承 6205', categoryId: 2, brandId: 2, spec: '6205', unit: '套', status: 1, createTime: '2026-11-16T18:56:41' },
    { id: 3, code: 'SKU-1003', name: '法兰盘 DN100 成品', categoryId: 3, brandId: 1, spec: 'DN100', unit: '件', status: 1, createTime: '2026-11-16T18:56:41' }
  ],
  stocks: [
    { id: 1, warehouseId: 2, itemId: 1, qty: 1200 },
    { id: 2, warehouseId: 2, itemId: 2, qty: 300 },
    { id: 3, warehouseId: 1, itemId: 3, qty: 210 }
  ],
  inouts: [
    { id: 1, orderNo: 'IO2026111001', type: 'IN', warehouseId: 2, targetWarehouseId: null, status: 1, remark: '原料入库', createTime: '2026-11-16T18:56:41', lines: [{ itemId: 1, qty: 500, price: 6.5 }] },
    { id: 2, orderNo: 'IO2026111002', type: 'OUT', warehouseId: 1, targetWarehouseId: null, status: 0, remark: '成品发货', createTime: '2026-11-16T18:56:41', lines: [{ itemId: 3, qty: 20, price: 88 }] }
  ],
  erpProducts: [
    { id: 1, code: 'EP-1001', name: '法兰盘 DN100', unit: '件', categoryName: '机加工成品', status: 1 },
    { id: 2, code: 'EP-1002', name: '轴承座 B-220', unit: '件', categoryName: '机加工成品', status: 1 },
    { id: 3, code: 'EP-2001', name: '减速机总成', unit: '台', categoryName: '装配成品', status: 1 }
  ],
  erpOrders: [
    { id: 1, orderNo: 'SO202611001', type: 'SALES', partnerId: 1, partnerName: '宁波恒信机械有限公司', amount: 126000, status: 1, orderDate: '2026-11-06' },
    { id: 2, orderNo: 'PO202611001', type: 'PURCHASE', partnerId: 2, partnerName: '江苏中冶钢材贸易', amount: 56000, status: 1, orderDate: '2026-11-07' }
  ],
  importBatches: [
    { id: 1, batchNo: 'IMP20261110001', bizType: 'WMS_ITEM', fileName: '物料导入模板_样例.xlsx', totalRows: 12, successRows: 10, failRows: 2, status: 'PARTIAL', createUser: 'admin', createTime: '2026-11-16T18:56:41' }
  ],
  importErrors: [
    { id: 1, batchId: 1, rowNo: 5, columnName: '物料编码', message: '物料编码不能为空', rawData: ',,原材料,星曜自有' },
    { id: 2, batchId: 1, rowNo: 9, columnName: '分类', message: '分类不存在：五金件', rawData: 'SKU-2009,垫片,五金件,星曜自有' }
  ],
  drawingTasks: [
    { id: 1, taskNo: 'DRW20261110001', fileCount: 2, successCount: 2, failCount: 0, status: 'DONE', createUser: 'admin', createTime: '2026-11-16T18:56:41' }
  ],
  drawingFiles: [
    { id: 1, taskId: 1, fileName: '法兰盘_DN100.pdf', fileSize: 348160, status: 'SUCCESS', recognizeResult: '件号：FL-100；材质：45#；规格：DN100；数量：1' },
    { id: 2, taskId: 1, fileName: '轴承座_B220.png', fileSize: 512000, status: 'SUCCESS', recognizeResult: '件号：BZ-220；材质：HT250；规格：B-220；数量：1' }
  ],
  loginLogs: [
    { id: 3, username: 'admin', ip: '127.0.0.1', success: 1, message: '登录成功', createTime: '2026-11-22T09:30:00' },
    { id: 2, username: 'zhangwei', ip: '10.231.99.20', success: 1, message: '登录成功', createTime: '2026-11-21T14:12:00' },
    { id: 1, username: 'admin', ip: '127.0.0.1', success: 0, message: '密码错误', createTime: '2026-11-20T08:05:00' }
  ]
}
