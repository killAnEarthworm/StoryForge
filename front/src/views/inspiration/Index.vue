<template>
  <div class="inspiration-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">灵感模块</h1>
        <p class="page-description">AI创意助手,帮助你突破创作瓶颈,发现新的故事可能性</p>
      </div>
    </div>

    <a-row :gutter="24">
      <a-col :span="24">
        <a-card :bordered="false">
          <a-empty description="灵感模块功能开发中..." />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.inspiration-page {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
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
</style>
