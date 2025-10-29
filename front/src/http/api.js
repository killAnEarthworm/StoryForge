import axiosInstance from './index'

/**
 * API接口封装
 * 基于后端REST API设计
 */

// ==================== 项目相关API ====================
export const projectApi = {
  // 获取所有项目
  getProjects: () => axiosInstance.get('/api/projects'),

  // 获取单个项目
  getProject: (id) => axiosInstance.get(`/api/projects/${id}`),

  // 创建项目
  createProject: (data) => axiosInstance.post('/api/projects', data),

  // 更新项目
  updateProject: (id, data) => axiosInstance.put(`/api/projects/${id}`, data),

  // 删除项目
  deleteProject: (id) => axiosInstance.delete(`/api/projects/${id}`),

  // 按状态获取项目
  getProjectsByStatus: (status) => axiosInstance.get(`/api/projects/status/${status}`),

  // 按类型获取项目
  getProjectsByGenre: (genre) => axiosInstance.get(`/api/projects/genre/${genre}`),

  // 搜索项目
  searchProjects: (name) => axiosInstance.get('/api/projects/search', { params: { name } }),
};

// ==================== 角色相关API ====================
export const characterApi = {
  // 获取角色列表
  getCharacters: (params) => axiosInstance.get('/api/characters', { params }),

  // 获取单个角色
  getCharacter: (id) => axiosInstance.get(`/api/characters/${id}`),

  // 创建角色
  createCharacter: (data) => axiosInstance.post('/api/characters', data),

  // 更新角色
  updateCharacter: (id, data) => axiosInstance.put(`/api/characters/${id}`, data),

  // 删除角色
  deleteCharacter: (id) => axiosInstance.delete(`/api/characters/${id}`),

  // AI生成角色
  generateCharacter: (data) => axiosInstance.post('/api/characters/generate', data),

  // 导出角色卡
  exportCharacter: (id) => axiosInstance.get(`/api/characters/${id}/export`, { responseType: 'blob' }),
};

// ==================== 世界观相关API ====================
export const worldviewApi = {
  getWorldviews: (params) => axiosInstance.get('/api/worldviews', { params }),
  getWorldview: (id) => axiosInstance.get(`/api/worldviews/${id}`),
  createWorldview: (data) => axiosInstance.post('/api/worldviews', data),
  updateWorldview: (id, data) => axiosInstance.put(`/api/worldviews/${id}`, data),
  deleteWorldview: (id) => axiosInstance.delete(`/api/worldviews/${id}`),
  generateWorldview: (data) => axiosInstance.post('/api/worldviews/generate', data),
};

// ==================== 时间线相关API ====================
export const timelineApi = {
  getTimelines: (params) => axiosInstance.get('/api/timelines', { params }),
  getTimeline: (id) => axiosInstance.get(`/api/timelines/${id}`),
  createTimeline: (data) => axiosInstance.post('/api/timelines', data),
  updateTimeline: (id, data) => axiosInstance.put(`/api/timelines/${id}`, data),
  deleteTimeline: (id) => axiosInstance.delete(`/api/timelines/${id}`),

  // 时间线事件相关
  getTimelineEvents: (timelineId) => axiosInstance.get(`/api/timelines/${timelineId}/events`),
  createEvent: (timelineId, data) => axiosInstance.post(`/api/timelines/${timelineId}/events`, data),
  updateEvent: (timelineId, eventId, data) => axiosInstance.put(`/api/timelines/${timelineId}/events/${eventId}`, data),
  deleteEvent: (timelineId, eventId) => axiosInstance.delete(`/api/timelines/${timelineId}/events/${eventId}`),

  // 检测悖论
  detectParadox: (timelineId) => axiosInstance.post(`/api/timelines/${timelineId}/detect-paradox`),
};

// ==================== 场景相关API ====================
export const sceneApi = {
  getScenes: (params) => axiosInstance.get('/api/scenes', { params }),
  getScene: (id) => axiosInstance.get(`/api/scenes/${id}`),
  createScene: (data) => axiosInstance.post('/api/scenes', data),
  updateScene: (id, data) => axiosInstance.put(`/api/scenes/${id}`, data),
  deleteScene: (id) => axiosInstance.delete(`/api/scenes/${id}`),
  generateScene: (data) => axiosInstance.post('/api/scenes/generate', data),
};

// ==================== 故事生成相关API ====================
export const storyApi = {
  // 获取故事列表
  getStories: (params) => axiosInstance.get('/api/stories', { params }),
  getStory: (id) => axiosInstance.get(`/api/stories/${id}`),

  // 生成故事
  generateStory: (data) => axiosInstance.post('/api/stories/generate', data),

  // 章节相关
  getChapters: (storyId) => axiosInstance.get(`/api/stories/${storyId}/chapters`),
  generateChapter: (storyId, data) => axiosInstance.post(`/api/stories/${storyId}/chapters/generate`, data),
  updateChapter: (storyId, chapterId, data) => axiosInstance.put(`/api/stories/${storyId}/chapters/${chapterId}`, data),

  // 对话生成
  generateDialogue: (data) => axiosInstance.post('/api/stories/generate-dialogue', data),

  // 质量评估
  evaluateQuality: (storyId) => axiosInstance.post(`/api/stories/${storyId}/evaluate`),

  // 导出故事
  exportStory: (id, format) => axiosInstance.get(`/api/stories/${id}/export`, {
    params: { format },
    responseType: 'blob'
  }),
};

// ==================== 灵感相关API ====================
export const inspirationApi = {
  // 获取灵感推荐
  getInspirations: (params) => axiosInstance.get('/api/inspirations', { params }),

  // 趋势分析
  getTrends: () => axiosInstance.get('/api/inspirations/trends'),

  // 创意组合
  combineIdeas: (data) => axiosInstance.post('/api/inspirations/combine', data),

  // 主题推荐
  recommendThemes: (params) => axiosInstance.get('/api/inspirations/themes', { params }),
};

// ==================== 用户相关API ====================
export const userApi = {
  login: (data) => axiosInstance.post('/api/auth/login', data),
  register: (data) => axiosInstance.post('/api/auth/register', data),
  logout: () => axiosInstance.post('/api/auth/logout'),
  getUserInfo: () => axiosInstance.get('/api/user/info'),
  updateUserInfo: (data) => axiosInstance.put('/api/user/info', data),
};

// ==================== 通用文件上传API ====================
export const uploadApi = {
  uploadImage: (file, onProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosInstance.post('/api/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress,
    });
  },
  uploadDocument: (file, onProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosInstance.post('/api/upload/document', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress,
    });
  },
};

// 导出默认API对象
export default {
  project: projectApi,
  character: characterApi,
  worldview: worldviewApi,
  timeline: timelineApi,
  scene: sceneApi,
  story: storyApi,
  inspiration: inspirationApi,
  user: userApi,
  upload: uploadApi,
};
