#!/usr/bin/env bash

DEV_PATH=~/dev
mkdir -p ${DEV_PATH}
cd ${DEV_PATH}

if [[ ! -d "$DEV_PATH/node" ]];then
    if [[ ! -d "$DEV_PATH/node-v8.14.0-linux-x64" ]];then
        if [[ ! -f "$DEV_PATH/node-v8.14.0-linux-x64.tar.gz" ]];then
            axel -n8 https://npm.taobao.org/mirrors/node/v8.14.0/node-v8.14.0-linux-x64.tar.gz
        fi
        tar -xzvf node-v8.14.0-linux-x64.tar.gz
    fi
    ln -s node-v8.14.0-linux-x64 node
fi