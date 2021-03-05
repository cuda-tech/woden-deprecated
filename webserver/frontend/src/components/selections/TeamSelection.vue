<style scoped>

</style>

<template>
    <Select :value="value" :multiple="multiple" @on-change="val => this.$emit('input', val)" placeholder="项目组">
        <Icon custom="iconfont icon-project" slot="prefix"/>
        <Option v-for="team in teams" :value="team.id" :key="team.id">
            {{ team.name }}
        </Option>
    </Select>
</template>

<script>
    import TeamService from "@/service/TeamService";

    /**
     * 项目组选择菜单
     * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
     * @since 0.1.0
     */
    export default {
        name: 'TeamSelection',
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
            TeamService.listing({pageId: 1, pageSize: 999999}, (count, teams) => {
                this.teams = teams;
            });
        },
        data() {
            return {
                teams: []
            }
        }
    }
</script>


