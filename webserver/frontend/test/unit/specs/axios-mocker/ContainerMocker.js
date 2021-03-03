export default {
    get: {
        '/container': {
            containers: [
                {
                    id: 1,
                    hostname: 'new host name',
                    cpuLoad: 34,
                    memLoad: 31,
                    diskUsage: 63,
                    createTime: '2029-06-06 19:57:08',
                    updateTime: '2020-05-23 01:40:25'
                },
                {
                    id: 3,
                    hostname: 'nknvleif',
                    cpuLoad: 98,
                    memLoad: 48,
                    diskUsage: 31,
                    createTime: '2035-11-05 14:17:43',
                    updateTime: '2020-05-23 01:40:25'
                },
                {
                    id: 5,
                    hostname: 'anything',
                    cpuLoad: 1,
                    memLoad: 60,
                    diskUsage: 59,
                    createTime: '2004-11-28 21:50:06',
                    updateTime: '2020-05-23 01:40:25'
                }
            ],
            count: 188
        },
        '/container/1': {
            container: {
                id: 1,
                hostname: 'new host name',
                cpuLoad: 34,
                memLoad: 31,
                diskUsage: 63,
                createTime: '2029-06-06 19:57:08',
                updateTime: '2020-05-23 01:40:25'
            }
        }
    },
    delete: {
        '/container/1': {}
    }
}
