export default {
  namespaced: true,
  state: {
    currentCharacter: null,
    characters: [],
  },
  mutations: {
    SET_CURRENT_CHARACTER(state, character) {
      state.currentCharacter = character;
    },
    SET_CHARACTERS(state, characters) {
      state.characters = characters;
    },
    ADD_CHARACTER(state, character) {
      state.characters.push(character);
    },
    UPDATE_CHARACTER(state, character) {
      const index = state.characters.findIndex(c => c.id === character.id);
      if (index !== -1) {
        state.characters.splice(index, 1, character);
      }
    },
    DELETE_CHARACTER(state, id) {
      state.characters = state.characters.filter(c => c.id !== id);
    },
  },
  actions: {
    async fetchCharacters({ commit }) {
      // TODO: 调用API获取角色列表
      // const characters = await api.getCharacters();
      // commit('SET_CHARACTERS', characters);
    },
    async createCharacter({ commit }, character) {
      // TODO: 调用API创建角色
      // const newCharacter = await api.createCharacter(character);
      // commit('ADD_CHARACTER', newCharacter);
    },
    async updateCharacter({ commit }, character) {
      // TODO: 调用API更新角色
      // const updated = await api.updateCharacter(character);
      // commit('UPDATE_CHARACTER', updated);
    },
    async deleteCharacter({ commit }, id) {
      // TODO: 调用API删除角色
      // await api.deleteCharacter(id);
      // commit('DELETE_CHARACTER', id);
    },
  },
  getters: {
    currentCharacter: state => state.currentCharacter,
    characters: state => state.characters,
  },
};
