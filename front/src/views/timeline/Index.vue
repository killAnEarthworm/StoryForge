<template>
  <div class="timeline-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">时间线管理</h1>
        <p class="page-description">创建和管理角色的关键事件与时间节点</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large">
          <template #icon><plus-outlined /></template>
          添加事件
        </a-button>
        <a-button size="large">
          <template #icon><filter-outlined /></template>
          筛选
        </a-button>
      </div>
    </div>

    <!-- 时间线概览 -->
    <a-row :gutter="24" style="margin-bottom: 24px">
      <a-col :span="8">
        <a-card :bordered="false">
          <a-statistic title="总事件数" :value="24" suffix="个">
            <template #prefix><calendar-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card :bordered="false">
          <a-statistic title="关键转折点" :value="5" suffix="个">
            <template #prefix><star-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card :bordered="false">
          <a-statistic title="时间跨度" :value="15" suffix="年">
            <template #prefix><clock-circle-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 时间线内容 -->
    <a-card title="时间线事件" :bordered="false">
      <a-timeline>
        <a-timeline-item color="blue">
          <h3>离开余杭镇 <a-tag color="blue">18岁</a-tag></h3>
          <p>为救婶婶,李逍遥初次离开家乡余杭镇,前往仙灵岛求药,从此踏上冒险之旅。</p>
        </a-timeline-item>
        <a-timeline-item color="green">
          <h3>结识林月如 <a-tag color="green">18岁</a-tag></h3>
          <p>在比武招亲中击败林月如,被迫与其同行。两人从互相敌视到逐渐产生情愫。</p>
        </a-timeline-item>
        <a-timeline-item color="red">
          <h3>锁妖塔救灵儿 <a-tag color="red">19岁</a-tag></h3>
          <p>为救被囚禁在锁妖塔中的赵灵儿,李逍遥与林月如一同闯入。</p>
        </a-timeline-item>
      </a-timeline>
    </a-card>
  </div>
</template>

<script setup>
import {
  PlusOutlined,
  FilterOutlined,
  CalendarOutlined,
  StarOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue';
</script>

<style scoped>
.timeline-page {
  max-width: 1400px;
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
</style>
