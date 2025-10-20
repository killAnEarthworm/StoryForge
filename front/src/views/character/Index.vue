<template>
  <div class="character-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">角色设定</h1>
        <p class="page-description">创建和管理你的故事角色,定义他们的特征、背景和发展</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large">
          <template #icon><save-outlined /></template>
          保存角色
        </a-button>
        <a-button size="large">
          <template #icon><download-outlined /></template>
          导出角色卡
        </a-button>
        <a-button size="large">
          <template #icon><plus-outlined /></template>
          新建角色
        </a-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-input-search
        placeholder="搜索角色..."
        size="large"
        style="max-width: 400px"
      />
    </div>

    <!-- 主要内容区 -->
    <a-row :gutter="24">
      <!-- 左侧：角色预览卡片 -->
      <a-col :span="8">
        <a-card class="character-preview-card" :bordered="false">
          <div class="character-banner">
            <div class="character-avatar-wrapper">
              <a-avatar :size="120" :src="characterAvatar" />
            </div>
          </div>
          <div class="character-info">
            <h2 class="character-name">{{ characterData.name || '未命名角色' }}</h2>
            <p class="character-basic-info">
              {{ characterData.age || '未知年龄' }} | {{ characterData.occupation || '未知职业' }}
            </p>
          </div>

          <a-divider />

          <div class="character-traits-section">
            <h3 class="section-title">核心性格特征</h3>
            <div class="traits-container">
              <a-tag v-if="characterData.traits.length === 0" color="default">待添加</a-tag>
              <a-tag v-for="trait in characterData.traits" :key="trait" color="blue">
                {{ trait }}
              </a-tag>
            </div>
          </div>

          <a-divider />

          <div class="motivation-section">
            <h3 class="section-title">核心动机</h3>
            <p class="motivation-text">{{ characterData.motivation || '尚未设定' }}</p>
          </div>

          <a-button type="dashed" block size="large" class="ai-generate-btn" @click="showAIModal">
            <template #icon><robot-outlined /></template>
            AI辅助生成
          </a-button>
        </a-card>
      </a-col>

      <!-- 右侧：角色设定表单 -->
      <a-col :span="16">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 基础信息层 -->
          <a-card title="基础信息层" :bordered="false">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="姓名">
                  <a-input v-model:value="characterData.name" placeholder="输入角色姓名" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="年龄">
                  <a-input v-model:value="characterData.age" placeholder="输入角色年龄" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="性别">
                  <a-select v-model:value="characterData.gender" placeholder="请选择">
                    <a-select-option value="male">男</a-select-option>
                    <a-select-option value="female">女</a-select-option>
                    <a-select-option value="other">其他</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="职业">
                  <a-input v-model:value="characterData.occupation" placeholder="输入角色职业" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="外貌特征">
                  <a-textarea
                    v-model:value="characterData.appearance"
                    :rows="3"
                    placeholder="描述角色的外貌特征,如身高、体型、发型、衣着风格等"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="核心性格特征">
                  <div class="traits-input-group">
                    <a-space wrap>
                      <a-tag
                        v-for="trait in characterData.traits"
                        :key="trait"
                        closable
                        @close="removeTrait(trait)"
                        color="blue"
                      >
                        {{ trait }}
                      </a-tag>
                    </a-space>
                    <a-input-group compact style="margin-top: 8px">
                      <a-input
                        v-model:value="newTrait"
                        placeholder="添加性格特征关键词"
                        @press-enter="addTrait"
                        style="width: calc(100% - 80px)"
                      />
                      <a-button type="primary" @click="addTrait">
                        <template #icon><plus-outlined /></template>
                        添加
                      </a-button>
                    </a-input-group>
                  </div>
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>

          <!-- 深度设定层 -->
          <a-card title="深度设定层" :bordered="false">
            <a-row :gutter="16">
              <a-col :span="24">
                <a-form-item label="核心动机">
                  <a-input
                    v-model:value="characterData.motivation"
                    placeholder="角色行动的核心驱动力"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="弱点">
                  <a-input
                    v-model:value="characterData.weakness"
                    placeholder="角色的主要弱点或缺陷"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="背景故事">
                  <a-textarea
                    v-model:value="characterData.background"
                    :rows="5"
                    placeholder="角色的童年经历、重要人生事件等背景故事"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="价值观和信念体系">
                  <a-textarea
                    v-model:value="characterData.values"
                    :rows="3"
                    placeholder="角色的核心价值观、道德准则和信念"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="恐惧">
                  <a-input
                    v-model:value="characterData.fears"
                    placeholder="角色最害怕的事物或情境"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="欲望">
                  <a-input
                    v-model:value="characterData.desires"
                    placeholder="角色强烈渴望得到的事物"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>

          <!-- 动态属性层 -->
          <a-card title="动态属性层" :bordered="false">
            <template #extra>
              <a-button type="link">
                <template #icon><link-outlined /></template>
                关联时间线
              </a-button>
            </template>
            <a-row :gutter="16">
              <a-col :span="24">
                <a-form-item label="口癖/说话风格">
                  <a-input
                    v-model:value="characterData.speechStyle"
                    placeholder="角色独特的说话方式或常用语"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="行为习惯">
                  <a-textarea
                    v-model:value="characterData.behaviorHabits"
                    :rows="3"
                    placeholder="角色的习惯性动作或行为模式"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="关键关系">
                  <a-button type="dashed" block @click="addRelationship">
                    <template #icon><plus-outlined /></template>
                    添加关系
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>
        </a-space>
      </a-col>
    </a-row>

    <!-- AI辅助生成模态框 -->
    <a-modal
      v-model:open="aiModalVisible"
      title="AI辅助生成"
      width="600px"
      @ok="handleAIGenerate"
    >
      <a-form layout="vertical">
        <a-form-item label="角色关键词">
          <a-input
            v-model:value="aiKeywords"
            placeholder="例如:古风侠客、孤独、剑术高手、寻找真相"
          />
          <div class="form-tip">输入角色的核心特征关键词,AI将基于这些关键词生成完整角色设定</div>
        </a-form-item>
        <a-form-item label="角色类型">
          <a-select v-model:value="aiCharacterType" placeholder="请选择角色类型">
            <a-select-option value="warrior">战士/侠客</a-select-option>
            <a-select-option value="mage">法师/术士</a-select-option>
            <a-select-option value="rogue">盗贼/刺客</a-select-option>
            <a-select-option value="scholar">学者/智者</a-select-option>
            <a-select-option value="leader">领袖/统治者</a-select-option>
            <a-select-option value="commoner">平民/普通人</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="生成详细程度">
          <a-radio-group v-model:value="aiDetailLevel">
            <a-radio value="basic">基础</a-radio>
            <a-radio value="medium">中等</a-radio>
            <a-radio value="detailed">详细</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import {
  SaveOutlined,
  DownloadOutlined,
  PlusOutlined,
  RobotOutlined,
  LinkOutlined,
} from '@ant-design/icons-vue';

