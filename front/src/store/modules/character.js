import { characterApi } from '../../http/api';

export default {
  namespaced: true,
  state: {
    currentCharacter: null,
    characters: [],
    loading: false,
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
    SET_LOADING(state, loading) {
      state.loading = loading;
    },
  },
  actions: {
    // 获取项目下的所有角色
    async fetchCharacters({ commit, rootGetters }) {
      const projectId = rootGetters.currentProjectId;
      if (!projectId) {
        console.warn('未选择项目，无法加载角色列表');
        return;
      }
      
      try {
        commit('SET_LOADING', true);
        const response = await characterApi.getCharacters({ projectId });
        // 后端返回格式：{ code: 200, message: "success", data: [...] }
        commit('SET_CHARACTERS', response.data.data || []);
      } catch (error) {
        console.error('获取角色列表失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 创建角色
    async createCharacter({ commit, rootGetters }, character) {
      const projectId = rootGetters.currentProjectId;
      if (!projectId) {
        throw new Error('未选择项目');
      }
      
      try {
        commit('SET_LOADING', true);
        const characterData = { ...character, projectId };
        const response = await characterApi.createCharacter(characterData);
        const newCharacter = response.data.data;
        commit('ADD_CHARACTER', newCharacter);
        return newCharacter;
      } catch (error) {
        console.error('创建角色失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 更新角色
    async updateCharacter({ commit }, { id, data }) {
      try {
        commit('SET_LOADING', true);
        const response = await characterApi.updateCharacter(id, data);
        const updated = response.data.data;
        commit('UPDATE_CHARACTER', updated);
        return updated;
      } catch (error) {
        console.error('更新角色失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 删除角色
    async deleteCharacter({ commit }, id) {
      try {
        commit('SET_LOADING', true);
        await characterApi.deleteCharacter(id);
        commit('DELETE_CHARACTER', id);
      } catch (error) {
        console.error('删除角色失败:', error);
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 设置当前角色
    setCurrentCharacter({ commit }, character) {
      commit('SET_CURRENT_CHARACTER', character);
    },
  },
  getters: {
    currentCharacter: state => state.currentCharacter,
    characters: state => state.characters,
    loading: state => state.loading,
  },
};
