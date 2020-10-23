<template>
    <Tooltip :placement="placement">
        <Icon type="md-help-circle"/>
        <div slot="content">
            <!-- @slot tips 的内容 -->
            <slot/>
        </div>
    </Tooltip>
</template>
<script>
    /**
     * 帮助提示
     * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
     * @since 0.1.0
     */
    export default {
        name: 'Tips',
        props: {
            /**
             * 弹出方向, 可选值: <br>
             * @values top, top-start, top-end,
             * bottom, bottom-start, bottom-end,
             * left, left-start, left-end,
             * right, right-start, right-end
             */
            placement: {
                type: String,
                default: 'top'
            }
        },
    }
</script>
