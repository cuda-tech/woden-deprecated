<style scoped>

</style>

<template>
    <Card style="margin: 30px;">
        <Row>
            <Button type="primary" @click="createMachineModal.visible=true">
                注册
                <Modal v-model="createMachineModal.visible" @on-ok="createMachine"
                       @on-cancel="createMachineModal.visible = false">
                    <div>
                        <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                        <span style="font-size: large; margin-left: 5px">注册服务器</span>
                    </div>
                    <Form :label-width="80" style="margin-top: 30px">
                        <FormItem label="hostname">
                            <Input v-model="createMachineModal.hostname" :maxlength="128" show-word-limit/>
                        </FormItem>
                        <FormItem label="IP">
                            <Input v-model="createMachineModal.ip" :maxlength="15" show-word-limit/>
                        </FormItem>
                    </Form>
                </Modal>
            </Button>

            <Modal v-model="updateMachineModal.visible" @on-ok="updateMachine"
                   @on-cancel="updateMachineModal.visible = false">
                <div>
                    <Icon type="md-alert" style="font-size: x-large; color: #2db7f5"/>
                    <span style="font-size: large; margin-left: 5px"> 更新服务器信息 </span>
                </div>
                <Form :label-width="80" style="margin-top: 30px">
                    <FormItem label="hostname">
                        <Input v-model="updateMachineModal.hostname" :maxlength="128" show-word-limit/>
                    </FormItem>
                    <FormItem label="IP">
                        <Input v-model="updateMachineModal.ip" :maxlength="15" show-word-limit/>
                    </FormItem>
                </Form>
            </Modal>

            <Col span="6" style="float: right">
                <Input v-model="search.key" search @on-enter="() => this.changePage(1)"/>
            </Col>
        </Row>

        <Table :columns="columns" :data="machines" :loading="loading" style="margin-top: 30px;"/>

        <div style="text-align: right; margin-top: 20px">
            <Page :current="pageId" :total="this.machineCount" show-sizer show-total
                  @on-change="changePage"
                  @on-page-size-change="changePageSize"/>
        </div>
    </Card>

</template>

<script>
    import MachineAPI from "../../api/MachineAPI";

    /**
     * 机器管理
     * 普通用户：无权查看
     * 超级管理员：增删改查机器
     */
    export default {
        name: "MachineManager",
        beforeMount() {
            this.changePage(1);
        },
        data() {
            return {
                columns: [
                    {
                        title: '服务器 ID',
                        key: 'id',
                        align: 'center',
                        width: 150
                    },
                    {
                        title: 'hostname',
                        align: 'center',
                        key: 'hostname',
                    },
                    {
                        title: 'IP',
                        align: 'center',
                        key: 'ip',
                    },
                    {
                        title: 'CPU 负载',
                        align: 'center',
                        key: 'cpuLoad',
                    },
                    {
                        title: '内存负载',
                        align: 'center',
                        key: 'memLoad'
                    },
                    {
                        title: '磁盘使用率',
                        align: 'center',
                        key: 'diskUsage'
                    },
                    {
                        title: '创建时间',
                        align: 'center',
                        key: 'createTime'
                    },
                    {
                        title: '更新时间',
                        align: 'center',
                        key: 'updateTime'
                    },
                    {
                        title: '操作',
                        align: 'center',
                        render: (h, {row, col, index}) => {
                            return h('div', [
                                h('Button', {
                                    props: {
                                        type: 'info',
                                        size: 'small',
                                        shape: 'circle',
                                        icon: 'md-create'
                                    },
                                    on: {
                                        'click': () => {
                                            this.updateMachineModal.id = row.id;
                                            this.updateMachineModal.hostname = row.hostname;
                                            this.updateMachineModal.ip = row.ip;
                                            this.updateMachineModal.rowIndex = index;
                                            this.updateMachineModal.visible = true;
                                        }
                                    }
                                }),
                                h('Poptip', {
                                        props: {
                                            title: `确认删除机器 ${row.hostname} ?`,
                                            confirm: true,
                                            transfer: true
                                        },
                                        on: {
                                            'on-ok': () => {
                                                MachineAPI.delete(row.id, () => {
                                                    this.machines.splice(index, 1);
                                                    //todo: 向下补全
                                                });
                                            }
                                        }
                                    }, [h('Button', {
                                        props: {
                                            type: 'error',
                                            size: 'small',
                                            shape: 'circle',
                                            icon: 'md-trash'
                                        },
                                        style: {
                                            marginLeft: '5px'
                                        }
                                    })]
                                ),
                            ])

                        }
                    }
                ],
                machines: [],
                machineCount: 0,
                pageSize: 10,
                pageId: 1,
                createMachineModal: {
                    visible: false,
                    hostname: null,
                    ip: null
                },
                updateMachineModal: {
                    visible: false,
                    id: null,
                    rowIndex: null,
                    hostname: null,
                    ip: null
                },
                search: {
                    key: null
                },
                loading: false

            }
        },
        methods: {
            changePage(pageId) {
                this.loading = true;
                MachineAPI.listing(pageId, this.pageSize, this.search.key, (count, machines) => {
                    this.machineCount = count;
                    this.machines = machines;
                    this.pageId = pageId;
                    this.loading = false;
                });
            },

            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            },

            createMachine() {
                MachineAPI.create(this.createMachineModal, machine => {
                    this.machineCount += 1;
                    if (this.machines.length < this.pageSize) {
                        this.machines.push(machine);
                    }
                    this.createMachineModal = {
                        visible: false,
                        hostname: null,
                        ip: null
                    };
                });
            },

            updateMachine() {
                MachineAPI.update(this.updateMachineModal, machine => {
                    this.$set(this.machines, this.updateMachineModal.rowIndex, machine);
                    this.updateMachineModal = {
                        visible: false,
                        id: null,
                        rowIndex: null,
                        hostname: null,
                        ip: null
                    }
                });
            }
        }
    }
</script>
