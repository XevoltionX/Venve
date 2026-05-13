#!/bin/bash
# 将配置导入 Nacos（需先启动 Nacos 并登录控制台获取 token）
# 或者直接在 Nacos 控制台 http://localhost:8848/nacos 手动创建：
#
# 1. 配置管理 → 配置列表 → 新建
# 2. 依次创建以下配置（DEFAULT_GROUP，格式 YAML）：

NACOS="http://localhost:8848"

# 登录获取 token（默认用户名密码 nacos/nacos）
TOKEN=$(curl -s -X POST "$NACOS/nacos/v1/auth/login" -d "username=nacos&password=nacos" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "无法登录 Nacos，请检查 Nacos 是否启动，用户名密码是否为 nacos/nacos"
    exit 1
fi

import_config() {
    local data_id=$1
    local file=$2
    local content=$(cat "$file" | sed 's/&/\&/g' | sed 's/=/\\=/g')

    curl -s -X POST "$NACOS/nacos/v1/cs/configs" \
        -d "tenant=public" \
        -d "dataId=$data_id" \
        -d "group=DEFAULT_GROUP" \
        -d "type=yaml" \
        -d "content=$content" \
        -H "Authorization: Bearer $TOKEN"
    echo "  → imported $data_id"
}

echo "Importing configs to Nacos..."
import_config "common-config.yaml" "common-config.yaml"
import_config "auth-service.yaml" "auth-service.yaml"
import_config "venue-service.yaml" "venue-service.yaml"
import_config "booking-service.yaml" "booking-service.yaml"
import_config "gateway-service.yaml" "gateway-service.yaml"
echo "Done."
