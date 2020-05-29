import ViewUI from 'view-design';
import {mount, createLocalVue} from '@vue/test-utils';
import GroupSelection from '@/components/selections/GroupSelection.vue';
import axios from "axios";

jest.mock('axios');
axios.get.mockResolvedValue({
    groups: [
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
    ],
    count: 32
});

const localVue = createLocalVue();
localVue.use(ViewUI);

const factory = (props = {}) => {
    return mount(GroupSelection, {
        localVue: localVue,
        propsData: {
            ...props
        }
    })
};

describe('项目组选择菜单', async () => {

    beforeEach(() => {
        // 貌似因为 iView 在 select 组件中引用的 popper 依赖有问题
        // 会导致 TypeError: Cannot read property 'nodeName' of undefined
        // 因此 mock 掉 console 的 error 函数让它闭嘴
        jest.spyOn(console, 'error').mockImplementation(() => {
        });
    });

    test('测试渲染', async () => {
        const wrapper = factory();
        await wrapper.vm.$nextTick();

        // 默认提示
        expect(wrapper.find('span.ivu-select-placeholder').text()).toBe('项目组');

        // 图标
        expect(wrapper.find('span.ivu-select-prefix').contains('i.icon-project')).toBe(true);

        // 选项
        let selections = wrapper.find('ul.ivu-select-dropdown-list')
            .findAll('li').wrappers.map(option => option.text());
        expect(selections).toEqual(['testUpdate', 'cdqmxplc', 'rdiwafif']);
    });


    test("测试双向绑定", async () => {
        const wrapper = factory({value: 3});
        await wrapper.vm.$nextTick();

        // 初始值
        expect(wrapper.find("span.ivu-select-selected-value").text().trim()).toBe("cdqmxplc");

        // 点击 testUpdate 选项
        wrapper.findAll('li.ivu-select-item').wrappers[0].trigger('click');
        await wrapper.vm.$nextTick();
        expect(wrapper.find("span.ivu-select-selected-value").text().trim()).toBe("testUpdate");
        expect(wrapper.emitted()).toHaveProperty("input");
    });


});
