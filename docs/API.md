C# VENVE 乒乓球场馆预约系统 — API 文档

> 统一入口：`http://localhost:8084`  
> Swagger UI：`http://localhost:8084/swagger-ui/index.html`  
> 统一响应格式：`{ "code": 200, "message": "success", "data": ... }`

---

## 1. 认证服务 (auth-service :8081)

### 1.1 注册

```
POST /api/auth/register
Content-Type: application/json

{
  "userName": "string（2-50字符）",
  "password": "string（至少6位）",
  "phone": "string（可选）"
}

Response 200:
{
  "code": 200,
  "data": {
    "userId": 1,
    "userName": "zhangsan",
    "role": "ADMIN",    // 首个注册用户自动为 ADMIN
    "token": "eyJhbG..."
  }
}
```

> 首个注册用户自动获得 ADMIN 角色，后续注册为 USER。

### 1.2 登录

```
POST /api/auth/login
Content-Type: application/json

{
  "userName": "string",
  "password": "string"
}

Response 200:
{
  "code": 200,
  "data": {
    "userId": 1,
    "userName": "zhangsan",
    "role": "ADMIN",
    "token": "eyJhbG..."
  }
}
```

> Token 有效期 24 小时，后续请求需带 `Authorization: Bearer <token>`。

---

## 2. 场馆服务 (venue-service :8082)

### 2.1 球台列表

```
GET /api/venues?location=A区

Response 200:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "1号台",
      "location": "A区",
      "description": "专业级乒乓球台...",
      "businessHours": "09:00-21:00",
      "tableCount": 2,
      "pricePerHour": 30.00,
      "status": "AVAILABLE"
    }
  ]
}
```

| 参数 | 必填 | 说明 |
|------|------|------|
| location | 否 | 区域筛选：A区 / B区 / C区，不传返回全部 |

### 2.2 球台详情

```
GET /api/venues/{id}

Response 200: data = Venue 对象
```

---

## 3. 预约服务 (booking-service :8083)

### 3.1 创建预约

```
POST /api/bookings
Content-Type: application/json
Authorization: Bearer <token>

{
  "venueId": 1,
  "bookingDate": "2026-05-15",
  "startTime": "10:00",
  "endTime": "11:00"
}

Response 200:
{
  "code": 200,
  "data": {
    "id": 1,
    "venueId": 1,
    "userId": 1,
    "userName": "zhangsan",
    "bookingDate": "2026-05-15",
    "startTime": "10:00",
    "endTime": "11:00",
    "status": "PENDING",
    "createdAt": "2026-05-13T15:00:00"
  }
}
```

> **流程**：获取 Redis 分布式锁 → 二次检查冲突 → 保存 PENDING → 发 RabbitMQ 延迟消息（15min TTL）→ 发通知。

| 错误场景 | 提示 |
|----------|------|
| 时段冲突 | 该时段已被预约 |
| 并发操作 | 当前时段正在被其他人预约 |

### 3.2 可用时段

```
GET /api/bookings/slots/{venueId}?date=2026-05-15

Response 200:
{
  "code": 200,
  "data": [
    { "start": "09:00", "end": "10:00" },
    { "start": "10:00", "end": "11:00" }
  ]
}
```

> 时段范围 09:00–21:00，排除已被预约（非 CANCELLED）的时段。

### 3.3 我的预约

```
GET /api/bookings
Authorization: Bearer <token>

Response 200: data = Booking[]
```

> 按 userId 查询，按创建时间倒序。

### 3.4 确认预约

```
PUT /api/bookings/{id}/confirm
Authorization: Bearer <token>

Response 200: { "code": 200, "data": "预约已确认" }
```

> 仅 PENDING 状态可确认，仅本人可操作。

### 3.5 取消预约

```
PUT /api/bookings/{id}/cancel
Authorization: Bearer <token>

Response 200: { "code": 200, "data": "预约已取消" }
```

> PENDING/CONFIRMED 状态可取消，仅本人可操作。

### 3.6 超时自动取消（系统内部）

