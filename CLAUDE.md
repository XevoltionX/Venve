# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

乒乓球场馆预约系统 — Spring Cloud 微服务架构，Spring Boot 3.3.5（Java 21）+ Vue 3 前端。Redis 分布式锁防超卖，RabbitMQ 延迟队列实现超时自动取消，JWT 无状态认证。

## 构建与运行

### 依赖环境

- MySQL（localhost:3306，`venve` 数据库，`ddl-auto: update` 自动建表）
- Redis（localhost:6379）
- RabbitMQ（localhost:5672，guest/guest）
- Nacos（localhost:8848，standalone 模式）

### Maven 多模块编译

```bash
mvn clean compile            # 全量编译
mvn clean package -DskipTests # 打包
mvn test                      # 运行测试（21 个，覆盖 common/auth/venue/booking）
```

### 按顺序启动服务

| 模块 | 端口 | 命令 |
|------|------|------|
| gateway-service | 8084 | `mvn -pl gateway-service spring-boot:run` |
| auth-service | 8081 | `mvn -pl auth-service spring-boot:run` |
| venue-service | 8082 | `mvn -pl venue-service spring-boot:run` |
| booking-service | 8083 | `mvn -pl booking-service spring-boot:run` |

也可在 IDE 中分别运行各 `*Application.java` 主类。

### 前端（Vue 3）

```bash
cd frontend
npm install                  # 首次运行
npm run dev                  # :3000，代理 /api → :8084
npm run build                # 生产构建
```

## 架构

### 微服务模块

```
venve-cloud/
├── common/              # 公共：JwtUtil、ApiResponse、AuthConstant、GlobalExceptionHandler
├── gateway-service/     # Spring Cloud Gateway + JWT 全局过滤器 + MetricsFilter
├── auth-service/        # 认证服务：注册、登录、JWT 签发（BCrypt 加密）
├── venue-service/       # 场馆服务：球台列表、场馆查询、区域筛选
├── booking-service/     # 预约服务：创建/确认/取消 + Redis 锁 + RabbitMQ 延迟 + 通知
└── frontend/            # Vue 3 前端
```

- **Gateway** 是唯一入口，校验 JWT Token 后将 `X-User-Id` / `X-User-Name` / `X-User-Role` 透传到下游。
- **白名单**：`/api/auth/login`、`/api/auth/register`、`/swagger-ui/**`、`/webjars/**`、`/v3/api-docs/**`、`/actuator/**`、`/api/monitor`。
- **服务间调用**：booking-service 通过 OpenFeign 调用 venue-service 获取场馆信息。
- **所有服务** 注册到 Nacos（`spring.cloud.nacos.discovery`）。
- **Nacos 配置中心**：各服务通过 `bootstrap.yml` 加载共享 `common-config.yaml`。

### 后端分层（每个服务内部）

```
controller/  →  service/  →  repository/  →  entity/
     ↓              ↓
   dto/          config/
```

- **Controller** 薄层，委托给 Service，返回 `ApiResponse<T>` 统一响应体。
- **Service** 全部业务逻辑，构造注入。
- **GlobalExceptionHandler**（在 common 模块）捕获 `RuntimeException`，返回 `ApiResponse.fail(400, msg)`。
- `@SpringBootApplication(scanBasePackages = "com.cs")` 确保扫描到 common 模块的组件。

### 领域模型

- **User**（`sys_user`）：userName（唯一）、password（BCrypt）、phone、role（USER/ADMIN）
- **Venue**（`venue`）：name、location、description、businessHours、tableCount、pricePerHour、status（AVAILABLE / MAINTENANCE）
- **Booking**（`booking`）：venueId、userId、userName、bookingDate、startTime、endTime、status（PENDING → CONFIRMED 或 CANCELLED）
- **Notification**（`notification`）：userId、title、content、type（BOOKING_CREATED / CONFIRMED / CANCELLED / TIMEOUT）、isRead
- 时段为 1 小时间隔，09:00–21:00

