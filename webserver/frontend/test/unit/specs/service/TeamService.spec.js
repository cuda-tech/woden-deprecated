import '../axios-mocker'
import TeamService from "@/service/TeamService";

describe('项目组服务', () => {

    test('通过 ID 查找项目组', done => {
        TeamService.find(2, team => {
            expect(team).toEqual({
                id: 2,
                name: 'testUpdate',
                createTime: '2029-05-26 23:17:01',
                updateTime: '2020-05-23 12:36:21'
            });
            done()
        })
    });

    test('创建项目组', done => {
        TeamService.create({name: 'testCreate'}, team => {
            expect(team).toEqual({
                id: 40,
                name: 'testCreate',
                createTime: '2020-05-23 12:36:52',
                updateTime: '2020-05-23 12:36:52'
            });
            done()
        })
    });

    test('删除项目组', done => {
        TeamService.delete(1, done)
    });

    test('项目组分页查询', done => {
        TeamService.listing({pageId: 1, pageSize: 3}, (count, teams) => {
            expect(count).toBe(32);
            expect(teams).toEqual([
                {
                    id: 2,
                    name: 'testUpdate',
                    createTime: '2029-05-26 23:17:01',
                    updateTime: '2020-05-23 12:36:21'
                },
                {
                    id: 3,
                    name: 'cdqmxplc',
                    createTime: '2045-06-15 10:48:04',
                    updateTime: '2046-03-20 16:54:28'
                },
                {
                    id: 4,
                    name: 'rdiwafif',
                    createTime: '2025-06-12 09:41:41',
                    updateTime: '2027-01-04 14:36:46'
                }
            ]);
            done()
        })
    });

    test('更新项目组', done => {
        TeamService.update(2, {name: 'testUpdate'}, team => {
            expect(team).toEqual({
                id: 2,
                name: 'testUpdate',
                createTime: '2029-05-26 23:17:01',
                updateTime: '2020-05-23 12:36:20'
            });
            done()
        })
    });

});
