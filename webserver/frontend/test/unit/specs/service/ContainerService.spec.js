import '../axios-mocker'
import ContainerService from "@/service/ContainerService";

describe('容器接口', () => {

    test('通过 ID 查找容器', done => {
        ContainerService.find(1, container => {
            expect(container).toEqual({
                id: 1,
                hostname: 'new host name',
                cpuLoad: 34,
                memLoad: 31,
                diskUsage: 63,
                createTime: '2029-06-06 19:57:08',
                updateTime: '2020-05-23 01:40:25'
            });
            done()
        })
    });


    test('删除容器', done => {
        ContainerService.delete(1, done)
    });

    test('容器分页查询', done => {
        ContainerService.listing({pageId: 1, pageSize: 3}, (count, containers) => {
            expect(count).toBe(188);
            expect(containers).toEqual([
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
            ]);
            done()
        })
    });

});
