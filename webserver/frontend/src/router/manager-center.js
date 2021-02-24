export default [
    {
        path: '/manager_center/user',
        name: 'UserManager',
        component: resolve => require(['../views/manager-center/UserManager.vue'], resolve)
    },
    {
        path: '/manager_center/group',
        name: 'GroupManager',
        component: resolve => require(['../views/manager-center/GroupManager.vue'], resolve)
    },
    {
        path: '/manager_center/machine',
        name: 'MachineManager',
        component: resolve => require(['../views/manager-center/MachineManager.vue'], resolve)
    },
]
