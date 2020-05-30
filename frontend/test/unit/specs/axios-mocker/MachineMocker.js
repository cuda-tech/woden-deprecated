export default {
    get: {
        '/machine': {
            machines: [
                {
                    id: 1,
                    hostname: 'new host name',
                    mac: '1F-72-5B-F7-10-AB',
                    ip: '107.116.90.29',
                    cpuLoad: 34,
                    memLoad: 31,
                    diskUsage: 63,
                    createTime: '2029-06-06 19:57:08',
                    updateTime: '2020-05-23 01:40:25'
                },
                {
                    id: 3,
                    hostname: 'nknvleif',
                    mac: '9E-EE-49-FA-00-F4',
                    ip: '192.168.1.1',
                    cpuLoad: 98,
                    memLoad: 48,
                    diskUsage: 31,
                    createTime: '2035-11-05 14:17:43',
                    updateTime: '2020-05-23 01:40:25'
                },
                {
                    id: 5,
                    hostname: 'anything',
                    mac: '7D-75-70-DE-73-0E',
                    ip: '192.168.1.2',
                    cpuLoad: 1,
                    memLoad: 60,
                    diskUsage: 59,
                    createTime: '2004-11-28 21:50:06',
                    updateTime: '2020-05-23 01:40:25'
                }
            ],
            count: 188
        },
        '/machine/1': {
            machine: {
                id: 1,
                hostname: 'new host name',
                mac: '1F-72-5B-F7-10-AB',
                ip: '107.116.90.29',
                cpuLoad: 34,
                memLoad: 31,
                diskUsage: 63,
                createTime: '2029-06-06 19:57:08',
                updateTime: '2020-05-23 01:40:25'
            }
        }
    },
    post: {
        '/machine': {
            machine: {
                id: 247,
                hostname: '',
                mac: '',
                ip: '192.168.1.20',
                cpuLoad: 0,
                memLoad: 0,
                diskUsage: 0,
                createTime: '2020-05-23 02:00:43',
                updateTime: '2020-05-23 02:00:43'
            }
        }
    },
    put: {
        '/machine/3': {
            machine: {
                id: 3,
                hostname: 'nknvleif',
                mac: '9E-EE-49-FA-00-F4',
                ip: '192.168.1.21',
                cpuLoad: 98,
                memLoad: 48,
                diskUsage: 31,
                createTime: '2035-11-05 14:17:43',
                updateTime: '2020-05-23 12:28:03'
            }
        }
    },
    delete: {
        '/machine/1': {}
    }
}
