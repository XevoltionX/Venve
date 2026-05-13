#!/bin/bash
NACOS="http://localhost:8848"
USER="nacos"
PASS="nacos"

echo "=== 登录 Nacos 获取 Token ==="
RESP=$(curl -s -X POST "$NACOS/nacos/v1/auth/login" \
  -d "username=$USER" \
  -d "password=$PASS")

TOKEN=$(echo "$RESP" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')

if [ -z "$TOKEN" ]; then
  echo "登录失败！响应: $RESP"
  echo "请确认 Nacos 用户名密码是 nacos/nacos"
  exit 1
fi
echo "Token 获取成功"

import_config() {
  local data_id=$1
  local content=$2

  echo "--- 导入 $data_id ---"
  RESULT=$(curl -s -X POST "$NACOS/nacos/v1/cs/configs" \
    -H "Authorization: Bearer $TOKEN" \
    --data-urlencode "dataId=$data_id" \
    -d "group=DEFAULT_GROUP" \
    -d "type=yaml" \
    --data-urlencode "content=$content")

  if echo "$RESULT" | grep -q "true\|success"; then
    echo "  ✓ $data_id 导入成功"
  else
    echo "  ✗ $data_id 可能已存在: $RESULT"
  fi
}

import_config "common-config.yaml" \
"spring:
  datasource:
    url: jdbc:mysql://localhost:3306/venve?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 111111
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848"

import_config "auth-service.yaml" \
"server:
  port: 8081"

import_config "venue-service.yaml" \
"server:
  port: 8082"

import_config "booking-service.yaml" \
"server:
  port: 8083

booking:
  pending-timeout-minutes: 15"

import_config "gateway-service.yaml" \
"server:
  port: 8084"

echo ""
echo "=== 完成 ==="
echo "刷新 Nacos 控制台 http://localhost:8848/nacos → 配置管理 → 配置列表"