// 角色数据
const characterData = reactive({
  name: '',
  age: '',
  gender: '',
  occupation: '',
  appearance: '',
  traits: [],
  motivation: '',
  weakness: '',
  background: '',
  values: '',
  fears: '',
  desires: '',
  speechStyle: '',
  behaviorHabits: '',
});

const characterAvatar = ref('https://via.placeholder.com/120');
const newTrait = ref('');

// AI辅助生成
const aiModalVisible = ref(false);
const aiKeywords = ref('');
const aiCharacterType = ref('');
const aiDetailLevel = ref('medium');

const showAIModal = () => {
  aiModalVisible.value = true;
};

const handleAIGenerate = () => {
  // TODO: 调用AI生成API
  console.log('AI生成', { aiKeywords, aiCharacterType, aiDetailLevel });
  aiModalVisible.value = false;
};

// 添加性格特征
const addTrait = () => {
  if (newTrait.value && !characterData.traits.includes(newTrait.value)) {
    characterData.traits.push(newTrait.value);
    newTrait.value = '';
  }
};

// 删除性格特征
const removeTrait = (trait) => {
  const index = characterData.traits.indexOf(trait);
  if (index > -1) {
    characterData.traits.splice(index, 1);
  }
};

// 添加关系
const addRelationship = () => {
  // TODO: 打开关系添加对话框
  console.log('添加关系');
};
</script>

<style scoped>
.character-page {
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
  color: #0a0a0a;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6b7280;
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-bar {
  margin-bottom: 24px;
}

/* 角色预览卡片样式 */
.character-preview-card {
  background: #1890ff;
  border-radius: 16px;
  position: sticky;
  top: 88px;
}

:deep(.ant-card-body) {
  padding: 0;
}

.character-banner {
  height: 200px;
  background: #1890ff;
  border-radius: 16px 16px 0 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 20px;
  position: relative;
}

.character-avatar-wrapper {
  border-radius: 50%;
  background: #9ca3af;
  position: absolute;
  bottom: -60px;
}

.character-info {
  text-align: center;
  padding: 70px 24px 16px;
}

.character-name {
  font-size: 24px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.character-basic-info {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.character-traits-section,
.motivation-section {
  padding: 0 24px 16px;
}

.section-title {
  font-size: 12px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 600;
  margin-bottom: 12px;
}

.traits-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.motivation-text {
  color: #6b7280;
  font-style: italic;
  margin: 0;
}

.ai-generate-btn {
  margin: 0 24px 24px;
  border-color: #1890ff;
  color: #1890ff;
  font-weight: 500;
}

.ai-generate-btn:hover {
  border-color: #0050b3;
  color: #0050b3;
  background: #d1d5db;
}

/* 表单样式 */
:deep(.ant-card) {
  background: rgba(30, 27, 75, 0.4);
  border-radius: 12px;
}

:deep(.ant-card-head) {
  color: #ffffff;
}

:deep(.ant-card-head-title) {
  color: #ffffff;
  font-weight: 600;
}

:deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.85);
}

:deep(.ant-input),
:deep(.ant-input-number),
:deep(.ant-select-selector),
:deep(.ant-picker) {
  background: rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
}

:deep(.ant-input::placeholder),
:deep(.ant-input-number::placeholder),
:deep(.ant-select-selection-placeholder) {
  color: rgba(255, 255, 255, 0.3);
}

:deep(.ant-input:hover),
:deep(.ant-select-selector:hover) {
  border-color: #0050b3;
}

:deep(.ant-input:focus),
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #d946ef;
  box-shadow: 0 0 0 2px rgba(217, 70, 239, 0.2);
}

.form-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 4px;
}

.traits-input-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
