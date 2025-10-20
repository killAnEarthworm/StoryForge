import { createStore } from 'vuex';
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
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = loading;
    },
    SET_USER(state, user) {
      state.user = user;
    },
  },
  actions: {
    setLoading({ commit }, loading) {
      commit('SET_LOADING', loading);
    },
    setUser({ commit }, user) {
      commit('SET_USER', user);
    },
  },
  modules: {
    character,
    worldview,
    timeline,
    scene,
    story,
    inspiration,
  },
});
