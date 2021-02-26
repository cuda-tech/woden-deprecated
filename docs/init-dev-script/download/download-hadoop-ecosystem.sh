#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1

HADOOP_VERSION='3.2.2'
SPARK_VERSION='3.0.2'

echo "下载 hadoop..."
if [[ ! -f "$DOWNLOAD_PATH/hadoop-${HADOOP_VERSION}.tar.gz" ]];then
    axel -n8 https://mirrors.huaweicloud.com/apache/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz \
        --output=${DOWNLOAD_PATH}/hadoop-${HADOOP_VERSION}.tar.gz
else
    echo "$DOWNLOAD_PATH/hadoop-${HADOOP_VERSION}.tar.gz 已存在，跳过下载"
fi
echo "解压 hadoop..."
if [[ ! -d "$DOWNLOAD_PATH/hadoop-${HADOOP_VERSION}" ]];then
    tar -xzvf $DOWNLOAD_PATH/hadoop-${HADOOP_VERSION}.tar.gz -C $DOWNLOAD_PATH
else
    echo "$DOWNLOAD_PATH/hadoop-${HADOOP_VERSION} 已存在，跳过解压"
fi
ln -sf $DOWNLOAD_PATH/hadoop-${HADOOP_VERSION} $DOWNLOAD_PATH/hadoop
if [ -z $HADOOP_HOME ];then
  echo "export HADOOP_HOME=$DOWNLOAD_PATH/hadoop" >> ~/.bash_profile
  echo "export PATH=\$HADOOP_HOME/bin:\$PATH" >> ~/.bash_profile
fi


echo "下载 Spark..."
if [[ ! -f "$DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz" ]];then
    axel -n8 https://mirrors.huaweicloud.com/apache/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz \
        --output=${DOWNLOAD_PATH}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz
else
    echo "$DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz 已存在，跳过下载"
fi
echo "解压 Spark..."
if [[ ! -d "$DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}" ]];then
    tar -xzvf $DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz -C $DOWNLOAD_PATH
else
    echo "$DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*}.tgz 已存在，跳过解压"
fi
ln -sf $DOWNLOAD_PATH/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION%.*} $DOWNLOAD_PATH/spark
if [ -z $SPARK_HOME ];then
  echo "export SPARK_HOME=$DOWNLOAD_PATH/spark" >> ~/.bash_profile
  echo "export PATH=\$SPARK_HOME/bin:\$PATH" >> ~/.bash_profile
fi

echo "你需要在当前终端手动执行 source ~/.bash_profile 或重开终端以使 HADOOP_HOME, SPARK_HOME 等环境变量生效"
