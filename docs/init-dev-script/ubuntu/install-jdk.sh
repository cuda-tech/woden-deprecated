#!/usr/bin/env bash

DEV_PATH=~/dev
mkdir -p ${DEV_PATH}
cd ${DEV_PATH}

if [[ ! -d "$DEV_PATH/java" ]];then
    if [[ ! -d "$DEV_PATH/jdk1.8.0_151" ]];then
        if [[ ! -f "$DEV_PATH/jdk-8u151-linux-x64.tar.gz" ]];then
            axel -n8 https://mirrors.huaweicloud.com/java/jdk/8u151-b12/jdk-8u151-linux-x64.tar.gz
        fi
        tar -xzvf jdk-8u151-linux-x64.tar.gz
    fi
    ln -s jdk1.8.0_151 jdk
fi