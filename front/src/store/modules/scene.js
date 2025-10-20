export default {
  namespaced: true,
  state: {
    currentScene: null,
    scenes: [],
  },
  mutations: {
    SET_CURRENT_SCENE(state, scene) {
      state.currentScene = scene;
    },
    SET_SCENES(state, scenes) {
      state.scenes = scenes;
    },
  },
  actions: {},
  getters: {},
};
