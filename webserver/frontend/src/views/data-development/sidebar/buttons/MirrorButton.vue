<style scoped>
    .time {
        font-size: 14px;
        font-weight: bold;
    }

    .btn {
        width: 100%;
        margin: 0 auto;
        line-height: 100%;
        padding-top: 20px;
        padding-bottom: 20px;
    }

</style>
<template>
    <div class="btn" @click="drawerIsOpen=true">
        <Icon type="md-camera" style="margin-bottom: 8px"/>
        <br>
        镜像管理
        <Drawer :closable="false" v-model="drawerIsOpen" width="20">
            <p slot="header">
                镜像列表
                <Tips>
                    点击节点可以将编辑<br>
                    区回滚到指定版本
                </Tips>
            </p>

            <Input v-if="count > 0" suffix="md-search" placeholder="搜素备注"/>

            <Timeline v-if="count > 0" style="margin-top: 20px">
                <TimelineItem v-for="mirror in mirrors" :key="mirror.id">
                    <Icon type="ios-redo" slot="dot"/>
                    <p class="time">
                        <Button type="primary" size="small" @click="() => rollback(mirror)">
                            节点{{ mirror.id }}
                        </Button>
                        {{ mirror.createTime }}
                    </p>
                    <p class="content">{{ mirror.message }}</p>
                </TimelineItem>
            </Timeline>
            <p v-else>尚未生成镜像</p>

            <Page v-if="count > 10" :current="pageId" :total="count" simple @on-change="changePage"/>

        </Drawer>
    </div>
</template>
<script>
    /**
     * 镜像管理按钮
     */
    import FileService from "@/service/FileService";
    import Tips from "@/components/misc/Tips";

    export default {
        name: 'MirrorButton',
        components: {
            Tips: Tips
        },
        props: {
            /**
             * 文件 ID
             */
            fileId: {
                type: Number,
                default: null
            }
        },

        beforeMount() {
            this.changePage(1);
        },

        data() {
            return {
                pageId: 1,
                pageSize: 10,
                like: null,
                count: 0,
                mirrors: [],
                drawerIsOpen: false
            }
        },
        methods: {
            changePage(pageId) {
                FileService.listingMirror(this.fileId, {
                    pageId: this.pageId,
                    pageSize: this.pageSize,
                    like: this.like
                }, (count, mirrors) => {
                    this.count = count;
                    this.mirrors = mirrors;
                })
            },

            changePageSize(pageSize) {
                this.pageSize = pageSize;
                this.changePage(1);
            },

            rollback() {
                alert(1)
            }
        }
    }
</script>
