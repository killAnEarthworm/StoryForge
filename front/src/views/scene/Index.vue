<template>
  <div class="scene-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">场景设定</h1>
        <p class="page-description">定义故事中的关键场景,包括环境、氛围和可用元素</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large">
          <template #icon><save-outlined /></template>
          保存场景
        </a-button>
        <a-button size="large">
          <template #icon><copy-outlined /></template>
          复制
        </a-button>
      </div>
    </div>

    <a-row :gutter="24">
      <a-col :span="16">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 场景基本信息 -->
          <a-card title="场景基本信息" :bordered="false">
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="场景名称">
                    <a-input placeholder="例如:月光下的古堡大厅" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="场景类型">
                    <a-select placeholder="请选择">
                      <a-select-option value="indoor">室内场景</a-select-option>
                      <a-select-option value="outdoor">室外自然场景</a-select-option>
                      <a-select-option value="city">城市环境</a-select-option>
                      <a-select-option value="special">特殊空间</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="物理环境描述">
                <a-textarea
                  :rows="4"
                  placeholder="详细描述场景的物理环境,包括布局、建筑风格、装饰等..."
                />
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 场景元素与道具 -->
          <a-card title="场景元素与道具" :bordered="false">
            <template #extra>
              <a-button type="primary">
                <template #icon><plus-outlined /></template>
                添加元素
              </a-button>
            </template>
            <a-empty v-if="sceneElements.length === 0" description="暂无场景元素" />
          </a-card>
        </a-space>
      </a-col>

      <a-col :span="8">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 场景视觉预览 -->
          <a-card title="场景视觉预览" :bordered="false">
            <div class="scene-preview">
              <a-empty description="暂无预览图" />
              <a-button type="primary" block style="margin-top: 16px">
                <template #icon><camera-outlined /></template>
                上传图片
              </a-button>
            </div>
          </a-card>

          <!-- 关联角色 -->
          <a-card title="关联角色" :bordered="false">
            <a-button type="dashed" block>
              <template #icon><plus-outlined /></template>
              添加关联角色
            </a-button>
          </a-card>
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  SaveOutlined,
  CopyOutlined,
  PlusOutlined,
  CameraOutlined,
} from '@ant-design/icons-vue';

const sceneElements = ref([]);
</script>

<style scoped>
.scene-page {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.page-description {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.scene-preview {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
</style>
