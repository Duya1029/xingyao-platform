/* 演示模式：在浏览器内模拟后端接口，数据保存在 localStorage */
import { SEED, MENUS } from './seed'

export const DEMO = import.meta.env.VITE_DEMO === '1'
const DB_KEY = 'xy_demo_db_v1'
const SESSION_KEY = 'xy_demo_user'

const clone = (v) => JSON.parse(JSON.stringify(v))

let cache = null

function db() {
  if (cache) return cache
  const saved = localStorage.getItem(DB_KEY)
  if (saved) {
    try {
      cache = JSON.parse(saved)
    } catch (e) {
      cache = clone(SEED)
    }
  } else {
    cache = clone(SEED)
  }
  for (const k of Object.keys(SEED)) {
    if (!(k in cache)) cache[k] = clone(SEED[k])
  }
  return cache
}

function save() {
  localStorage.setItem(DB_KEY, JSON.stringify(cache))
}

const ok = (data = null) => ({ code: 200, msg: 'ok', data })
const fail = (msg, code = 400) => ({ code, msg, data: null })
const now = () => new Date().toISOString().slice(0, 19)
const nextId = (arr) => (arr.length ? Math.max(...arr.map((x) => Number(x.id) || 0)) + 1 : 1)

function pageOf(arr, params = {}) {
  const pageNum = Number(params.pageNum) || 1
  const pageSize = Number(params.pageSize) || 10
  const start = (pageNum - 1) * pageSize
  return {
    records: arr.slice(start, start + pageSize),
    total: arr.length,
    size: pageSize,
    current: pageNum,
    pages: Math.ceil(arr.length / pageSize)
  }
}

function kwFilter(arr, keyword) {
  if (!keyword) return arr
  const k = String(keyword).toLowerCase()
  return arr.filter((row) => JSON.stringify(row).toLowerCase().includes(k))
}

function currentUser() {
  const username = localStorage.getItem(SESSION_KEY)
  if (!username) return null
  return db().users.find((u) => u.username === username) || null
}

function roleCodeOf(user) {
  const role = db().roles.find((r) => (user.roleIds || []).includes(r.id))
  return role?.roleCode || 'VIEWER'
}

function permsOf(user) {
  const role = roleCodeOf(user)
  if (role === 'ADMIN') {
    return MENUS.flatMap((m) => (m.children || [m]).map((x) => x.perms)).filter(Boolean)
  }
  const allowed = role === 'MES_OPERATOR' ? ['/mes', '/drawing/batch'] : ['/wms', '/erp']
  return MENUS.filter((m) => allowed.includes(m.path))
    .flatMap((m) => (m.children || [m]).map((x) => x.perms))
    .filter(Boolean)
}

function publicUser(user) {
  if (!user) return null
  const { password, ...rest } = user
  return { ...rest, roles: [roleCodeOf(user)], perms: permsOf(user) }
}

function menusOf(user) {
  const role = roleCodeOf(user)
  if (role === 'ADMIN') return clone(MENUS)
  const allowed = role === 'MES_OPERATOR' ? ['/mes', '/drawing/batch'] : ['/wms', '/erp']
  return clone(MENUS.filter((m) => allowed.includes(m.path)))
}

function userById(id) {
  return db().users.find((u) => u.id === Number(id))
}

function stationName(id) {
  return db().workstations.find((w) => w.id === Number(id))?.name || ''
}

function enrichTask(t) {
  const d = db()
  const wo = d.workOrders.find((x) => x.id === t.workOrderId)
  const route = d.routes.find((x) => x.id === t.routeId)
  const st = d.workstations.find((x) => x.id === t.workstationId)
  const assignee = userById(t.assigneeId)
  const reported = d.feedback
    .filter((f) => f.taskId === t.id && (f.status === 'PENDING' || f.status === 'APPROVED'))
    .reduce((s, f) => s + Number(f.qty || 0), 0)
  return {
    ...t,
    orderNo: wo?.orderNo || '',
    productName: wo?.productName || '',
    routeName: route?.name || '',
    workstationName: st?.name || '',
    assigneeName: t.assignType === 'PERSON' ? assignee?.realName || '' : st?.name || '',
    reportedQty: reported,
    remainQty: Math.max(0, Number(t.planQty || 0) - reported)
  }
}

