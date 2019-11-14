export default [
    {
        path: '/data_map/tables',
        name: 'DataMapTables',
        component: resolve => require(['../components/data-map/TableList.vue'], resolve)
    },
    {
        path: '/data_map/dependence',
        name: 'DataMapDependence',
        component: resolve => require(['../components/data-map/TableDependence.vue'], resolve)
    },
    {
        path: '/data_map/albums',
        name: 'DataMapAlbums',
        component: resolve => require(['../components/data-map/TableAlbum.vue'], resolve)
    },
]
