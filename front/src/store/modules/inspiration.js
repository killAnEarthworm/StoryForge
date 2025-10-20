export default {
  namespaced: true,
  state: {
    inspirations: [],
  },
  mutations: {
    SET_INSPIRATIONS(state, inspirations) {
      state.inspirations = inspirations;
    },
  },
  actions: {},
  getters: {},
};
