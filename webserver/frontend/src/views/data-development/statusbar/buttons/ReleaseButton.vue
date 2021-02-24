<style scoped>
    .btn:hover {
        background-color: #4C5052;
    }
</style>
<template>
    <Tooltip content="发布" placement="top" theme="dark" transfer :delay="1000">
        <Button class="btn" icon="md-paper-plane" @click="modalIsOpen=true" type="text" style="color: #3592C0"/>
        <Modal width="800"
            v-model="modalIsOpen"
            title="发布到生产环境"
            :footer-hide="true"
            @on-ok="releaseNode"
            @on-cancel="cancel">

            <div v-if="step === 0">
                代码
            </div>
            <div v-else-if="step === 1">
                <Form :label-width="70">
                    <FormItem label="任务名">
                        <Input placeholder="Enter something..." />
                    </FormItem>
                    <FormItem label="负责人">
                        <Select>
                        </Select>
                    </FormItem>
                    <FormItem label="执行参数">
                        <Input type="textarea" placeholder="Enter something..." />
                    </FormItem>
                    <FormItem label="调度信息">
                        每
                        <Select value="1" style="width: 50px" size="small">
                            <Option value="0" label="时"/>
                            <Option value="1" label="天"/>
                            <Option value="2" label="周"/>
                            <Option value="3" label="月"/>
                        </Select>
                        在队列
                        <Select value="1" style="width: 70px" size="small">
                            <Option value="0" label="day"/>
                            <Option value="1" label="bigdata"/>
                            <Option value="2" label="spark"/>
                        </Select>
                        上以优先级
                        <Rate />
                        执行
                    </FormItem>
                    <FormItem label="超时配置">
                        超过 <InputNumber value="0" :max="120" :min="0" :step="10" style="width: 70px" size="small"/> 分钟未执行，
                        或超过 <InputNumber value="0" :max="120" :min="0" :step="10" style="width: 70px" size="small"/> 分钟未执行成功则向负责人报警
                    </FormItem>
                    <FormItem label="失败处理" style="display: inline">
                        失败后
                        <Select value="1" style="width: 70px" size="small">
                            <Option value="0" label="跳过"/>
                            <Option value="1" label="重试"/>
                        </Select>
                        <span>
                            最多 <InputNumber :max="10" :min="0" size="small" style="width: 50px"  /> 次，
                            每次间隔 <InputNumber value="0" :max="90" :min="0" :step="10" size="small" style="width: 50px"  /> 分钟
                        </span>
                        <span>
                            该任务，直接执行下游
                        </span>
                    </FormItem>

                    <FormItem label="上游依赖" style="margin-top: 10px">
                        <List border size="small" style="margin-top: 10px">
                            <ListItem>
                                database_1.table_1

                                偏移 <InputNumber :max="10" :min="0" size="small" style="width: 50px" /> 天，
                            </ListItem>
                            <ListItem>database_2.table_2</ListItem>
                            <ListItem>database_3.table_3</ListItem>
                            <ListItem><Button>+</Button></ListItem>
                        </List>
                    </FormItem>
                </Form>
            </div>

            <div v-else-if="step === 2">
                <Input type="textarea" placeholder="Enter something..." />
            </div>
            <div v-else>
                <Steps  direction="vertical">
                    <Step title="生成镜像" content="这里是该步骤的描述信息"></Step>
                    <Step title="创建调度任务" content="这里是该步骤的描述信息"></Step>
                    <Step title="All Done!" content="这里是该步骤的描述信息"></Step>
                </Steps>
            </div>

            <Steps :current="step" v-if="step < 3">
                <Step title="语法检查"/>
                <Step title="调度配置"/>
                <Step title="变更说明"/>
            </Steps>

            <div style="margin-top: 20px; text-align: right" v-if="step<3">
                <Button v-if="step > 0" @click="step--">上一步</Button>
                <Button @click="step++">下一步</Button>
            </div>
        </Modal>
    </Tooltip>
</template>
<script>
    export default {
        data() {
            return {
                modalIsOpen: true,
                step: 1
            }
        },
        methods: {
            releaseNode() {
                this.modalIsOpen = false;
            },
            cancel() {
                this.modalIsOpen = false;
            }
        },
    }
</script>
