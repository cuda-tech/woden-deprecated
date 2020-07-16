<style scoped>

</style>

<template>
    <Select :value="value" :multiple="multiple" @on-change="val => this.$emit('input', val)" placeholder="项目组">
        <Icon custom="iconfont icon-project" slot="prefix"/>
        <Option v-for="group in groups" :value="group.id" :key="group.id">
            {{ group.name }}
        </Option>
    </Select>
</template>

<script>
    import GroupService from "@/service/GroupService";

    /**
     * 项目组选择菜单
     * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
     * @since 1.0.0
     */
    export default {
        name: 'GroupSelection',
        props: {
            /**
             * @model
             * 双向绑定的项目组 ID
             */
            value: {
                type: Number,
                default: null
            },

            /**
             * 是否支持多选
             */
            multiple: {
                type: Boolean,
                default: false
            }
        },
        beforeMount() {
            GroupService.listing({pageId: 1, pageSize: 999999}, (count, groups) => {
                this.groups = groups;
            });
        },
        data() {
            return {
                groups: []
            }
        }
    }
</script>


