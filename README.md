## Datahub
### Datahub 是什么

### Change Log
#### v0.1.0
开发中

### 如何开发
[生成api文档](service/readme/generate-apidoc.md)


### 代码结构
```
.
├── README.md
├── build                   # build 脚本
├── config                  # prod/dev build config 文件
├── hera                    # 代码发布上线
├── index.html              # 前端入口
├── package.json
├── src                     # 前端核心业务
│   ├── App.vue             # Vue 根组件
│   ├── api                 # 网络请求
│   ├── assets              # 静态文件
│   ├── components          # 通用组件
│   ├── event-bus           # 事件总线
│   ├── main.js             # Vue 入口文件
│   ├── router              # 前端路由
│   ├── service             # 服务层，用于处理服务端返回的数据, 整合到 store 中
│   ├── store               # Vuex 状态管理
│   ├── util                # 项目全局的工具函数
│   └── view                # 业务页面
├── static                  # DevServer 静态文件
└── test                    # 测试
```
