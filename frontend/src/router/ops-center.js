export default [
    {
        path: '/ops_center/overview',
        name: 'OpsCenterOverview',
        component: resolve => require(['../views/ops-center/Overview.vue'], resolve)
    },
    {
        path: '/ops_center/tasks',
        name: 'OpsCenterTaskList',
        component: resolve => require(['../views/ops-center/TaskList.vue'], resolve)
    },
    {
        path: '/ops_center/routine',
        name: 'OpsCenterRoutine',
        component: resolve => require(['../views/ops-center/Routine.vue'], resolve)
    },
    {
        path: '/ops_center/runtime',
        name: 'OpsCenterRuntime',
        component: resolve => require(['../views/ops-center/Runtime.vue'], resolve)
    },
]
