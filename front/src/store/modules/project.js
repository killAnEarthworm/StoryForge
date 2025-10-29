import { projectApi } from '../../http/api';

export default {
  namespaced: true,
  state: {
    projects: [],
    currentProject: null,
    loading: false,
  },
  mutations: {
    SET_PROJECTS(state, projects) {
      state.projects = projects;
    },
    SET_CURRENT_PROJECT(state, project) {
      state.currentProject = project;
    },
    ADD_PROJECT(state, project) {
      state.projects.push(project);
    },
    UPDATE_PROJECT(state, project) {
      const index = state.projects.findIndex(p => p.id === project.id);
      if (index !== -1) {
        state.projects.splice(index, 1, project);
      }
    },
    DELETE_PROJECT(state, id) {
      state.projects = state.projects.filter(p => p.id !== id);
    },
    SET_LOADING(state, loading) {
      state.loading = loading;
    },
  },
  actions: {
    // 获取所有项目
    async fetchProjects({ commit }) {
      try {
        commit('SET_LOADING', true);
        const response = await projectApi.getProjects();
        commit('SET_PROJECTS', response.data.data || []);
      } catch (error) {
        console.error('获取项目列表失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 创建项目
    async createProject({ commit }, project) {
      try {
        commit('SET_LOADING', true);
        const response = await projectApi.createProject(project);
        const newProject = response.data.data;
        commit('ADD_PROJECT', newProject);
        return newProject;
      } catch (error) {
        console.error('创建项目失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 更新项目
    async updateProject({ commit }, { id, data }) {
      try {
        commit('SET_LOADING', true);
        const response = await projectApi.updateProject(id, data);
        const updated = response.data.data;
        commit('UPDATE_PROJECT', updated);
        return updated;
      } catch (error) {
        console.error('更新项目失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 删除项目
    async deleteProject({ commit }, id) {
      try {
        commit('SET_LOADING', true);
        await projectApi.deleteProject(id);
        commit('DELETE_PROJECT', id);
      } catch (error) {
        console.error('删除项目失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 设置当前项目
    setCurrentProject({ commit }, project) {
      commit('SET_CURRENT_PROJECT', project);
    },
  },
  getters: {
    projects: state => state.projects,
    currentProject: state => state.currentProject,
    loading: state => state.loading,
  },
};
