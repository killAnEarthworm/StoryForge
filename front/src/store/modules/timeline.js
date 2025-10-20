export default {
  namespaced: true,
  state: {
    currentTimeline: null,
    timelines: [],
    events: [],
  },
  mutations: {
    SET_CURRENT_TIMELINE(state, timeline) {
      state.currentTimeline = timeline;
    },
    SET_EVENTS(state, events) {
      state.events = events;
    },
  },
  actions: {},
  getters: {},
};
