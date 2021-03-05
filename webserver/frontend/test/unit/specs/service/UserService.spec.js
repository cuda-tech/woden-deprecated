import '../axios-mocker'
import UserService from "@/service/UserService";

describe('用户服务', () => {

    test('登录测试', done => {
        UserService.login({username: 'root', password: 'root'}, token => {
            expect(token).toEqual('token Value');
            done()
        })
    });

    test('删除用户测试', done => {
        UserService.delete(1, done)
    });

    test('创建用户测试', done => {
        UserService.create({
                name: 'testName',
                password: 'testPassword',
                teamIds: [1, 2, 3],
                email: 'testEmail'
            }, user => {
                expect(user).toEqual({
                    id: 180,
                    teams: [1, 2, 3],
                    name: 'testName',
                    email: 'testEmail',
                    createTime: '2020-05-22 01:43:10',
                    updateTime: '2020-05-22 01:43:10'
                });
                done()
            }
        )
    });

    test('更新用户测试', done => {
        UserService.update(180, {
                password: 'testPassword',
                teamIds: [1, 2, 3],
                email: 'testEmail'
            }, user => {
                expect(user).toEqual({
                    id: 180,
                    teams: [1, 2, 3],
                    name: 'testName',
                    email: 'testEmail',
                    createTime: '2020-05-22 01:43:11',
                    updateTime: '2020-05-22 01:49:55'
                });
                done()
            }
        )
    });

    test('获取用户列表测试', done => {
        UserService.listing({pageId: 1, pageSize: 3}, (count, user) => {
            expect(count).toBe(144);
            expect(user).toEqual([
                {
                    id: 1,
                    teams: [1],
                    name: 'root',
                    email: 'root@woden.com',
                    createTime: '2048-08-14 06:10:35',
                    updateTime: '2051-03-13 21:06:23'
                },
                {
                    id: 2,
                    teams: [1, 2, 5, 6, 7, 8, 9],
                    name: 'guest',
                    email: 'guest@woden.com',
                    createTime: '2041-02-10 19:37:55',
                    updateTime: '2042-03-23 08:54:17'
                },
                {
                    id: 3,
                    teams: [2, 3, 4, 6, 8],
                    name: 'OHzXwnDAAd',
                    email: 'OHzXwnDAAd@189.com',
                    createTime: '2041-11-20 12:44:46',
                    updateTime: '2044-05-12 14:09:07'
                }
            ]);
            done()
        })
    });

    test('获取当前用户登录信息', done => {
        UserService.currentUser(user => {
            expect(user).toEqual({
                id: 1,
                teams: [1],
                name: 'root',
                email: 'root@woden.com',
                createTime: '2048-08-14 06:10:35',
                updateTime: '2051-03-13 21:06:23'
            });
            done()
        })
    });

    test('通过 ID 查找用户', done => {
        UserService.find(10, user => {
            expect(user).toEqual({
                id: 10,
                teams: [1, 3, 4, 5, 6, 8, 9],
                name: 'IinOzxLtGL',
                email: 'IinOzxLtGL@139.com',
                createTime: '2018-03-21 03:59:24',
                updateTime: '2019-02-28 12:26:06'
            });
            done()
        })
    })

});

