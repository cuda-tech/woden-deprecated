<style>

    /*左边菜单栏 icon 在收起时居中*/
    .collapsed-menu .ivu-menu-item {
        padding-left: 10px !important;
        padding-top: 30px !important;
    }

    /*去掉左边子菜单在收起时，以及顶部菜单的展开箭头*/
    .collapsed-menu i.ivu-icon-ios-arrow-down, .ivu-layout-header i.ivu-icon-ios-arrow-down {
        display: none !important;
    }

    /*左边子菜单图标居中*/
    .collapsed-menu .ivu-menu-submenu-title .ivu-icon {
        margin-left: -15px;
    }

    /*顶部菜单头像居中*/
    .ivu-layout-header .ivu-menu-submenu-title span > i {
        margin-right: 0;
    }

    /*头部菜单背景色*/
    .ivu-layout-header, .ivu-menu-horizontal {
        background: #515a6e;
    }

    /*左边菜单背景色*/
    .ivu-layout-sider, .ivu-menu-vertical, .ivu-layout-sider-trigger {
        background: #5b6270;
    }


    /*左边子菜单打开时样式*/
    .menu-item span {
        display: inline-block;
        overflow: hidden;
        width: 69px;
        text-overflow: ellipsis;
        white-space: nowrap;
        vertical-align: bottom;
        transition: width .2s ease .2s;
    }

    .menu-item i {
        transform: translateX(0px);
        transition: font-size .2s ease, transform .2s ease;
        vertical-align: middle;
        font-size: 16px;
    }

    /*左边子菜单收起时样式*/
    .collapsed-menu span {
        width: 0;
        transition: width .2s ease;
    }

    .collapsed-menu i {
        transform: translateX(5px);
        transition: font-size .2s ease .2s, transform .2s ease .2s;
        vertical-align: middle;
        font-size: 22px;
    }

    .layout {
        border: 1px solid #d7dde4;
        background: #f5f7f9;
        position: relative;
        border-radius: 4px;
        overflow: hidden;
    }

    .logo {
        color: white;
        font-size: 1.8em;
        font-weight: bolder;
    }
</style>

<template>
    <div class="layout">
        <Layout>
            <Header style="height: 60px">
                <Menu mode="horizontal" theme="dark" active-name="1" style="height: 60px">
                    <span class="logo">
                        DataHub
                    </span>
                    <span style="float: right;">
                        <Submenu name="user">
                            <template slot="title">
                                <Avatar icon="ios-person" style="margin-bottom: 5px"/>
                            </template>
                            <MenuItem name="my_table">我的资产</MenuItem>
                            <MenuItem name="my_album">我的专辑</MenuItem>
                            <MenuItem name="user_guide">用户手册</MenuItem>
                            <MenuItem name="logout">退出</MenuItem>
                        </Submenu>
                    </span>
                </Menu>
            </Header>
            <Layout :style="{minHeight: '90vh'}">
                <Sider collapsible :collapsed-width="50" v-model="isCollapsed" :width="180">
                    <Menu active-name="1-2" theme="dark" width="auto" :class="menuitemClasses">
                        <MenuItem name="DataDevelopment">
                            <div @click="$router.push({name: 'DataDevelopment'})">
                                <Icon type="md-construct"></Icon>
                                <span>数据开发</span>
                            </div>
                        </MenuItem>
                        <MenuItem name="TempQuery">
                            <div @click="$router.push({name: 'TempQuery'})">
                                <Icon type="md-search"></Icon>
                                <span>临时查询</span>
                            </div>
                        </MenuItem>

                        <Submenu name="ReleaseCenter">
                            <template slot="title">
                                <Icon type="md-paper-plane"></Icon>
                                <span>发布中心</span>
                            </template>
                            <MenuItem name="CreatePackage">
                                <div @click="$router.push({name: 'ReleaseCenterCreatePackage'})">
                                    创建发布包
                                </div>
                            </MenuItem>
                            <MenuItem name="ListPackage">
                                <div @click="$router.push({name: 'ReleaseCenterListPackage'})">
                                    发布包列表
                                </div>
                            </MenuItem>
                        </Submenu>

                        <MenuItem name="DataQuality">
                            <Icon type="md-bug"/>
                            <span>质量监控</span>
                        </MenuItem>

                        <Submenu name="OpsCenter">
                            <template slot="title">
                                <Icon type="md-pulse"></Icon>
                                <span>运维中心</span>
                            </template>
                            <MenuItem name="OpsOverview">运维大屏</MenuItem>
                            <MenuItem name="OpsRuntime">实时运维</MenuItem>
                            <MenuItem name="OpsRoutine">周期运维</MenuItem>
                        </Submenu>


                        <Submenu name="DataMap">
                            <template slot="title">
                                <Icon type="md-map"></Icon>
                                <span>数据地图</span>
                            </template>
                            <MenuItem name="TableList">数据资产</MenuItem>
                            <MenuItem name="TableDependence">血缘分析</MenuItem>
                            <MenuItem name="TableAlbum">数据专辑</MenuItem>
                        </Submenu>

                        <MenuItem name="FunctionCenter">
                            <Icon type="logo-foursquare"/>
                            <span>函数中心</span>
                        </MenuItem>

                        <MenuItem name="Trash">
                            <Icon type="md-trash"></Icon>
                            <span>回收站</span>
                        </MenuItem>
                    </Menu>
                </Sider>
                <router-view style="width: 100%"/>


            </Layout>


        </Layout>


    </div>
</template>

<script>
    export default {
        data() {
            return {
                isCollapsed: true,
            };
        },
        computed: {
            menuitemClasses: function () {
                return [
                    'menu-item',
                    this.isCollapsed ? 'collapsed-menu' : ''
                ]
            }
        },
        methods: {}
    }
</script>

