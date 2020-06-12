
# 前端开发环境配置
* chromedriver
```shell
npm install chromedriver --chromedriver_cdnurl=http://cdn.npm.taobao.org/dist/chromedriver
```

* 安装 jest 支持

在 IDEA 里, File -> Settings -> Languages & Frameworks -> JavaScript -> Libraries

点击 Download, 从选项菜单里选择 jest, 点击 Download and Install

这样你就在 IDEA 里获得了 jest 的自动补全和自动提示功能

* 添加 vue-jest 支持

在 IDEA 里, Run -> Edit Configurations -> Template -> Jest -> Configuration File

填入
```
/path/to/datahub/frontend/test/unit/jest.conf.js
```
这样你就可以直接在 IDEA 里直接右键执行某个文件的单测而不需要去命令行里执行 jest 命令

* 添加 webpack.config 支持

在 IDEA 里，File -> Settings -> Languages & Frameworks -> JavaScript -> Webpack

将配置文件路径更新为
```
/path/to/datahub/frontend/build_script/webpack.dev.conf.js
```

这样你就可以使用
```javascript
import Tips from "@/components/misc/Tips"
```
来引入组件依赖而不再需要
```javascript
import Tips from "../../../components/misc/Tips"
```
这种迷惑的相对路径引入

# 后端开发环境配置
### IDEA 环境配置
修复单测时 IDEA 的 panel 乱码问题，Help -> Edit Custom VM Opthions，然后添加下面两行配置
```$xslt
-Dfile.encoding=UTF-8
-Dconsole.encoding=UTF-8
```