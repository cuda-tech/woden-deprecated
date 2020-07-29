#!/usr/bin/env bash

DEV_PATH=~/dev
mkdir -p ${DEV_PATH}
cd ${DEV_PATH}

if [[ ! -d "$DEV_PATH/gradle" ]];then
    if [[ ! -d "$DEV_PATH/gradle-6.3" ]];then
        if [[ ! -f "$DEV_PATH/gradle-6.3-all.zip" ]];then
            axel -n8 https://services.gradle.org/distributions/gradle-6.3-all.zip
        fi
        unzip gradle-6.3-all.zip
    fi
    ln -s gradle-6.3 gradle
fi