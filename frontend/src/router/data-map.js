export default [
    {
        path: '/data_map/tables',
        name: 'DataMapTables',
        component: resolve => require(['../views/data-map/TableList.vue'], resolve)
    },
    {
        path: '/data_map/dependence',
        name: 'DataMapDependence',
        component: resolve => require(['../views/data-map/TableDependence.vue'], resolve)
    },
    {
        path: '/data_map/albums',
        name: 'DataMapAlbums',
        component: resolve => require(['../views/data-map/TableAlbum.vue'], resolve)
    },
    {
        path: '/function_center',
        name: 'FunctionCenter',
        component: resolve => require(['../views/data-map/FunctionCenter.vue'], resolve)
    }
]
