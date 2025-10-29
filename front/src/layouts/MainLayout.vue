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

        <!-- 项目列表 -->
        <div class="recent-projects" v-if="!collapsed">
          <a-divider style="margin: 8px 0; border-color: #f0f0f0" />
          <div class="projects-header">
            <div class="recent-projects-title">项目</div>
            <a-button
                type="text"
                size="small"
                @click="openCreateProjectModal"
                class="add-project-btn"
            >
              <plus-outlined />
            </a-button>
          </div>

          <a-spin :spinning="loading">
            <div
                v-for="project in projectList"
                :key="project.id"
                class="project-item"
                :class="{ active: currentProjectId === project.id }"
            >
              <div class="project-content" @click="selectProject(project.id)">
                <div class="project-name">{{ project.name }}</div>
                <div class="project-meta">
                  <span class="project-genre" v-if="project.genre">{{ project.genre }}</span>
                  <span class="project-time">{{ formatTime(project.updatedAt) }}</span>
                </div>
              </div>
              <div class="project-actions">
                <a-button
                    type="text"
                    size="small"
                    @click.stop="openEditProjectModal(project)"
                >
                  <edit-filled />
                </a-button>
                <a-button
                    type="text"
                    size="small"
                    danger
                    @click.stop="deleteProject(project)"
                >
                  <delete-outlined />
                </a-button>
              </div>
            </div>

            <div v-if="projectList.length === 0" class="no-projects">
              <p>暂无项目</p>
              <a-button type="link" size="small" @click="openCreateProjectModal">
                创建第一个项目
              </a-button>
            </div>
          </a-spin>
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
    <!-- 项目创建/编辑对话框 -->
    <a-modal
        v-model:open="showProjectModal"
        :title="isEditing ? '编辑项目' : '新建项目'"
        @ok="saveProject"
        @cancel="closeProjectModal"
        :confirmLoading="loading"
        width="600px"
    >
      <a-form :model="projectForm" layout="vertical">
        <a-form-item label="项目名称" required>
          <a-input
              v-model:value="projectForm.name"
              placeholder="输入项目名称"
              :maxlength="255"
          />
        </a-form-item>

        <a-form-item label="项目描述">
          <a-textarea
              v-model:value="projectForm.description"
              placeholder="简要描述你的项目"
              :rows="3"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="类型">
              <a-select
                  v-model:value="projectForm.genre"
                  placeholder="选择项目类型"
                  allowClear
              >
                <a-select-option value="科幻">科幻</a-select-option>
                <a-select-option value="古风">古风</a-select-option>
                <a-select-option value="魔幻">魔幻</a-select-option>
                <a-select-option value="悬疑">悬疑</a-select-option>
                <a-select-option value="现实">现实</a-select-option>
                <a-select-option value="言情">言情</a-select-option>
                <a-select-option value="武侠">武侠</a-select-option>
                <a-select-option value="其他">其他</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="projectForm.status" placeholder="选择状态">
                <a-select-option value="draft">草稿</a-select-option>
                <a-select-option value="in_progress">进行中</a-select-option>
                <a-select-option value="completed">已完成</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="主题">
          <a-input
              v-model:value="projectForm.theme"
              placeholder="例如：牺牲与救赎、成长与蜕变"
          />
        </a-form-item>

        <a-form-item label="写作风格">
          <a-textarea
              v-model:value="projectForm.writingStyle"
              placeholder="描述你希望的写作风格和语言特点"
              :rows="2"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message, Modal } from 'ant-design-vue';
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
  PlusOutlined,
  DeleteOutlined,
  EditFilled,
} from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();
const store = useStore();

const collapsed = ref(false);
const selectedKeys = ref([route.path]);

// 项目管理相关状态
const showProjectModal = ref(false);
const projectForm = ref({
  name: '',
  description: '',
  genre: '',
  theme: '',
  writingStyle: '',
  status: 'draft',
});
const isEditing = ref(false);
const editingProjectId = ref(null);

// 从store获取项目列表和当前项目
const projectList = computed(() => store.getters['project/projects']);
const currentProjectId = computed(() => store.getters.currentProjectId);
const loading = computed(() => store.getters['project/loading']);

// 初始化时加载项目列表
onMounted(async () => {
  try {
    await store.dispatch('project/fetchProjects');

    // 如果有项目且未选择，自动选择第一个
    if (projectList.value.length > 0 && !currentProjectId.value) {
      selectProject(projectList.value[0].id);
    }
  } catch (error) {
    message.error('加载项目列表失败');
  }
});

