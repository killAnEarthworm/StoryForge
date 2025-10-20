<template>
  <div class="worldview-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">世界观设定</h1>
        <p class="page-description">定义你的世界基础规则、社会结构和历史背景</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large">
          <template #icon><save-outlined /></template>
          保存世界观
        </a-button>
        <a-button size="large">
          <template #icon><download-outlined /></template>
          导出设定
        </a-button>
      </div>
    </div>

    <a-row :gutter="24">
      <a-col :span="16">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 宇宙法则 -->
          <a-card title="宇宙法则" :bordered="false">
            <template #extra>
              <a-button type="link"><robot-outlined /> AI辅助</a-button>
            </template>
            <a-form layout="vertical">
              <a-form-item label="基础物理/魔法规则">
                <a-textarea
                  :rows="4"
                  placeholder="描述这个世界的基本运行规则..."
                />
              </a-form-item>
              <a-form-item label="核心力量体系">
                <a-input placeholder="如:元素魔法、灵能等" />
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 社会结构 -->
          <a-card title="社会结构" :bordered="false">
            <template #extra>
              <a-button type="link"><robot-outlined /> AI辅助</a-button>
            </template>
            <a-form layout="vertical">
              <a-form-item label="政治体制">
                <a-input placeholder="如:君主专制、民主共和、神权统治等" />
              </a-form-item>
              <a-form-item label="社会阶层划分">
                <a-textarea :rows="3" placeholder="描述社会中的主要阶层..." />
              </a-form-item>
              <a-form-item label="经济体系">
                <a-input placeholder="如:货币经济、物物交换等" />
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 地理环境 -->
          <a-card title="地理环境" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="世界地图(可选)">
                <a-upload-dragger>
                  <p class="ant-upload-drag-icon"><inbox-outlined /></p>
                  <p class="ant-upload-text">点击或拖拽上传地图</p>
                </a-upload-dragger>
              </a-form-item>
            </a-form>
          </a-card>
        </a-space>
      </a-col>

      <a-col :span="8">
        <a-space direction="vertical" :size="24" style="width: 100%">
          <!-- 历史背景 -->
          <a-card title="历史背景" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="当前时代">
                <a-input placeholder="如:魔法复兴时代、星际殖民时期" />
              </a-form-item>
            </a-form>
          </a-card>

          <!-- 专有名词词典 -->
          <a-card title="专有名词词典" :bordered="false">
            <a-input-search placeholder="搜索专有名词..." style="margin-bottom: 16px" />
            <a-button type="dashed" block>
              <template #icon><plus-outlined /></template>
              添加新名词
            </a-button>
          </a-card>
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { SaveOutlined, DownloadOutlined, RobotOutlined, InboxOutlined, PlusOutlined } from '@ant-design/icons-vue';
</script>

<style scoped>
@import './style.css';
</style>
