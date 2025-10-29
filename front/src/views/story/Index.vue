<template>
  <div class="story-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">故事生成</h1>
        <p class="page-description">根据您的角色设定、世界观和场景,AI将帮助您生成符合设定的精彩故事内容</p>
      </div>
    </div>

    <!-- 生成模式选择 -->
    <a-card title="生成模式" :bordered="false" style="margin-bottom: 24px">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card class="mode-card" @click="selectMode('short')">
            <file-text-outlined class="mode-icon" />
            <h3>短篇模式</h3>
            <p>输入概要,AI生成一篇完整故事</p>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card class="mode-card" @click="selectMode('chapter')">
            <book-outlined class="mode-icon" />
            <h3>章节模式</h3>
            <p>先生成大纲,随后逐章生成,支持修改</p>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card class="mode-card" @click="selectMode('dialogue')">
            <message-outlined class="mode-icon" />
            <h3>对话模式</h3>
            <p>多个角色在场景中互动,生成符合性格的对话</p>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <a-row :gutter="24">
      <a-col :span="8">
        <!-- 设定集选择 -->
        <a-card title="设定集" :bordered="false">
          <a-form layout="vertical">
            <a-form-item label="角色">
              <a-select placeholder="选择角色...">
                <a-select-option value="1">李逍遥 - 仙侠世界</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="世界观">
              <a-select placeholder="选择世界观...">
                <a-select-option value="1">仙侠世界 - 古风</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="场景">
              <a-select placeholder="选择场景...">
                <a-select-option value="1">桃花岛 - 仙侠世界</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <a-col :span="16">
        <!-- 故事概要输入 -->
        <a-card title="故事概要/灵感" :bordered="false">
          <a-textarea
            :rows="8"
            placeholder="请输入故事概要或灵感..."
            style="margin-bottom: 16px"
          />
          <div style="text-align: right">
            <a-button type="primary" size="large">
              <template #icon><robot-outlined /></template>
              生成故事
            </a-button>
          </div>
        </a-card>

        <!-- 生成结果 -->
        <a-card title="生成结果" :bordered="false" style="margin-top: 24px">
          <a-empty description="等待生成故事" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  FileTextOutlined,
  BookOutlined,
  MessageOutlined,
  RobotOutlined,
} from '@ant-design/icons-vue';

const selectedMode = ref('short');

const selectMode = (mode) => {
  selectedMode.value = mode;
};
</script>

<style scoped>
.story-page {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6b7280;
  margin: 0;
  font-size: 18px;
}

.mode-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.mode-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(217, 70, 239, 0.3);
}

.mode-icon {
  font-size: 48px;
  color: #d946ef;
  margin-bottom: 16px;
}

.mode-card h3 {
  margin: 12px 0 8px 0;
}

.mode-card p {
  color: #4b5563;
  margin: 0;
}
</style>
