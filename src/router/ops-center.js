export default [
    {
        path: '/ops_center/overview',
        name: 'OpsCenterOverview',
        component: resolve => require(['../components/ops-center/Overview.vue'], resolve)
    },
    {
        path: '/ops_center/routine',
        name: 'OpsCenterRoutine',
        component: resolve => require(['../components/ops-center/Routine.vue'], resolve)
    },
    {
        path: '/ops_center/runtime',
        name: 'OpsCenterRuntime',
        component: resolve => require(['../components/ops-center/Runtime.vue'], resolve)
    },
]
