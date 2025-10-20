<template>
  <a-layout class="main-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="header">
      <div class="header-content">
        <div class="logo-section">
          <book-outlined class="logo-icon" />
          <h1 class="logo-title">故事创作助手</h1>
        </div>
        <div class="header-actions">
          <a-badge :count="5" :offset="[-5, 5]">
            <a-button type="text" class="header-btn">
              <bell-outlined />
            </a-button>
          </a-badge>
          <a-button type="text" class="header-btn">
            <setting-outlined />
          </a-button>
          <a-dropdown>
            <a-button type="text" class="header-btn user-btn">
              <a-avatar size="small" :style="{ backgroundColor: '#1890ff' }">
                U
              </a-avatar>
              <span class="username">创作者</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <user-outlined />
                  个人资料
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">
                  <logout-outlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </a-layout-header>

    <a-layout class="content-layout">
      <!-- 侧边栏菜单 -->
      <a-layout-sider
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        class="sidebar"
        :width="240"
      >
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="inline"
          class="sidebar-menu"
          @click="handleMenuClick"
        >
          <a-menu-item key="/character">
            <user-outlined />
            <span>角色设定</span>
          </a-menu-item>
          <a-menu-item key="/worldview">
            <global-outlined />
            <span>世界观设定</span>
          </a-menu-item>
          <a-menu-item key="/timeline">
            <clock-circle-outlined />
            <span>时间线</span>
          </a-menu-item>
          <a-menu-item key="/scene">
            <environment-outlined />
            <span>场景</span>
          </a-menu-item>
          <a-menu-item key="/story">
            <edit-outlined />
            <span>故事生成</span>
          </a-menu-item>
          <a-menu-item key="/inspiration">
            <bulb-outlined />
            <span>灵感模块</span>
          </a-menu-item>
        </a-menu>

        <!-- 最近项目 -->
        <div class="recent-projects" v-if="!collapsed">
          <a-divider style="margin: 16px 0; border-color: #f0f0f0" />
          <div class="recent-projects-title">最近项目</div>
          <div class="project-item">
            <div class="project-name">仙侠世界</div>
            <div class="project-time">上次编辑: 今天</div>
          </div>
          <div class="project-item">
            <div class="project-name">未来都市</div>
            <div class="project-time">上次编辑: 昨天</div>
          </div>
        </div>
      </a-layout-sider>

      <!-- 主内容区 -->
      <a-layout-content class="main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  BookOutlined,
  BellOutlined,
  SettingOutlined,
  UserOutlined,
  LogoutOutlined,
  GlobalOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  EditOutlined,
  BulbOutlined,
} from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();

const collapsed = ref(false);
const selectedKeys = ref([route.path]);

// 监听路由变化更新选中项
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath];
  }
);

// 菜单点击处理
const handleMenuClick = ({ key }) => {
  router.push(key);
};
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏样式 */
.header {
  background: #ffffff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 10;
  height: 64px;
  line-height: 64px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
  color: #1890ff;
}

.logo-title {
  font-size: 20px;
  font-weight: bold;
  color: #333333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  color: rgba(0, 0, 0, 0.65);
  font-size: 18px;
  border-radius: 8px;
  transition: all 0.3s;
}

.header-btn:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
}

.username {
  font-size: 14px;
  color: #333333;
}

/* 侧边栏样式 */
.content-layout {
  background: #f5f5f5;
}

.sidebar {
  background: #ffffff !important;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  height: calc(100vh - 64px);
}

.sidebar-menu {
  background: transparent;
  border: none;
  padding: 16px 8px;
}

:deep(.ant-menu-item) {
  color: rgba(0, 0, 0, 0.65);
  border-radius: 8px;
  margin: 4px 0;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s;
}

:deep(.ant-menu-item:hover) {
  background: #e6f7ff;
  color: #1890ff;
}

:deep(.ant-menu-item-selected) {
  background: #e6f7ff;
  color: #1890ff !important;
  font-weight: 500;
  border-left: 3px solid #1890ff;
}

:deep(.ant-menu-item .anticon) {
  font-size: 18px;
  margin-right: 10px;
}

/* 最近项目样式 */
.recent-projects {
  padding: 0 16px;
}

.recent-projects-title {
  font-size: 12px;
  text-transform: uppercase;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 600;
  margin-bottom: 12px;
  padding: 0 8px;
}

.project-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.project-item:hover {
  background: #f5f5f5;
}

.project-name {
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.project-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

/* 主内容区样式 */
.main-content {
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
  overflow-y: auto;
}

.content-wrapper {
  padding: 24px;
  min-height: 100%;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar,
.main-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-thumb,
.main-content::-webkit-scrollbar-thumb {
  background: #bfbfbf;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover,
.main-content::-webkit-scrollbar-thumb:hover {
  background: #8c8c8c;
}
</style>
