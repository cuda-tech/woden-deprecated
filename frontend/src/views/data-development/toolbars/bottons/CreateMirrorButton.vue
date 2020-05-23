<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="创建镜像" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-camera" @click="modalIsOpen=true" type="text" style="color: #3592C0"/>
        <Modal
            v-model="modalIsOpen"
            title="新建文件镜像"
            @on-ok="releaseNode"
            @on-cancel="cancel">
            <Form :label-width="70">
                <FormItem label="镜像名">
                    <Input v-model="mirror.name" maxlength="128" show-word-limit/>
                </FormItem>
                <FormItem label="备注">
                    <Input v-model="mirror.message" maxlength="512" show-word-limit type="textarea"
                           :autosize="{minRows: 4, maxRows: 8}"/>
                </FormItem>
            </Form>
        </Modal>
    </Tooltip>
</template>

<script>
    /**
     * 创建镜像按钮
     */
    export default {
        name: "CreateMirrorButton",
        data() {
            return {
                modalIsOpen: false,
                mirror: {
                    name: null,
                    message: null
                }
            }
        },
        methods: {
            releaseNode() {
                this.modalIsOpen = false;
            },
            cancel() {
                this.modalIsOpen = false;
            }

        }
    }
</script>
