#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

echo "下载 Linux gradle"
if [[ ! -f "$DOWNLOAD_PATH/node-v8.14.0-linux-x64.tar.gz" ]];then
    axel -n8 https://npm.taobao.org/mirrors/node/v8.14.0/node-v8.14.0-linux-x64.tar.gz \
        --output=${DOWNLOAD_PATH}/node-v8.14.0-linux-x64.tar.gz
fi

if grep -qEi "(Microsoft|WSL)" /proc/version &> /dev/null ; then
    echo "下载 Windows gradle"
    if [[ ! -f "$DOWNLOAD_PATH/node-v8.14.0-x64.msi" ]];then
        axel -n8 https://npm.taobao.org/mirrors/node/v8.14.0/node-v8.14.0-x64.msi \
            --output=${DOWNLOAD_PATH}/node-v8.14.0-x64.msi
    fi
fi