function enrichFeedback(f) {
  const d = db()
  const task = d.tasks.find((t) => t.id === f.taskId)
  return {
    ...f,
    taskNo: task?.taskNo || '',
    userName: userById(f.userId)?.realName || '',
    planQty: task?.planQty || 0,
    finishedQty: task?.finishedQty || 0
  }
}

function options() {
  const d = db()
  return {
    workstations: d.workstations.map((w) => ({
      ...w,
      workshopName: d.workshops.find((x) => x.id === w.workshopId)?.name || '',
      memberCount: (d.stationMembers[w.id] || []).length
    })),
    routes: d.routes.map((r) => ({
      ...r,
      categoryName: d.routeCategories.find((c) => c.id === r.categoryId)?.name || ''
    })),
    routeCategories: d.routeCategories.map((c) => ({
      ...c,
      routeCount: d.routes.filter((r) => r.categoryId === c.id).length
    })),
    workOrders: d.workOrders,
    brands: d.brands,
    partners: d.partners,
    workshops: d.workshops,
    warehouses: d.warehouses,
    itemCategories: d.itemCategories,
    users: d.users.map(({ password, ...u }) => u),
    products: d.erpProducts
  }
}

function overview() {
  const d = db()
  const tasks = [...d.tasks].sort((a, b) => b.id - a.id)
  return {
    taskTotal: d.tasks.length,
    taskRunning: d.tasks.filter((t) => Number(t.status) === 1).length,
    taskFinished: d.tasks.filter((t) => Number(t.status) === 2).length,
    itemTotal: d.items.length,
    feedbackPending: d.feedback.filter((f) => f.status === 'PENDING').length,
    recentPlanQty: d.tasks.reduce((s, t) => s + Number(t.planQty || 0), 0),
    recentTasks: tasks.slice(0, 5).map(enrichTask)
  }
}

