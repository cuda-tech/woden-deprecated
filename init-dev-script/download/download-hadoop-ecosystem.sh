#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

echo "下载 hadoop"
if [[ ! -f "$DOWNLOAD_PATH/hadoop-2.7.7.tar.gz" ]];then
    axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.3.0/hadoop-3.3.0.tar.gz \
        --output=${DOWNLOAD_PATH}/hadoop-2.7.7.tar.gz
fi

echo "下载 hive"
if [[ ! -f "$DOWNLOAD_PATH/apache-hive-2.3.7-bin.tar.gz" ]];then
    axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hive/hive-2.3.7/apache-hive-2.3.7-bin.tar.gz \
        --output=${DOWNLOAD_PATH}/apache-hive-2.3.7-bin.tar.gz
fi

echo "下载 Spark"
if [[ ! -f "$DOWNLOAD_PATH/spark-2.3.4-bin-hadoop2.7.tgz" ]];then
    axel -n8 https://mirrors.huaweicloud.com/apache/spark/spark-2.3.4/spark-2.3.4-bin-hadoop2.7.tgz \
        --output=${DOWNLOAD_PATH}/spark-2.3.4-bin-hadoop2.7.tgz
fi

echo "下载 Livy"
if [[ ! -f "$DOWNLOAD_PATH/apache-livy-0.7.0-incubating-bin.zip" ]];then
    axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/incubator/livy/0.7.0-incubating/apache-livy-0.7.0-incubating-bin.zip \
        --output=${DOWNLOAD_PATH}/apache-livy-0.7.0-incubating-bin.zip
fi
