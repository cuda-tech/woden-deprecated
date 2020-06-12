# 调度模块
## 代码结构
```
.
├── src
│   ├── main
│   │   ├── kotlin...service
│   │   │   ├── ops              # 调度 OP
│   │   │   ├── tracker          # 调度 tracker
│   │   │   ├── utils            # 工具类
│   │   │   └── SchedulerServer  # 主入口
│   │   └ resource               # 资源文件
│   └── test                     # 单元测试
├── build.gradle                 # gradle 配置文件
└── README.md
```

## 开发环境配置
请参考[环境配置指南](../readme/env-config.md#后端开发环境配置)