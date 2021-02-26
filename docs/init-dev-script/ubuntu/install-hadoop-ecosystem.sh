#!/usr/bin/env bash
BASEDIR=$(dirname $(realpath "$0"))

DEV_PATH=$1
echo "安装路径：$DEV_PATH"
mkdir -p ${DEV_PATH}

echo "安装 hadoop"
if [[ ! -d "$DEV_PATH/hadoop" ]];then
    if [[ ! -d "$DEV_PATH/hadoop-3.3.0" ]];then
        if [[ ! -f "$DEV_PATH/hadoop-3.3.0.tar.gz" ]];then
            axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.3.0/hadoop-3.3.0.tar.gz \
                --output=${DEV_PATH}/hadoop-3.3.0.tar.gz
        fi
        tar -xzvf ${DEV_PATH}/hadoop-3.3.0.tar.gz
    fi
    ln -s ${DEV_PATH}/hadoop-3.3.0 ${DEV_PATH}/hadoop
fi

echo "安装 hive"
if [[ ! -d "$DEV_PATH/hive" ]];then
    if [[ ! -d "$DEV_PATH/apache-hive-3.1.2-bin" ]];then
        if [[ ! -f "$DEV_PATH/apache-hive-3.1.2-bin.tar.gz" ]];then
            axel -n8 https://mirrors.tuna.tsinghua.edu.cn/apache/hive/hive-3.1.2/apache-hive-3.1.2-bin.tar.gz \
                --output=${DEV_PATH}/apache-hive-3.1.2-bin.tar.gz
        fi
        tar -xzvf ${DEV_PATH}/apache-hive-3.1.2-bin.tar.gz
    fi
    ln -s ${DEV_PATH}/apache-hive-3.1.2-bin ${DEV_PATH}/hive
fi

echo "解决 hive 和 hadoop 的 guava 冲突"
if [[ -f "$DEV_PATH/hive/lib/guava-19.0.jar" ]];then
    rm ${DEV_PATH}/hive/lib/guava-19.0.jar
    cp ${DEV_PATH}/hadoop/share/hadoop/common/lib/guava-27.0-jre.jar ${DEV_PATH}/hive/lib/
fi

echo "配置 hive 的 MetaStore 为 MySQL"
if [[ ! -f "$DEV_PATH/hive/conf/hive-site.xml" ]];then
    axel http://maven.aliyun.com/nexus/content/groups/public/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar \
        --output=${DEV_PATH}/hive/lib/mysql-connector-java-5.1.49.jar
    ln -s ${BASEDIR}/config/hive-site.xml ${DEV_PATH}/hive/conf/hive-site.xml
    ${DEV_PATH}/hive/bin/schematool -dbType mysql -initSchema
fi
