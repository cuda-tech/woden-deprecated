#!/usr/bin/env bash

DEV_PATH=~/dev
mkdir -p ${DEV_PATH}
cd ${DEV_PATH}

# hadoop
if [[ ! -d "$DEV_PATH/hadoop" ]];then
    if [[ ! -d "$DEV_PATH/hadoop-2.7.2" ]];then
        if [[ ! -f "$DEV_PATH/hadoop-3.3.0.tar.gz" ]];then
            axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.3.0/hadoop-3.3.0.tar.gz
        fi
        tar -xzvf hadoop-3.3.0.tar.gz
    fi
    ln -s hadoop-3.3.0 hadoop
fi

# hive
if [[ ! -d "$DEV_PATH/hive" ]];then
    if [[ ! -d "$DEV_PATH/apache-hive-3.1.2-bin" ]];then
        if [[ ! -f "$DEV_PATH/apache-hive-3.1.2-bin.tar.gz" ]];then
            wget -c https://mirrors.tuna.tsinghua.edu.cn/apache/hive/hive-3.1.2/apache-hive-3.1.2-bin.tar.gz
        fi
        tar -xzvf apache-hive-3.1.2-bin.tar.gz
    fi
    ln -s apache-hive-3.1.2-bin hive
fi




