# StoryForge 前端项目

基于原型图搭建的AI故事创作助手前端框架

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Ant Design Vue 4.x** - 企业级UI组件库
- **Vue Router 4** - 官方路由管理器
- **Vuex 4** - 状态管理
- **Axios** - HTTP客户端

## 项目结构

```
front/
├── src/
│   ├── layouts/                # 布局组件
│   │   └── MainLayout.vue      # 主布局(顶部导航+侧边栏)
│   ├── views/                  # 页面视图
│   │   ├── character/          # 角色设定模块
│   │   │   └── Index.vue
│   │   ├── worldview/          # 世界观设定模块
│   │   │   ├── Index.vue
│   │   │   └── style.css
│   │   ├── timeline/           # 时间线模块
│   │   │   └── Index.vue
│   │   ├── scene/              # 场景设定模块
│   │   │   └── Index.vue
│   │   ├── story/              # 故事生成模块
│   │   │   └── Index.vue
│   │   └── inspiration/        # 灵感模块
│   │       └── Index.vue
│   ├── store/                  # Vuex状态管理
│   │   ├── index.js            # store入口
│   │   └── modules/            # 模块化store
│   │       ├── character.js
│   │       ├── worldview.js
│   │       ├── timeline.js
│   │       ├── scene.js
│   │       ├── story.js
│   │       └── inspiration.js
│   ├── router/                 # 路由配置
│   │   ├── index.js            # 路由实例
│   │   └── router.config.js    # 路由配置
│   ├── http/                   # HTTP请求封装
│   │   ├── index.js            # axios实例
│   │   ├── api.js              # API接口封装
│   │   ├── axios.config.js     # axios配置
│   │   └── axios.interceptors.config.js  # 拦截器
│   ├── components/             # 公共组件
│   ├── assets/                 # 静态资源
│   ├── App.vue                 # 根组件
│   ├── main.js                 # 入口文件
│   └── index.css               # 全局样式
├── public/                     # 公共资源
├── index.html                  # HTML模板
├── package.json                # 项目依赖
└── vite.config.js              # Vite配置
```

## 六大核心模块

### 1. 角色设定模块 (Character)
- 路由: `/character`
- 功能:
  - 角色基本信息管理(姓名、年龄、性别、职业、外貌)
  - 性格特征标签系统
  - 深度设定(动机、弱点、背景、价值观、恐惧、欲望)
  - 动态属性(口癖、行为习惯、关键关系)
  - AI辅助生成角色
  - 角色卡片预览
  - 导出角色卡功能

### 2. 世界观设定模块 (Worldview)
- 路由: `/worldview`
- 功能:
  - 宇宙法则设定(物理规则、魔法系统、力量体系)
  - 社会结构设定(政治体制、阶层划分、经济体系)
  - 地理环境(世界地图上传、区域描述)
  - 历史背景(当前时代、关键历史事件)
  - 专有名词词典
  - 参考资料管理

### 3. 时间线模块 (Timeline)
- 路由: `/timeline`
- 功能:
  - 时间线事件管理
  - 关键转折点标记
  - 事件类型分类(启程冒险、关系变化、重大牺牲等)
  - 时间跨度统计
  - 角色关联
  - 悖论检测
  - 事件筛选和排序

### 4. 场景设定模块 (Scene)
- 路由: `/scene`
- 功能:
  - 场景基本信息(名称、类型、时间背景)
  - 物理环境描述
  - 氛围与情绪基调设定
  - 场景元素与道具管理
  - 场景视觉预览(图片上传)
  - 关联角色管理
  - 场景标签与分类

### 5. 故事生成模块 (Story)
- 路由: `/story`
- 功能:
  - 三种生成模式(短篇、章节、对话)
  - 设定集选择(角色、世界观、场景)
  - 冲突与主题设定
  - 写作风格选择
  - 故事概要输入
  - AI智能生成
  - 质量评估
  - 故事导出

### 6. 灵感模块 (Inspiration)
- 路由: `/inspiration`
- 功能:
  - 创意推荐
  - 趋势分析
  - 想法组合
  - 主题推荐
  - (待进一步开发)

## 设计特色

### UI设计
- **深色主题**: 基于原型图的深黑色背景(#0a0a0a)
- **粉紫渐变**: 主色调使用粉色(#d946ef)到紫色(#8b5cf6)的渐变
- **玻璃态拟态**: 半透明卡片+毛玻璃效果
- **响应式布局**: 支持不同屏幕尺寸
- **动画过渡**: 平滑的页面切换和交互动画

### 技术亮点
- **模块化设计**: 每个功能模块独立管理
- **状态管理**: Vuex集中管理应用状态
- **API封装**: 统一的HTTP请求接口
- **路由懒加载**: 优化首屏加载速度
- **类型化API**: 完整的RESTful API接口定义

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```
访问: http://localhost:8888

### 生产构建
```bash
npm run build
```

## API接口说明

所有API接口已在 `src/http/api.js` 中定义,包括:

- **characterApi**: 角色相关接口
- **worldviewApi**: 世界观相关接口
- **timelineApi**: 时间线相关接口
- **sceneApi**: 场景相关接口
- **storyApi**: 故事生成相关接口
- **inspirationApi**: 灵感相关接口
- **userApi**: 用户认证接口
- **uploadApi**: 文件上传接口

## 后续开发步骤

### 阶段一:完善现有模块(已完成✅)
- [x] 创建项目目录结构
- [x] 配置路由系统
- [x] 创建主布局组件
- [x] 创建6大模块基础页面
- [x] 配置Vuex状态管理
- [x] 封装API请求
- [x] 配置全局样式和主题

### 阶段二:功能实现(待开发)
- [ ] 完善角色设定表单验证和数据持久化
- [ ] 实现世界观设定的富文本编辑
- [ ] 开发时间线可视化组件
- [ ] 场景元素拖拽排列功能
- [ ] 故事生成实时流式输出
- [ ] 灵感模块完整功能开发

### 阶段三:AI集成(待开发)
- [ ] 对接后端GPT-4 API
- [ ] 实现角色AI生成
- [ ] 实现世界观AI辅助
- [ ] 实现故事AI生成
- [ ] 质量评估系统集成

### 阶段四:优化增强(待开发)
- [ ] 性能优化(虚拟滚动、懒加载)
- [ ] 离线缓存支持
- [ ] 数据导入导出功能
- [ ] 多语言支持
- [ ] 暗色/亮色主题切换
- [ ] 移动端适配优化

## 配置说明

### 代理配置
在 `vite.config.js` 中已配置后端代理:
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

### 环境变量
可在项目根目录创建 `.env` 文件配置:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=故事创作助手
```

## 注意事项

1. **Node版本**: 建议使用 Node.js 16+
2. **依赖安装**: 首次运行请先执行 `npm install`
3. **后端依赖**: 需要配合Spring Boot后端运行
4. **浏览器兼容**: 建议使用Chrome、Edge、Firefox最新版本

## 相关文档

- [Vue 3文档](https://v3.vuejs.org/)
- [Ant Design Vue文档](https://antdv.com/)
- [Vite文档](https://vitejs.dev/)
- [Vue Router文档](https://router.vuejs.org/)
- [Vuex文档](https://vuex.vuejs.org/)

## License

本项目仅供学习和开发使用。