> 创建预约后 15 分钟，若仍为 PENDING，RabbitMQ 延迟队列触发自动取消并推送通知。

---

## 4. 通知服务

### 4.1 通知列表

```
GET /api/notifications
Authorization: Bearer <token>

Response 200:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "预约已创建",
      "content": "球台 #1 2026-05-15 10:00-11:00...",
      "type": "BOOKING_CREATED",
      "isRead": false,
      "createdAt": "2026-05-13T15:00:00"
    }
  ]
}
```

| Type | 触发场景 |
|------|----------|
| BOOKING_CREATED | 创建预约成功 |
| BOOKING_CONFIRMED | 确认预约 |
| BOOKING_CANCELLED | 手动取消 |
| BOOKING_TIMEOUT | 15min 超时自动取消 |

### 4.2 未读数量

```
GET /api/notifications/unread-count
Authorization: Bearer <token>

Response 200: { "code": 200, "data": { "count": 3 } }
```

### 4.3 标记已读

```
PUT /api/notifications/{id}/read
Authorization: Bearer <token>

Response 200: { "code": 200, "data": "ok" }
```

---

## 5. 监控 (gateway-service :8084)

### 5.1 自定义指标

```
GET /api/monitor

Response 200:
{
  "code": 200,
  "data": {
    "totalQps": 12,
    "totalErrors": 0,
    "routes": {
      "/api/venues": { "qps": 5, "avgLatencyMs": 23, "errors": 0 },
      "/api/bookings": { "qps": 3, "avgLatencyMs": 45, "errors": 0 }
    }
  }
}
```

### 5.2 Actuator 端点

