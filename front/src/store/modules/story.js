export default {
  namespaced: true,
  state: {
    currentStory: null,
    stories: [],
    generationHistory: [],
  },
  mutations: {
    SET_CURRENT_STORY(state, story) {
      state.currentStory = story;
    },
    ADD_GENERATION_HISTORY(state, history) {
      state.generationHistory.unshift(history);
    },
  },
  actions: {},
  getters: {},
};
