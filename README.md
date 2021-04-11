# Woden: Workshop of Data Enhancement
一款集成 ETL 开发、元数据管理、任务调度、数据质量监控、算法建模的数据平台

## 目录
* [项目背景](#项目背景)
* [安装指南](#安装指南)
* [使用指南](#使用指南)
* [开发指南](#开发指南)
* [变更日志](#变更日志)
* [贡献者](#贡献者)

## 项目背景
todo

## 安装指南
### 系统依赖
* hadoop 3.3.0
* spark 3.0.0
* java8

### docker 安装

### 手动安装


### 源码安装
todo

## 使用指南
todo

## 开发指南

### 技术选型
* 后端：Spring(Kotlin) + MySQL
* 前端：VUE
* 构建工具：gradle

### 代码结构
```
.
├── annotation-processor    # 注解处理器
├── frontend                # 前端工程
├── scheduler               # 调度模块
├── service                 # 数据服务层模块
└── webserver               # 管理系统
```

注解处理器模块的开发请参考该模块下的[文档](annotation-processor/README.md)

前端工程模块的开发请参考该模块下的[文档](frontend/README.md)

调度模块的开发请参考该模块下的[文档](scheduler/README.md)

管理系统的开发请参考该模块下的[文档](webserver/README.md)

## 变更日志
todo



## 贡献者
[@Jensen Qi](https://github.com/JensenQi)

## 鸣谢
感谢 [JetBrains 社区支持团队](https://www.jetbrains.com/opensource/) 为本项目提供了 All Products Pack License.

## License
[Apache2.0](http://www.apache.org/licenses/LICENSE-2.0)
