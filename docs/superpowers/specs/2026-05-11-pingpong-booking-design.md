# 乒乓球场馆预约系统 - 设计文档

## 概述
Spring Boot + RabbitMQ + Redis + MySQL 实现的乒乓球台预约后端。

## 核心实体
- **Venue（球台）**: id, name, location, status
- **Booking（预约）**: id, venue_id, user_name, booking_date, start_time, end_time, status(PENDING/CONFIRMED/CANCELLED), created_at

## API
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/venues | 球台列表 |
| GET | /api/venues/{id}/slots?date=xxx | 某球台某日可预约时段 |
| POST | /api/bookings | 创建预约(PENDING) |
| GET | /api/bookings | 用户预约列表 |
| PUT | /api/bookings/{id}/confirm | 确认预约 |
| PUT | /api/bookings/{id}/cancel | 取消预约 |

## Redis
- 分布式锁防超卖：创建预约时对 venue_id+date+time_slot 加锁

## RabbitMQ
- 延迟队列：创建预约15分钟后若未确认，自动取消释放时段
