# 管理系统

## 代码结构
```
.
├── src
│   ├── main
│   │   ├── kotlin...webserver
│   │   │   ├── auth             # JWT 权限
│   │   │   ├── config           # 配置参数
│   │   │   ├── controller       # controller
│   │   │   ├── converter        # 请求参数的转化
│   │   │   ├── handler          # 事件 handler
│   │   │   ├── utils            # 工具类
│   │   │   └── RestfulServer.kt # 主入口
│   │   └ resource               # 资源文件
│   └── test                     # 单元测试
├── apidoc.json                  # 生成 API 文档的配置文件
├── build.gradle                 # gradle 配置文件
└── README.md
```

## 开发环境配置
生成api文档请参考[文档](readme/generate-apidoc.md)

请参考[环境配置指南](../readme/env-config.md#后端开发环境配置)

