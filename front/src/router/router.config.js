/**
 * 路由配置
 * 基于原型图设计的6大核心模块
 */
export default [
    {
        path: '/',
        name: 'Layout',
        component: () => import('../layouts/MainLayout.vue'),
        redirect: '/character',
        children: [
            {
                path: '/character',
                name: 'Character',
                meta: { title: '角色设定', icon: 'UserOutlined' },
                component: () => import('../views/character/Index.vue'),
            },
            {
                path: '/worldview',
                name: 'Worldview',
                meta: { title: '世界观设定', icon: 'GlobalOutlined' },
                component: () => import('../views/worldview/Index.vue'),
            },
            {
                path: '/timeline',
                name: 'Timeline',
                meta: { title: '时间线', icon: 'ClockCircleOutlined' },
                component: () => import('../views/timeline/Index.vue'),
            },
            {
                path: '/scene',
                name: 'Scene',
                meta: { title: '场景设定', icon: 'EnvironmentOutlined' },
                component: () => import('../views/scene/Index.vue'),
            },
            {
                path: '/story',
                name: 'Story',
                meta: { title: '故事生成', icon: 'EditOutlined' },
                component: () => import('../views/story/Index.vue'),
            },
            {
                path: '/inspiration',
                name: 'Inspiration',
                meta: { title: '灵感模块', icon: 'BulbOutlined' },
                component: () => import('../views/inspiration/Index.vue'),
            },
        ]
    },
]