function mockResult(method, path, params, body) {
  const d = db()
  const m = method.toLowerCase()

  /* ---------- 认证 ---------- */
  if (path === '/auth/config') return ok({ captchaEnabled: false })
  if (path === '/auth/captcha') return ok({ key: 'demo', image: '' })
  if (path === '/auth/login' && m === 'post') {
    const user = d.users.find((u) => u.username === body.username)
    if (!user || user.password !== body.password) return fail('用户名或密码错误')
    if (Number(user.status) !== 1) return fail('账号已被停用')
    localStorage.setItem(SESSION_KEY, user.username)
    d.loginLogs.unshift({ id: nextId(d.loginLogs), username: user.username, ip: '127.0.0.1', success: 1, message: '登录成功', createTime: now() })
    save()
    return ok({ token: 'demo-' + user.username })
  }
  if (path === '/auth/register' && m === 'post') {
    if (d.users.some((u) => u.username === body.username)) return fail('用户名已存在')
    const viewer = d.roles.find((r) => r.roleCode === 'VIEWER')
    d.users.push({
      id: nextId(d.users),
      username: body.username,
      password: body.password,
      realName: body.realName || body.username,
      phone: body.phone || '',
      deptName: body.deptName || '',
      status: 1,
      roleIds: [viewer?.id || 3],
      createTime: now()
    })
    save()
    return ok(null)
  }
  if (path === '/auth/info') {
    const user = currentUser()
    if (!user) return fail('未登录或登录已过期', 401)
    return ok(publicUser(user))
  }
  if (path === '/auth/menus') {
    const user = currentUser()
    if (!user) return fail('未登录或登录已过期', 401)
    return ok(menusOf(user))
  }
  if (path === '/auth/logout') {
    localStorage.removeItem(SESSION_KEY)
    return ok(null)
  }

  /* ---------- 公共选项 ---------- */
  if (path === '/lookup/options') return ok(options())
  if (path === '/dashboard/overview') return ok(overview())

  /* ---------- 系统管理 ---------- */
  if (path === '/system/users' && m === 'get') {
    return ok(pageOf(kwFilter([...d.users].sort((a, b) => b.id - a.id), params.keyword), params))
  }
  if (path === '/system/users/all') return ok(d.users.map(({ password, ...u }) => u))
  if (path === '/system/users' && m === 'post') {
    if (d.users.some((u) => u.username === body.username)) return fail('用户名已存在')
    d.users.push({ ...body, id: nextId(d.users), password: '123456', status: body.status ?? 1, createTime: now() })
    save()
    return ok(null)
  }
  if (/^\/system\/users\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.users.findIndex((u) => u.id === id)
    if (m === 'put') {
      if (idx >= 0) d.users[idx] = { ...d.users[idx], ...body, id }
      save()
      return ok(null)
    }
    if (m === 'delete') {
      if (id === 1) return fail('管理员账号不能删除')
      d.users = d.users.filter((u) => u.id !== id)
      save()
      return ok(null)
    }
  }
  if (path === '/system/roles') return ok(d.roles)
  if (path === '/system/login-logs') {
    return ok(pageOf([...d.loginLogs].sort((a, b) => b.id - a.id), params))
  }

  /* ---------- MES 任务 ---------- */
  if (path === '/mes/tasks' && m === 'get') {
    let list = [...d.tasks].sort((a, b) => b.id - a.id)
    if (params.assignType) list = list.filter((t) => t.assignType === params.assignType)
    if (params.status !== undefined && params.status !== '') list = list.filter((t) => String(t.status) === String(params.status))
    list = kwFilter(list, params.keyword)
    const paged = pageOf(list, params)
    paged.records = paged.records.map(enrichTask)
    return ok(paged)
  }
  if (/^\/mes\/tasks\/\d+$/.test(path) && m === 'get') {
    const id = Number(path.split('/').pop())
    const t = d.tasks.find((x) => x.id === id)
    if (!t) return fail('任务不存在', 404)
    return ok({ ...enrichTask(t), members: d.stationMembers[t.workstationId] || [] })
  }
  if (path === '/mes/tasks' && m === 'post') {
    if (body.assignType === 'PERSON' && !body.assigneeId) return fail('指派到个人时必须选择指派人员')
    if (body.assignType === 'STATION' && !body.workstationId) return fail('指派到工作站时必须选择工作站')
    const task = {
      ...body,
      id: nextId(d.tasks),
      taskNo: body.taskNo || 'TK' + Date.now().toString().slice(-8),
      finishedQty: 0,
      status: body.status ?? 0,
      createTime: now()
    }
    d.tasks.push(task)
    save()
    return ok(task)
  }
  if (/^\/mes\/tasks\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.tasks.findIndex((x) => x.id === id)
    if (m === 'put') {
      if (body.assignType === 'PERSON' && !body.assigneeId) return fail('指派到个人时必须选择指派人员')
      if (body.assignType === 'STATION' && !body.workstationId) return fail('指派到工作站时必须选择工作站')
      if (idx >= 0) d.tasks[idx] = { ...d.tasks[idx], ...body, id }
      save()
      return ok(null)
    }
    if (m === 'delete') {
      d.tasks = d.tasks.filter((x) => x.id !== id)
      d.feedback = d.feedback.filter((f) => f.taskId !== id)
      save()
      return ok(null)
    }
  }

  /* ---------- MES 报工 ---------- */
  if (path === '/mes/feedback' && m === 'get') {
    let list = [...d.feedback].sort((a, b) => b.id - a.id)
    if (params.status) list = list.filter((f) => f.status === params.status)
    if (params.keyword) list = list.filter((f) => String(f.feedbackNo).includes(params.keyword))
    const paged = pageOf(list, params)
    paged.records = paged.records.map(enrichFeedback)
    return ok(paged)
  }
  if (path === '/mes/feedback' && m === 'post') {
    const task = d.tasks.find((t) => t.id === Number(body.taskId))
    if (!task) return fail('任务不存在', 404)
    const user = currentUser()
    const related = roleCodeOf(user) === 'ADMIN' ||
      Number(task.assigneeId) === Number(user.id) ||
      (d.stationMembers[task.workstationId] || []).includes(user.id)
    if (!related) return fail('您不是本订单关联人员，不允许报工', 403)
    const approved = d.feedback
      .filter((f) => f.taskId === task.id && f.status === 'APPROVED')
      .reduce((s, f) => s + Number(f.qty || 0), 0)
    const pending = d.feedback
      .filter((f) => f.taskId === task.id && f.status === 'PENDING')
      .reduce((s, f) => s + Number(f.qty || 0), 0)
    const remain = Number(task.planQty) - approved - pending
    if (Number(body.qty) > remain) return fail(`本次报工数量超过剩余可报数量（剩余 ${remain}）`)
    const fb = {
      ...body,
      id: nextId(d.feedback),
      feedbackNo: 'FB' + Date.now().toString().slice(-10),
      userId: user.id,
      status: 'PENDING',
      createTime: now()
    }
    d.feedback.push(fb)
    save()
    return ok(fb)
  }
  if (/^\/mes\/feedback\/\d+\/(approve|reject)$/.test(path)) {
    const parts = path.split('/')
    const id = Number(parts[3])
    const action = parts[4]
    const fb = d.feedback.find((f) => f.id === id)
    if (!fb) return fail('报工记录不存在', 404)
    if (action === 'reject') {
      fb.status = 'REJECTED'
      fb.auditRemark = body?.remark || ''
      fb.auditTime = now()
      save()
      return ok(null)
    }
    const task = d.tasks.find((t) => t.id === fb.taskId)
    if (!task) return fail('任务不存在', 404)
    const approved = d.feedback
      .filter((f) => f.taskId === task.id && f.status === 'APPROVED' && f.id !== id)
      .reduce((s, f) => s + Number(f.qty || 0), 0)
    if (approved + Number(fb.qty) > Number(task.planQty)) {
      return fail(`通过后累计报工将超过排产数量（最多可再通过 ${Number(task.planQty) - approved}）`)
    }
    fb.status = 'APPROVED'
    fb.auditRemark = body?.remark || ''
    fb.auditTime = now()
    fb.auditUserId = currentUser()?.id || 1
    const total = d.feedback
      .filter((f) => f.taskId === task.id && f.status === 'APPROVED')
      .reduce((s, f) => s + Number(f.qty || 0), 0)
    task.finishedQty = total
    task.status = total >= Number(task.planQty) ? 2 : 1
    save()
    return ok(null)
  }

  /* ---------- 工艺路线 ---------- */
  if (path === '/mes/routes' && m === 'get') {
    let list = [...d.routes]
    if (params.categoryId) list = list.filter((r) => r.categoryId === Number(params.categoryId))
    if (params.keyword) list = kwFilter(list, params.keyword)
    return ok(list.map((r) => ({ ...r, categoryName: d.routeCategories.find((c) => c.id === r.categoryId)?.name || '' })))
  }
  if (path === '/mes/routes' && m === 'post') {
    const r = { ...body, id: nextId(d.routes), status: body.status ?? 1 }
    d.routes.push(r)
    save()
    return ok(r)
  }
  if (/^\/mes\/routes\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.routes.findIndex((x) => x.id === id)
    if (m === 'put' && idx >= 0) d.routes[idx] = { ...d.routes[idx], ...body, id }
    if (m === 'delete') d.routes = d.routes.filter((x) => x.id !== id)
    save()
    return ok(null)
  }

  /* ---------- 工艺分类 ---------- */
  if (path === '/mes/route-categories' && m === 'get') {
    return ok(kwFilter(d.routeCategories, params.keyword).map((c) => ({
      ...c,
      routeCount: d.routes.filter((r) => r.categoryId === c.id).length
    })))
  }
  if (path === '/mes/route-categories' && m === 'post') {
    const c = { ...body, id: nextId(d.routeCategories), parentId: body.parentId || 0, status: body.status ?? 1 }
    d.routeCategories.push(c)
    save()
    return ok(c)
  }
  if (/^\/mes\/route-categories\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    if (m === 'delete') {
      if (d.routes.some((r) => r.categoryId === id)) return fail('该分类下存在工艺路线，不能删除')
      if (d.routeCategories.some((c) => c.parentId === id)) return fail('该分类下存在子分类，不能删除')
      d.routeCategories = d.routeCategories.filter((c) => c.id !== id)
      save()
      return ok(null)
    }
    if (m === 'put') {
      const idx = d.routeCategories.findIndex((c) => c.id === id)
      if (idx >= 0) d.routeCategories[idx] = { ...d.routeCategories[idx], ...body, id }
      save()
      return ok(null)
    }
  }

  /* ---------- 工作站 ---------- */
  if (path === '/mes/workstations' && m === 'get') {
    let list = d.workstations.map((w) => ({
      ...w,
      workshopName: d.workshops.find((x) => x.id === w.workshopId)?.name || '',
      memberCount: (d.stationMembers[w.id] || []).length
    }))
    if (params.keyword) list = kwFilter(list, params.keyword)
    return ok(list)
  }
  if (path === '/mes/workstations' && m === 'post') {
    const w = { ...body, id: nextId(d.workstations), status: body.status ?? 1 }
    d.workstations.push(w)
    save()
    return ok(w)
  }
  if (/^\/mes\/workstations\/\d+\/members$/.test(path)) {
    const id = Number(path.split('/')[3])
    if (m === 'get') return ok((d.stationMembers[id] || []).map(userById).filter(Boolean).map(({ password, ...u }) => u))
    if (m === 'post') {
      d.stationMembers[id] = Array.isArray(body) ? body.map(Number) : []
      save()
      return ok(null)
    }
  }
  if (/^\/mes\/workstations\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.workstations.findIndex((x) => x.id === id)
    if (m === 'put' && idx >= 0) d.workstations[idx] = { ...d.workstations[idx], ...body, id }
    if (m === 'delete') d.workstations = d.workstations.filter((x) => x.id !== id)
    save()
    return ok(null)
  }
  if (path === '/mes/workshops') return ok(d.workshops)
  if (path === '/mes/work-orders') return ok(pageOf([...d.workOrders].sort((a, b) => b.id - a.id), params))

  /* ---------- WMS ---------- */
  if (path === '/wms/warehouses' && m === 'get') return ok(d.warehouses)
  if (path === '/wms/warehouses' && m === 'post') {
    const w = { ...body, id: nextId(d.warehouses), status: body.status ?? 1 }
    d.warehouses.push(w)
    save()
    return ok(w)
  }
  if (/^\/wms\/warehouses\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.warehouses.findIndex((x) => x.id === id)
    if (m === 'put' && idx >= 0) d.warehouses[idx] = { ...d.warehouses[idx], ...body, id }
    if (m === 'delete') d.warehouses = d.warehouses.filter((x) => x.id !== id)
    save()
    return ok(null)
  }
  if (path === '/wms/item-categories') return ok(d.itemCategories)
  if (path === '/wms/items' && m === 'get') {
    let list = d.items.map((it) => ({
      ...it,
      categoryName: d.itemCategories.find((c) => c.id === it.categoryId)?.name || '',
      brandName: d.brands.find((b) => b.id === it.brandId)?.name || '',
      stockQty: d.stocks.filter((s) => s.itemId === it.id).reduce((sum, s) => sum + Number(s.qty || 0), 0)
    }))
    if (params.categoryId) list = list.filter((x) => x.categoryId === Number(params.categoryId))
    list = kwFilter(list, params.keyword)
    return ok(pageOf(list.sort((a, b) => b.id - a.id), params))
  }
  if (path === '/wms/items' && m === 'post') {
    const it = { ...body, id: nextId(d.items), status: body.status ?? 1, createTime: now() }
    d.items.push(it)
    save()
    return ok(it)
  }
  if (/^\/wms\/items\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.items.findIndex((x) => x.id === id)
    if (m === 'put' && idx >= 0) d.items[idx] = { ...d.items[idx], ...body, id }
    if (m === 'delete') d.items = d.items.filter((x) => x.id !== id)
    save()
    return ok(null)
  }
  if (path === '/wms/stocks') {
    return ok(pageOf(d.stocks.map((s) => ({
      ...s,
      warehouseName: d.warehouses.find((w) => w.id === s.warehouseId)?.name || '',
      itemName: d.items.find((i) => i.id === s.itemId)?.name || '',
      itemCode: d.items.find((i) => i.id === s.itemId)?.code || ''
    })), params))
  }
  if (path === '/wms/inouts' && m === 'get') {
    const list = d.inouts.map((o) => ({
      ...o,
      warehouseName: d.warehouses.find((w) => w.id === o.warehouseId)?.name || ''
    }))
    return ok(pageOf([...list].sort((a, b) => b.id - a.id), params))
  }
  if (path === '/wms/inouts' && m === 'post') {
    const o = {
      ...body,
      id: nextId(d.inouts),
      orderNo: 'IO' + Date.now().toString().slice(-10),
      status: 0,
      createTime: now(),
      lines: body.lines || []
    }
    d.inouts.push(o)
    save()
    return ok(o)
  }
  if (/^\/wms\/inouts\/\d+\/post$/.test(path)) {
    const id = Number(path.split('/')[3])
    const o = d.inouts.find((x) => x.id === id)
    if (!o) return fail('单据不存在', 404)
    if (Number(o.status) === 1) return fail('单据已过账')
    if (o.type === 'IN') {
      for (const line of o.lines || []) {
        let s = d.stocks.find((x) => x.warehouseId === o.warehouseId && x.itemId === Number(line.itemId))
        if (!s) {
          s = { id: nextId(d.stocks), warehouseId: o.warehouseId, itemId: Number(line.itemId), qty: 0 }
          d.stocks.push(s)
        }
        s.qty = Number(s.qty || 0) + Number(line.qty || 0)
      }
    } else {
      for (const line of o.lines || []) {
        const s = d.stocks.find((x) => x.warehouseId === o.warehouseId && x.itemId === Number(line.itemId))
        if (!s || Number(s.qty) < Number(line.qty)) return fail('库存不足，无法出库')
        s.qty = Number(s.qty) - Number(line.qty)
      }
    }
    o.status = 1
    save()
    return ok(null)
  }

  /* ---------- ERP ---------- */
  if (path === '/erp/products' && m === 'get') {
    return ok(pageOf(kwFilter([...d.erpProducts].sort((a, b) => b.id - a.id), params.keyword), params))
  }
  if (path === '/erp/products' && m === 'post') {
    const p = { ...body, id: nextId(d.erpProducts), status: body.status ?? 1 }
    d.erpProducts.push(p)
    save()
    return ok(p)
  }
  if (/^\/erp\/products\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.erpProducts.findIndex((x) => x.id === id)
    if (m === 'put' && idx >= 0) d.erpProducts[idx] = { ...d.erpProducts[idx], ...body, id }
    if (m === 'delete') d.erpProducts = d.erpProducts.filter((x) => x.id !== id)
    save()
    return ok(null)
  }
  if (path === '/erp/orders' && m === 'get') {
    let list = [...d.erpOrders].sort((a, b) => b.id - a.id)
    if (params.type) list = list.filter((o) => o.type === params.type)
    list = kwFilter(list, params.keyword)
    return ok(pageOf(list, params))
  }
  if (path === '/erp/orders' && m === 'post') {
    const partner = d.partners.find((p) => p.id === Number(body.partnerId))
    const o = {
      ...body,
      id: nextId(d.erpOrders),
      orderNo: body.orderNo || (body.type === 'SALES' ? 'SO' : 'PO') + Date.now().toString().slice(-8),
      partnerName: partner?.name || body.partnerName || '',
      status: body.status ?? 0
    }
    d.erpOrders.push(o)
    save()
    return ok(o)
  }
  if (/^\/erp\/orders\/\d+$/.test(path)) {
    const id = Number(path.split('/').pop())
    const idx = d.erpOrders.findIndex((x) => x.id === id)
    if (m === 'put') {
      const partner = d.partners.find((p) => p.id === Number(body.partnerId))
      const partnerName = partner?.name || body.partnerName || ''
      if (idx >= 0) d.erpOrders[idx] = { ...d.erpOrders[idx], ...body, partnerName, id }
    }
    if (m === 'delete') d.erpOrders = d.erpOrders.filter((x) => x.id !== id)
    save()
    return ok(null)
  }

  /* ---------- 导入中心 ---------- */
  if (path === '/import/types') {
    return ok([
      { bizType: 'WMS_ITEM', label: 'WMS 物料基础数据' },
      { bizType: 'MES_WORKSTATION', label: 'MES 工作站基础数据' },
      { bizType: 'MES_ROUTE_CATEGORY', label: 'MES 工艺分类' },
      { bizType: 'ERP_PRODUCT', label: 'ERP 产品基础数据' }
    ])
  }
  if (path === '/import/batches') return ok(pageOf([...d.importBatches].sort((a, b) => b.id - a.id), params))
  if (/^\/import\/batches\/\d+\/errors$/.test(path)) {
    const id = Number(path.split('/')[3])
    return ok(d.importErrors.filter((e) => e.batchId === id))
  }
  if (path === '/import/upload' && m === 'post') {
    const bizType = body?.get ? body.get('bizType') : 'WMS_ITEM'
    const file = body?.get ? body.get('file') : null
    const total = 8
    const failRows = file?.name?.includes('错误') ? 2 : 0
    const success = total - failRows
    const batch = {
      id: nextId(d.importBatches),
      batchNo: 'IMP' + Date.now().toString().slice(-11),
      bizType,
      fileName: file?.name || '导入文件.xlsx',
      totalRows: total,
      successRows: success,
      failRows,
      status: failRows ? 'PARTIAL' : 'DONE',
      createUser: currentUser()?.username || 'admin',
      createTime: now()
    }
    d.importBatches.push(batch)
    if (failRows) {
      d.importErrors.push(
        { id: nextId(d.importErrors), batchId: batch.id, rowNo: 3, columnName: '物料编码', message: '物料编码不能为空', rawData: ',,原材料,星曜自有' },
        { id: nextId(d.importErrors), batchId: batch.id, rowNo: 6, columnName: '分类', message: '分类不存在：五金件', rawData: 'SKU-9001,垫片,五金件,星曜自有' }
      )
    }
    save()
    return ok({ batchId: batch.id, total, success, fail: failRows, status: batch.status })
  }

  /* ---------- 图纸识别 ---------- */
  if (path === '/drawing/tasks') return ok(pageOf([...d.drawingTasks].sort((a, b) => b.id - a.id), params))
  if (path === '/drawing/batch' && m === 'post') {
    const files = body?.getAll ? body.getAll('files') : []
    const taskId = nextId(d.drawingTasks)
    const task = {
      id: taskId,
      taskNo: 'DRW' + Date.now().toString().slice(-11),
      fileCount: files.length,
      successCount: files.length,
      failCount: 0,
      status: 'DONE',
      createUser: currentUser()?.username || 'admin',
      createTime: now()
    }
    d.drawingTasks.push(task)
    const partNos = ['FL-100', 'BZ-220', 'JG-310', 'ZC-505']
    files.forEach((f, i) => {
      d.drawingFiles.push({
        id: nextId(d.drawingFiles),
        taskId,
        fileName: f.name,
        fileSize: f.size || 102400,
        status: 'SUCCESS',
        recognizeResult: `件号：${partNos[i % 4]}；材质：45#；规格：DN${100 + i * 10}；数量：1`
      })
    })
    save()
    return ok(task)
  }
  if (/^\/drawing\/tasks\/\d+\/files$/.test(path)) {
    const id = Number(path.split('/')[3])
    return ok(d.drawingFiles.filter((f) => f.taskId === id))
  }
  if (/^\/drawing\/files\/\d+\/retry$/.test(path)) {
    const id = Number(path.split('/')[3])
    const f = d.drawingFiles.find((x) => x.id === id)
    if (f) {
      f.status = 'SUCCESS'
      f.recognizeResult = '件号：RT-001；材质：45#；规格：DN100；数量：1'
    }
    save()
    return ok(null)
  }

  return fail('演示环境未实现的接口：' + method.toUpperCase() + ' ' + path, 404)
}

