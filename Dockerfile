FROM centos
MAINTAINER JensenQi jinxiu.qi@alu.hit.edu.cn
RUN mkdir -p ~/dev \
    && cd ~/dev \
    && wget -c https://mirrors.huaweicloud.com/java/jdk/8u151-b12/jdk-8u151-linux-x64.tar.gz \
    && tar -xzvf jdk-8u151-linux-x64.tar.gz \
    && ln -s jdk1.8.0_151 jdk
