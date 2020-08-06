#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

echo "下载 hadoop"
if [[ ! -f "$DOWNLOAD_PATH/hadoop-3.3.0.tar.gz" ]];then
    axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.3.0/hadoop-3.3.0.tar.gz \
        --output=${DOWNLOAD_PATH}/hadoop-3.3.0.tar.gz
fi

echo "下载 hive"
if [[ ! -f "$DOWNLOAD_PATH/apache-hive-3.1.2-bin.tar.gz" ]];then
    axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hive/hive-3.1.2/apache-hive-3.1.2-bin.tar.gz \
        --output=${DOWNLOAD_PATH}/apache-hive-3.1.2-bin.tar.gz
fi
