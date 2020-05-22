import machineAPI from '../../../../src/api/MachineAPI'
import axios from 'axios';

jest.mock('axios');

describe('服务器接口', () => {

    test('通过 ID 查找服务器', done => {
        axios.get.mockResolvedValue({
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
        });

        machineAPI.find(2, machine => {
            expect(machine).toEqual({
                id: 1,
                hostname: 'new host name',
                mac: '1F-72-5B-F7-10-AB',
                ip: '107.116.90.29',
                cpuLoad: 34,
                memLoad: 31,
                diskUsage: 63,
                createTime: '2029-06-06 19:57:08',
                updateTime: '2020-05-23 01:40:25'
            });
            done()
        })
    });

    test('创建服务器', done => {
        axios.post.mockResolvedValue({
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
        });

        machineAPI.create('192.168.1.20', token => {
            expect(token).toEqual({
                id: 247,
                hostname: '',
                mac: '',
                ip: '192.168.1.20',
                cpuLoad: 0,
                memLoad: 0,
                diskUsage: 0,
                createTime: '2020-05-23 02:00:43',
                updateTime: '2020-05-23 02:00:43'
            });
            done()
        })
    });

    test('删除服务器', done => {
        axios.delete.mockResolvedValue();
        machineAPI.delete(1, () => done())
    });

    test('服务器分页查询', done => {
        axios.get.mockResolvedValue({
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
        });

        machineAPI.listing(1, 3, null, (count, machines) => {
            expect(count).toBe(188);
            expect(machines).toEqual([
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
            ]);
            done()
        })
    });

    test('更新服务器', done => {
        axios.put.mockResolvedValue({
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
        });

        machineAPI.update({hostname: 'nknvleif'}, machine => {
            expect(machine).toEqual({
                id: 3,
                hostname: 'nknvleif',
                mac: '9E-EE-49-FA-00-F4',
                ip: '192.168.1.21',
                cpuLoad: 98,
                memLoad: 48,
                diskUsage: 31,
                createTime: '2035-11-05 14:17:43',
                updateTime: '2020-05-23 12:28:03'
            });
            done()
        })
    });


});
