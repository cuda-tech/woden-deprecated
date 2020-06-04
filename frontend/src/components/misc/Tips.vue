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
     * @since 1.0.0
     */
    export default {
        name: 'Tips',
        props: {
            /**
             * 弹出方向, 可选值: <br>
             * top, top-start, top-end <br>
             * bottom, bottom-start, bottom-end <br>
             * left, left-start, left-end <br>
             * right, right-start, right-end <br>
             */
            placement: {
                type: String,
                default: 'top'
            }
        },
    }
</script>
