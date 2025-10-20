export default {
  namespaced: true,
  state: {
    currentWorldview: null,
    worldviews: [],
  },
  mutations: {
    SET_CURRENT_WORLDVIEW(state, worldview) {
      state.currentWorldview = worldview;
    },
    SET_WORLDVIEWS(state, worldviews) {
      state.worldviews = worldviews;
    },
  },
  actions: {
    async fetchWorldviews({ commit }) {
      // TODO: API调用
    },
  },
  getters: {
    currentWorldview: state => state.currentWorldview,
  },
};
