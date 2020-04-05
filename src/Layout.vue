<style>

    /*左边菜单栏 icon 在收起时居中*/
    .collapsed-menu .ivu-menu-item {
        padding-left: 10px !important;
        padding-top: 30px !important;
    }

    /*去掉左边子菜单在收起时，以及顶部菜单的展开箭头*/
    .collapsed-menu i.ivu-icon-ios-arrow-down, .ivu-layout-header .ivu-menu-submenu-title i.ivu-icon-ios-arrow-down {
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
        background-color: #3C3F41;
        border-bottom: 2px solid #515151
    }

    /*左边菜单背景色*/
    .ivu-layout-sider, .ivu-menu-vertical, .ivu-layout-sider-trigger {
        background-color: #3C3F41;
        border-right: 1px solid #323232;
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
            <Header style="height: 60px; ">
                <Menu mode="horizontal" theme="dark" active-name="1" style="height: 60px;">
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
                            <MenuItem name="logout"><div @click="logout">退出</div></MenuItem>
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

                        <MenuItem name="DataQuality">
                            <div @click="$router.push({name: 'DataQuality'})">
                                <Icon type="md-bug"/>
                                <span>质量监控</span>
                            </div>
                        </MenuItem>

                        <Submenu name="OpsCenter">
                            <template slot="title">
                                <Icon type="md-pulse"></Icon>
                                <span>运维中心</span>
                            </template>
                            <MenuItem name="OpsOverview">
                                <div @click="$router.push({name: 'OpsCenterOverview'})">
                                    运维大屏
                                </div>
                            </MenuItem>
                            <MenuItem name="TaskList">
                                <div @click="$router.push({name: 'OpsCenterTaskList'})">
                                    任务列表
                                </div>
                            </MenuItem>
                            <MenuItem name="OpsRuntime">
                                <div @click="$router.push({name: 'OpsCenterRuntime'})">
                                    实时运维
                                </div>
                            </MenuItem>
                            <MenuItem name="OpsRoutine">
                                <div @click="$router.push({name: 'OpsCenterRoutine'})">
                                    周期运维
                                </div>
                            </MenuItem>
                        </Submenu>


                        <Submenu name="DataMap">
                            <template slot="title">
                                <Icon type="md-map"/>
                                <span>数据地图</span>
                            </template>
                            <MenuItem name="TableList">
                                <div @click="$router.push({name: 'DataMapTables'})">
                                    数据资产
                                </div>
                            </MenuItem>
                            <MenuItem name="TableDependence">
                                <div @click="$router.push({name: 'DataMapDependence'})">
                                    血缘分析
                                </div>
                            </MenuItem>
                            <MenuItem name="TableAlbum">
                                <div @click="$router.push({name: 'DataMapAlbums'})">
                                    数据专辑
                                </div>
                            </MenuItem>
                            <MenuItem name="FunctionCenter">
                                <div @click="$router.push({name: 'FunctionCenter'})">
                                    函数中心
                                </div>
                            </MenuItem>
                        </Submenu>


                        <Submenu name="ManagerCenter">
                            <template slot="title">
                                <Icon type="md-settings"/>
                                <span>管理中心</span>
                            </template>
                            <MenuItem name="UserManager">
                                <div @click="$router.push({name: 'UserManager'})">
                                    用户管理
                                </div>
                            </MenuItem>
                            <MenuItem name="GroupManager">
                                <div @click="$router.push({name: 'GroupManager'})">
                                    项目组管理
                                </div>
                            </MenuItem>
                            <MenuItem name="MachineManager">
                                <div @click="$router.push({name: 'MachineManager'})">
                                    机器管理
                                </div>
                            </MenuItem>
                        </Submenu>

                        <MenuItem name="Trash">
                            <div @click="$router.push({name: 'Trash'})">
                                <Icon type="md-trash"></Icon>
                                <span>回收站</span>
                            </div>
                        </MenuItem>
                    </Menu>
                </Sider>
                <Content>
                    <router-view/>
                </Content>
            </Layout>
        </Layout>
    </div>
</template>

<script>
    export default {
        mounted() {
        },
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
        methods: {
            logout() {
                window.localStorage["token"] = null;
                this.$router.push({name: 'Login'});
            }
        }
    }
</script>

