#!/usr/bin/env bash
set -e

DOWNLOAD_PATH=$1
mkdir -p ${DOWNLOAD_PATH}

bash ./download-gradle.sh ${DOWNLOAD_PATH}
bash ./download-hadoop-ecosystem.sh ${DOWNLOAD_PATH}
bash ./download-jdk.sh ${DOWNLOAD_PATH}
bash ./download-node.sh ${DOWNLOAD_PATH}
