import { createStore } from 'vuex';
import project from './modules/project';
import character from './modules/character';
import worldview from './modules/worldview';
import timeline from './modules/timeline';
import scene from './modules/scene';
import story from './modules/story';
import inspiration from './modules/inspiration';

export default createStore({
  state: {
    loading: false,
    user: null,
    currentProjectId: null, // 当前选中的项目ID
    projects: [], // 项目列表
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = loading;
    },
    SET_USER(state, user) {
      state.user = user;
    },
    SET_CURRENT_PROJECT_ID(state, projectId) {
      state.currentProjectId = projectId;
    },
    SET_PROJECTS(state, projects) {
      state.projects = projects;
    },
  },
  actions: {
    setLoading({ commit }, loading) {
      commit('SET_LOADING', loading);
    },
    setUser({ commit }, user) {
      commit('SET_USER', user);
    },
    setCurrentProjectId({ commit }, projectId) {
      commit('SET_CURRENT_PROJECT_ID', projectId);
    },
    setProjects({ commit }, projects) {
      commit('SET_PROJECTS', projects);
    },
  },
  getters: {
    currentProjectId: (state) => state.currentProjectId,
    projects: (state) => state.projects,
  },
  modules: {
    project,
    character,
    worldview,
    timeline,
    scene,
    story,
    inspiration,
  },
});