/* 供页面直接调用的演示辅助 */
export function downloadDemoTemplate(bizType) {
  const headers = {
    WMS_ITEM: '物料编码,物料名称,分类,品牌,规格,单位',
    MES_WORKSTATION: '工作站编码,工作站名称,车间',
    MES_ROUTE_CATEGORY: '分类编码,分类名称,排序',
    ERP_PRODUCT: '产品编码,产品名称,单位,分类'
  }
  const sample = {
    WMS_ITEM: 'SKU-9001,示例物料,原材料,星曜自有,Φ50,件',
    MES_WORKSTATION: 'ST09,示例工作站,机加车间',
    MES_ROUTE_CATEGORY: 'RC09,示例工艺,9',
    ERP_PRODUCT: 'EP-9001,示例产品,件,机加工成品'
  }
  const csv = '\ufeff' + (headers[bizType] || '编码,名称') + '\n' + (sample[bizType] || '')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = bizType + '_模板.csv'
  a.click()
  URL.revokeObjectURL(url)
}

export function demoAdapter(config) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const method = (config.method || 'get').toLowerCase()
      const path = String(config.url || '').replace(/^\/api/, '').split('?')[0]
      let body = config.data
      if (typeof body === 'string') {
        try {
          body = JSON.parse(body)
        } catch (e) {
          body = {}
        }
      }
      let result
      try {
        result = mockResult(method, path, config.params || {}, body || {})
      } catch (e) {
        result = fail('演示数据异常：' + e.message, 500)
      }
      resolve({
        data: result,
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
        request: {}
      })
    }, 120)
  })
}