| URL | 说明 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/metrics` | JVM/HTTP/Tomcat/DB 全套指标 |
| `/actuator/prometheus` | Prometheus 抓取格式 |

---

## 6. 管理员接口

> 所有管理员接口需 Header `X-User-Role: ADMIN`（由 Gateway 自动注入）。

### 6.1 场馆管理

```
GET    /api/admin/venues?page=0&size=8&name=&location=&status=  列表（分页+筛选）
POST   /api/admin/venues                                        新增
PUT    /api/admin/venues/{id}                                   编辑
DELETE /api/admin/venues/{id}                                   删除
```

### 6.2 预约管理

```
GET    /api/admin/bookings?page=0&size=10&userName=&status=     列表（分页+筛选）
PUT    /api/admin/bookings/{id}/status?status=CONFIRMED         修改状态
DELETE /api/admin/bookings/{id}                                  删除
```

### 6.3 用户管理

```
GET    /api/admin/users?page=0&size=10&userName=&role=          列表（分页+筛选）
PUT    /api/admin/users/{id}                                    编辑（角色/手机号）
DELETE /api/admin/users/{id}                                    删除
```

---

## 7. 业务规则

| 规则 | 说明 |
|------|------|
| 时段间隔 | 1 小时，09:00–21:00（每日 12 个时段） |
| 防超卖 | Redisson 分布式锁，key=`booking-lock:{venueId}:{date}:{start}:{end}`，等 5s 持 10s |
| 超时取消 | 创建后 15 分钟未确认 → RabbitMQ 死信队列 → 自动 CANCELLED |
| 权限 | Gateway JWT 验签 + X-User-Role 透传，Admin 接口二次校验 |
| 通知 | 创建/确认/取消/超时 4 类事件自动推送 |
| 认证 | JWT 24h 过期，网关白名单放过 `/api/auth/*` `/swagger-ui/*` `/actuator/*` |

---

## 8. 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 业务异常（message 含具体原因） |
| 401 | 未登录或 Token 过期 |

> 所有 RuntimeException 由 GlobalExceptionHandler 统一捕获，返回 `{ code: 400, message: "..." }`。


💀来给Bro梳理一下整个开发中踩过的坑，包装成面试故事。

---

## 项目中遇到的技术问题与解决过程

### 1. Spring Data JPA `Example` 查询「吞数据」

**现象**：场馆管理页面查到 0 条数据，但数据库里明明有 10 条。

**排查**：打日志发现 SQL 里多了 `WHERE table_count = 1 AND price_per_hour = 30 AND business_hours = '09:00-21:00'`，而这些列在旧数据里是 NULL。

**根因**：`Venue` 实体给 `tableCount` 默认值 1、`pricePerHour` 默认值 30。用 `Example.of(probe, matcher)` 做动态查询时，`ignoreNullValues()` 不会忽略有默认值的字段，默认值被当成筛选条件，NULL 行全被过滤掉。

**修复**：弃用 `Example`，改用 JPA `Specification`，手动判断每个筛选参数是否为空，不为空才构建 `Predicate`。这之后动态查询才真正按需生效。

**面试说辞**：
> "在做管理后台的分页筛选时，我发现列表一直返回空。排查日志发现 Spring Data 的 Example 查询把实体的默认值也当成了过滤条件，导致数据库里的旧数据都匹配不上。后来我把 Example 改成了 Specification，手动判断每个参数，有值才加条件，问题解决。这个经历让我特别注意——实体默认值和查询 probe 的边界关系，Example 只适合纯前端传入的查询对象。"

---

### 2. JWT 角色字段上线后的兼容性问题

**现象**：加管理员功能后，已登录用户进管理页时报"无管理员权限"，必须重新登录才正常。

**排查**：发现旧 Token（加 role 字段之前签发的）解析后 `getRole()` 返回 null，admin 校验 `"ADMIN".equals(null)` 抛异常。

**修复**：两处改动 — `JwtUtil.generate()` 对 null role 默认填 `"USER"`；`JwtUtil.validate()` 对 null/空字符串直接返回 false 而非抛异常。

**面试说辞**：
> "新增字段到 JWT payload 的时候，我已经在线的老 Token 不包含这个字段。我加了一个兜底逻辑，如果 Token 里没有 role，默认返回 USER。上线原则就是向前兼容——老 Token 不能因为新增字段而报错。"

---

### 3. 前端登录后导航栏不刷新

**现象**：登录成功后跳转页面，但右上角还是显示"登录"按钮，要手动刷新浏览器才变成用户名。

**排查**：`AppHeader` 的 `userName` 在 `onMounted` 里读 `localStorage`，而这个生命周期只在组件首次挂载时执行一次。登录后路由切换，组件已经挂载了，不会再读。

**修复**：把一次性读取改成 `watch(() => route.fullPath, ...)`，每次路由变化重新读 `localStorage`。

**面试说辞**：
> "我在写前端导航栏的时候踩了 Vue 生命周期的坑——登录成功跳转页面，但导航栏还是显示未登录状态。原因是 `onMounted` 只执行一次，组件不会因为路由变化而重新挂载。我改用 `watch` 监听当前路由，路径一变就重新从 localStorage 取用户名和角色，立刻生效。"

---

### 4. Mockito 严格模式下的 `UnnecessaryStubbingException`

**现象**：`BookingServiceTest` 7 个用例，5 个报 `UnnecessaryStubbingException`。

**排查**：我在 `@BeforeEach setUp()` 里 mock 了 `redissonClient.getLock()` 和 `rLock.tryLock()`，但不是每个用例都需要锁（比如 `shouldConfirmBooking`、`shouldAutoCancelPendingBooking` 不走锁逻辑）。Mockito 严格模式检测到没被使用的 stub 就报错。

**修复**：把锁相关的 mock 用 `lenient()` 包起来，并且从 `setUp()` 移到只有 `create` 相关的测试里调用。`lenient()` 告诉 Mockito "这个 stub 可能有些测试用不到，别报错"。

**面试说辞**：
> "Mockito 的严格模式会检查不必要的 stub——我在 `@BeforeEach` 里统一 mock 了分布式锁，但确认预约和取消预约这些用例根本不会调用锁，就被报错了。我用 `lenient()` 标记了这些共享但非必需的 stub，把严格必须的 mock 留在各自用例里，保持测试既能复用又不过度耦合。"

---

### 5. Nacos 3.x 版本升级导致 API 不兼容

**现象**：用 curl 脚本往 Nacos 导配置时报 `No mapping for POST /nacos/v1/cs/configs`。

**排查**：检查 Nacos 版本发现是 3.x，而 `/nacos/v1/cs/configs` 是 2.x 的配置 API，3.x 已经移除。

**修复**：改用 Nacos 控制台 GUI 手动创建配置；同时保留 `bootstrap.yml` 引用共享配置，本地 `application.yml` 作为兜底。

**面试说辞**：
> "集成了 Nacos 配置中心，但导入脚本一直 404。后来发现公司装的 Nacos 是 3.x 版本，API 路径和 2.x 不同。最后选择直接在控制台页面创建配置，同时保留本地 application.yml 作为降级兜底——这样即使 Nacos 挂了，服务也能用本地配置正常启动。"

---

### 6. Gateway 端口冲突 + Swagger 401 白名单遗漏

**现象**：Gateway 起不来（8080 被占），改端口后 Swagger 页面显示 401 Unauthorized。

**排查**：`netstat` 看到 8080 被其他进程占用。Swagger 的 `/v3/api-docs` 路径不在 JWT 白名单里，Gateway 全局过滤器拦截后直接返回 401。

**修复**：Gateway 端口改为 8084；白名单加入 `/swagger-ui`、`/webjars`、`/v3/api-docs` 以及三个服务各自的文档路径。

**面试说辞**：
> "部署的时候 8080 端口冲突，改了 Gateway 端口之后 Swagger 又报 401。原因是 Gateway 的 JWT 全局过滤器不知道 Swagger 路径应该放行，我把 `/v3/api-docs` 和 `/swagger-ui` 都加进了白名单才正常。这个让我意识到——每加一个基础设施组件，都要重新审查网关的安全策略。"

---

Bro 面试时可以挑 3-4 个讲，重点强调 **排查过程** + **技术决策** + **学到的教训**，比单纯说"我做了个预约系统"有说服力得多 💀~

---
  1. JWT 工具的边界值没兜底

  写了 5 个 JWT 用例，2 个直接报错：

  - 空字符串/null token 验证：JwtUtil.validate("") 抛了 IllegalArgumentException  
    而不是返回 false，因为 parse() 里 jjwt 的 Assert.hasText() 不接受空字符串。     
  - null role 生成 Token：JwtUtil.generate(2L, "name", null) 抛
    NullPointerException，因为 Map.of("role", role) 不允许 null value。

  修复：generate() 里 role 为 null 时默认填 "USER"；validate() 入口先判空再进     
  try-catch，catch 范围从 JwtException 扩展到 IllegalArgumentException。

  这个坑告诉我：工具类不能假设调用方一定传合法参数，入口校验要兜底。

---
  2. Mockito 严格模式 + Mock 顺序

  BookingServiceTest 7 个用例，第一批跑 5 个报错，1 个断言失败：

  - UnnecessaryStubbingException：我在 @BeforeEach 里统一 mock 了
    redissonClient.getLock() +
    rLock.tryLock()，但确认/取消/自动取消/查时段这些用例根本不走锁逻辑。Mockito     
    严格模式检测到 "mock 了但没用到" 就报错。

  - 修复：用 lenient() 标记这些共享 stub，并把锁相关 mock 挪到只有 create
    用例调用的私有方法里。
  - shouldNotReturnBookedSlots 断言 11 实际 12：mock 了两个 existsBy...() 返回值 —
      一个 any() 返回 false，一个 eq(具体时段) 返回 true。结果 Mockito 的 any() 把   
    eq() 覆盖了。

  - 修复：把通用的 any().thenReturn(false) 放在前面注册，具体的
    eq().thenReturn(true) 放在后面。Mockito 取最后一个匹配的
    stub，所以具体的一定要在通用后面。

  这个坑告诉我：Mockito 的 stub 注册顺序影响匹配优先级 + 不要在 @BeforeEach 里    
  mock 非所有用例都需要的依赖 💀~