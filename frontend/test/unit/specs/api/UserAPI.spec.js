import userAPI from '../../../../src/api/UserAPI'
import axios from 'axios';

jest.mock('axios');

describe('用户接口', () => {

    test('登录测试', done => {
        axios.post.mockResolvedValue({token: 'token Value'});

        userAPI.login('root', 'root', token => {
            expect(token).toEqual('token Value');
            done()
        })
    });

    test('删除用户测试', done => {
        axios.delete.mockResolvedValue();
        userAPI.delete(1, () => done())
    });

    test('创建用户测试', done => {
        axios.post.mockResolvedValue({
            user: {
                id: 180,
                groups: [1, 2, 3],
                name: "testName",
                email: "testEmail",
                createTime: "2020-05-22 01:43:10",
                updateTime: "2020-05-22 01:43:10"
            }
        });
        userAPI.create({
                name: 'testName',
                password: 'testPassword',
                groupIds: [1, 2, 3],
                email: 'testEmail'
            }, user => {
                expect(user).toEqual({
                    id: 180,
                    groups: [1, 2, 3],
                    name: "testName",
                    email: "testEmail",
                    createTime: "2020-05-22 01:43:10",
                    updateTime: "2020-05-22 01:43:10"
                });
                done()
            }
        )
    });

    test('更新用户测试', done => {
        axios.put.mockResolvedValue({
            user: {
                id: 180,
                groups: [1, 2, 3],
                name: "testName",
                email: "testEmail",
                createTime: "2020-05-22 01:43:11",
                updateTime: "2020-05-22 01:49:55"
            }
        });

        userAPI.update({
                id: 180,
                password: 'testPassword',
                groupIds: [1, 2, 3],
                email: 'testEmail'
            }, user => {
                expect(user).toEqual({
                    id: 180,
                    groups: [1, 2, 3],
                    name: "testName",
                    email: "testEmail",
                    createTime: "2020-05-22 01:43:11",
                    updateTime: "2020-05-22 01:49:55"
                });
                done()
            }
        )
    });

    test('获取用户列表测试', done => {
        axios.get.mockResolvedValue({
            count: 144,
            users: [
                {
                    id: 1,
                    groups: [1],
                    name: "root",
                    email: "root@datahub.com",
                    createTime: "2048-08-14 06:10:35",
                    updateTime: "2051-03-13 21:06:23"
                },
                {
                    id: 2,
                    groups: [1, 2, 5, 6, 7, 8, 9],
                    name: "guest",
                    email: "guest@datahub.com",
                    createTime: "2041-02-10 19:37:55",
                    updateTime: "2042-03-23 08:54:17"
                },
                {
                    id: 3,
                    groups: [2, 3, 4, 6, 8],
                    name: "OHzXwnDAAd",
                    email: "OHzXwnDAAd@189.com",
                    createTime: "2041-11-20 12:44:46",
                    updateTime: "2044-05-12 14:09:07"
                }
            ]
        });
        userAPI.listing(1, 3, null, (count, user) => {
            expect(count).toBe(144);
            expect(user).toEqual([
                {
                    id: 1,
                    groups: [1],
                    name: "root",
                    email: "root@datahub.com",
                    createTime: "2048-08-14 06:10:35",
                    updateTime: "2051-03-13 21:06:23"
                },
                {
                    id: 2,
                    groups: [1, 2, 5, 6, 7, 8, 9],
                    name: "guest",
                    email: "guest@datahub.com",
                    createTime: "2041-02-10 19:37:55",
                    updateTime: "2042-03-23 08:54:17"
                },
                {
                    id: 3,
                    groups: [2, 3, 4, 6, 8],
                    name: "OHzXwnDAAd",
                    email: "OHzXwnDAAd@189.com",
                    createTime: "2041-11-20 12:44:46",
                    updateTime: "2044-05-12 14:09:07"
                }
            ]);
            done()
        })
    });

});


