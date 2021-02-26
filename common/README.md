# 数据服务层

## 代码结构
```
.
├── src
│   ├── main
│   │   ├── kotlin...service
│   │   │   ├── config           # 配置参数
│   │   │   ├── dao              # 数据操作对象
│   │   │   ├── dto              # 数据传输对象
│   │   │   ├── exception        # 自定义异常
│   │   │   ├── mysql            # MySQL 方言扩展
│   │   │   ├── po               # 持久对象
│   │   │   ├── utils            # 工具类
│   │   │   └── *.Service.kt     # 数据服务
│   │   └ resource               # 资源文件
│   └── test                     # 单元测试
├── build.gradle                 # gradle 配置文件
└── README.md
```

#### 设计规范
* 对外只透出 DTO 和 Service，以及数据类型；对于 PO、DAO 等不应透出，需要控制权限等级
* 读操作不应该返回异常，如果查找不到，则返回 null(对于对象) 或空(对于数组)
* 写操作不应返回 null，如果操作失败，应该返回异常，并在异常的 message 说明为何失败

## 开发环境配置
请参考[环境配置指南](../readme/env-config.md#后端开发环境配置)