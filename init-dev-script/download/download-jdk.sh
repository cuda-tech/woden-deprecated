#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

echo "下载 Linux JDK..."
if [[ ! -f "$DOWNLOAD_PATH/jdk-8u202-linux-x64.tar.gz" ]];then
    axel -n8 https://mirrors.huaweicloud.com/java/jdk/8u202-b08/jdk-8u202-linux-x64.tar.gz \
        --output=${DOWNLOAD_PATH}/jdk-8u202-linux-x64.tar.gz
fi

if grep -qEi "(Microsoft|WSL)" /proc/version &> /dev/null ; then
    echo "下载 Windows JDK"
    if [[ ! -f "$DOWNLOAD_PATH/jdk-8u202-windows-x64.exe" ]];then
        axel -n8 https://mirrors.huaweicloud.com/java/jdk/8u202-b08/jdk-8u202-windows-x64.exe \
            -o ${DOWNLOAD_PATH}/jdk-8u202-windows-x64.exe
    fi
fi