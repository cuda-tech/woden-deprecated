#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

echo "下载 gradle"
if [[ ! -f "$DOWNLOAD_PATH/gradle-6.3-all.zip" ]];then
    axel -n8 https://mirrors.aliyun.com/macports/distfiles/gradle/gradle-6.3-all.zip \
        --output=${DOWNLOAD_PATH}/gradle-6.3-all.zip
fi