### 认证流程

```
POST /api/auth/register  →  auth-service 签发 JWT  →  前端存 localStorage
POST /api/auth/login     →  auth-service 验证密码  →  返回 JWT + userName + role
    ↓
后续请求 → axios interceptor 自动带 Authorization: Bearer <token>
    ↓
Gateway AuthGlobalFilter 验签 → 放行 + 透传 X-User-Id / X-User-Name / X-User-Role
    ↓
booking-service 从 Header 读取 userId 做权限校验
```

- 首个注册用户自动获得 ADMIN 角色
- Admin 接口内部二次校验 `X-User-Role`

### Redis：分布式锁（防超卖）

创建预约时，`BookingService.create()` 获取 Redisson 锁，锁键 `booking-lock:{venueId}:{date}:{start}:{end}`，等待 5 秒、持有 10 秒。获取锁后二次检查无冲突预约才保存。

### RabbitMQ：延迟自动取消

TTL 队列 → 死信交换机 → 处理队列。

1. `RabbitMQConfig` 在 booking-service 中声明延迟队列，TTL 15 分钟。
2. 创建预约后，booking ID 通过 `booking.delay` 路由键发送到延迟队列。
3. 15 分钟后消息过期，死信转发到 `booking.process.queue`。
4. `BookingTimeoutConsumer` 接收消息，仅当仍为 PENDING 时自动取消并发送通知。

### 通知消息

预约创建/确认/取消/超时四种场景自动推送通知。用户可在前端消息铃铛查看未读数和消息列表。

### 系统监控

- **自定义指标**：Gateway 的 `MetricsFilter` 采集每个路由的 QPS、平均延迟、错误数，通过 `/api/monitor` 暴露。
- **Actuator**：`/actuator/health`、`/actuator/metrics`、`/actuator/prometheus`。
- 监控页仅管理员可见。

### 前端路由

| 路径 | 视图 | 认证 | 功能 |
|------|------|------|------|
| `/login` | `Login.vue` | 游客 | 登录 |
| `/register` | `Register.vue` | 游客 | 注册 |
| `/` | `Home.vue` | 需登录 | 首页（Hero + 统计 + 特性介绍） |
| `/venues` | `VenueList.vue` | 需登录 | 球台列表（按区域筛选） |
| `/book/:venueId` | `BookingCreate.vue` | 需登录 | 选日期→选时段→提交（日历选择器） |
| `/bookings` | `MyBookings.vue` | 需登录 | 自动加载预约，确认/取消 |
| `/notifications` | `Notifications.vue` | 需登录 | 消息中心，点击已读 |
| `/monitor` | `Monitor.vue` | 管理员 | QPS/延迟/错误数面板 |
| `/admin` | `AdminDashboard.vue` | 管理员 | 场馆/预约/用户 CRUD |

路由守卫：未登录 → 跳转 `/login`；已登录访问登录页 → 跳转首页；非管理员访问 `/admin` `/monitor` → 跳转首页。

### 测试

```bash
mvn test  # 21 个单元测试
```

| 模块 | 测试数 | 覆盖内容 |
|------|--------|----------|
| common | 5 | JWT 生成/验证/角色/边界 |
| auth-service | 4 | 注册/重复用户/登录/密码错误 |
| venue-service | 5 | 可用球台/区域筛选/空结果/按ID查/404 |
| booking-service | 7 | 创建+锁/冲突/确认/他人确认/自动取消/时段/已订时段 |

### API 文档

完整接口文档：`docs/API.md`（8 个章节，含请求响应示例和业务规则）。  
Swagger 聚合入口：`http://localhost:8084/swagger-ui/index.html`。

### 注意事项

当回复中的"你"改为"Bro"，"我"改为"💀"，并且在结尾加上"💀~"
实例："我接下来将跟你确认一下细节.." → "💀接下来将跟Bro确认一下细节..💀~"
