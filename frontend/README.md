# 前端工程

## 代码结构
```
.
├── build_script            # build 脚本
├── config                  # prod/dev build config 文件
├── package.json
├── src                     # 前端核心业务
│   ├── api                 # 网络请求，原则上只应提供给 service 层调用
│   ├── assets              # 静态文件
│   ├── components          # 通用组件
│   ├── const               # 通用常量
│   ├── event-bus           # 事件总线
│   ├── router              # 前端路由
│   ├── service             # 服务层，用于处理服务端返回的数据, 整合到 store 中以及缓存等应用
│   ├── store               # Vuex 状态管理
│   ├── styles              # 通用样式
│   ├── util                # 项目全局的工具函数
│   ├── views               # 业务界面
│   ├── App.vue             # Vue 根组件
│   ├── Layout.vue          # 页面总布局
│   └── main.js             # 主入口
├── static                  # DevServer 静态文件
├── test                    # 测试
├── index.html              # 前端入口
└── README.md
```

## 开发环境配置
请参考[环境配置指南](../readme/env-config.md#前端开发环境配置)
