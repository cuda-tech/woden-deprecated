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
    import GroupAPI from '../../api/GroupAPI';

    /**
     * 项目组选择菜单
     */
    export default {
        name: 'GroupSelection',
        props: {
            /**
             * 绑定的项目组 ID, 可使用 v-model 双向绑定
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
            GroupAPI.listing(1, 999999, null, (count, groups) => {
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