// 监听路由变化更新选中项
watch(
    () => route.path,
    (newPath) => {
      selectedKeys.value = [newPath];
    }
);

// 菜单点击处理
const handleMenuClick = ({ key }) => {
  if (!currentProjectId.value) {
    message.warning('请先选择一个项目');
    return;
  }
  router.push(key);
};

// 选择项目
const selectProject = (projectId) => {
  store.dispatch('setCurrentProjectId', projectId);
  message.success('已切换项目');
};

// 打开新建项目对话框
const openCreateProjectModal = () => {
  isEditing.value = false;
  editingProjectId.value = null;
  projectForm.value = {
    name: '',
    description: '',
    genre: '',
    theme: '',
    writingStyle: '',
    status: 'draft',
  };
  showProjectModal.value = true;
};

// 打开编辑项目对话框
const openEditProjectModal = (project) => {
  isEditing.value = true;
  editingProjectId.value = project.id;
  projectForm.value = { ...project };
  showProjectModal.value = true;
};

// 保存项目（创建或更新）
const saveProject = async () => {
  if (!projectForm.value.name) {
    message.warning('项目名称不能为空');
    return;
  }

  try {
    if (isEditing.value) {
      // 更新项目
      await store.dispatch('project/updateProject', {
        id: editingProjectId.value,
        data: projectForm.value,
      });
      message.success('项目更新成功');
    } else {
      // 创建项目
      const newProject = await store.dispatch('project/createProject', projectForm.value);
      message.success('项目创建成功');

      // 自动选择新创建的项目
      selectProject(newProject.id);
    }

    showProjectModal.value = false;
  } catch (error) {
    message.error('保存失败: ' + (error.message || '未知错误'));
  }
};

// 删除项目
const deleteProject = (project) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除项目"${project.name}"吗？此操作将删除项目下所有相关数据，不可恢复。`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await store.dispatch('project/deleteProject', project.id);
        message.success('项目删除成功');

        // 如果删除的是当前项目，清除选择
        if (currentProjectId.value === project.id) {
          store.dispatch('setCurrentProjectId', null);
        }
      } catch (error) {
        message.error('删除失败: ' + (error.message || '未知错误'));
      }
    },
  });
};

// 关闭对话框
const closeProjectModal = () => {
  showProjectModal.value = false;
};

// 格式化时间显示
const formatTime = (dateString) => {
  if (!dateString) return '未知';

  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return '刚刚';
  if (diffMins < 60) return `${diffMins}分钟前`;
  if (diffHours < 24) return `${diffHours}小时前`;
  if (diffDays === 0) return '今天';
  if (diffDays === 1) return '昨天';
  if (diffDays < 7) return `${diffDays}天前`;

  return date.toLocaleDateString('zh-CN');
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
  position: fixed;
  left: 0;
  top: 64px;
  z-index: 9;
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

/* 项目头部样式 */
.projects-header {
  display: flex;
  justify-content: space-between;
  align-items: center; /* 添加这行确保垂直居中 */
  margin-bottom: 8px;
  padding: 0 8px;
}

.recent-projects-title {
  font-size: 13px;
  text-transform: uppercase;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 600;
  /* 移除原来的 margin-bottom，改用 padding 控制间距 */
  padding: 8px 0;
  margin: 0; /* 确保没有外边距影响对齐 */
}

.add-project-btn {
  color: #1890ff;
  padding: 0;
  height: auto;
  /* 移除原来的 margin-top，改用 align-items: center 控制垂直对齐 */
  margin: 0;
}

.add-project-btn:hover {
  color: #40a9ff;
}

/* 最近项目容器样式 */
.recent-projects {
  padding: 0 16px;
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

.project-item.active {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
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
  margin-left: 240px;
  transition: margin-left 0.2s;
}

/* 侧边栏收起时，调整主内容区margin */
.sidebar.ant-layout-sider-collapsed + .main-content {
  margin-left: 80px;
}

.content-wrapper {
  padding: 24px;
  min-height: 100%;
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


.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.project-item:hover {
  background: #f5f5f5;
}

.project-item.active {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.project-content {
  flex: 1;
  min-width: 0;
}

.project-name {
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.project-genre {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 0 6px;
  border-radius: 4px;
}

.project-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.project-actions {
  display: none;
  gap: 4px;
}

.project-item:hover .project-actions {
  display: flex;
}

.project-actions .ant-btn {
  padding: 0 4px;
  height: auto;
}

.no-projects {
  text-align: center;
  padding: 24px 12px;
  color: rgba(0, 0, 0, 0.45);
}

.no-projects p {
  margin-bottom: 8px;
}
</style>
