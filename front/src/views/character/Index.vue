<template>
  <div class="character-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">角色设定</h1>
        <p class="page-description">创建和管理你的故事角色，定义他们的特征、背景和发展</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" size="large" @click="saveCharacter" :loading="loading">
          <template #icon><save-outlined /></template>
          保存角色
        </a-button>
        <a-button size="large">
          <template #icon><download-outlined /></template>
          导出角色卡
        </a-button>
        <a-button size="large" @click="addNewCharacter">
          <template #icon><plus-outlined /></template>
          新建角色
        </a-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-input-search
          placeholder="搜索角色..."
          size="large"
          style="max-width: 400px"
      />
    </div>

    <!-- 角色卡片列表（上方） -->
    <div class="character-list-wrapper">
      <a-row :gutter="16" wrap="false" class="character-list" style="overflow-x:auto; flex-wrap: nowrap;">
        <a-col
            v-for="(char, index) in characterList"
            :key="char.id || index"
            :span="6"
            class="character-card-col"
        >
          <a-card
              hoverable
              class="character-preview-card"
              :class="{ active: selectedCharacterIndex === index }"
              @click="selectCharacter(index)"
          >
            <div class="character-card-inner">
              <a-avatar :size="100" icon="user" />
              <h3 class="character-name">{{ char.name || '未命名角色' }}</h3>
              <p class="character-basic-info">
                {{ char.age || '未知年龄' }} | {{ char.occupation || '未知职业' }}
              </p>
              <div class="traits-container">
                <a-tag
                    v-for="trait in (char.personalityTraits || []).slice(0, 3)"
                    :key="trait"
                    color="blue"
                    size="small"
                >
                  {{ trait }}
                </a-tag>
                <a-tag v-if="!char.personalityTraits || char.personalityTraits.length === 0" color="default">待添加</a-tag>
              </div>
              <a-button
                  danger
                  size="small"
                  style="margin-top: 8px"
                  @click.stop="deleteCharacterConfirm(char.id)"
              >
                删除
              </a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 角色设定表单（下方） -->
    <div v-if="currentCharacter" class="character-form-section">
      <a-space direction="vertical" :size="24" style="width: 100%">
        <!-- 基础信息层 -->
        <a-card title="基础信息层" :bordered="false">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="姓名">
                <a-input v-model:value="currentCharacter.name" placeholder="输入角色姓名" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="年龄">
                <a-input-number v-model:value="currentCharacter.age" placeholder="输入角色年龄" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="性别">
                <a-select v-model="currentCharacter.gender" placeholder="请选择">
                  <a-select-option value="男">男</a-select-option>
                  <a-select-option value="女">女</a-select-option>
                  <a-select-option value="其他">其他</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="职业">
                <a-input v-model:value="currentCharacter.occupation" placeholder="输入角色职业" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="外貌特征">
                <a-textarea
                    v-model:value="currentCharacter.appearance"
                    :rows="3"
                    placeholder="描述角色的外貌特征"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="核心性格特征">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="trait in currentCharacter.personalityTraits || []"
                        :key="trait"
                        closable
                        @close="removeArrayItem(currentCharacter.personalityTraits, trait)"
                        color="blue"
                    >
                      {{ trait }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newTrait"
                        placeholder="添加性格特征关键词"
                        @press-enter="addTrait"
                        style="width: calc(100% - 200px)"
                    />
                    <a-button type="primary" @click="addTrait">
                      <template #icon><plus-outlined /></template>
                      添加
                    </a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>

        <!-- 深度设定层 -->
        <a-card title="深度设定层" :bordered="false">
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="背景故事">
                <a-textarea
                    v-model:value="currentCharacter.backgroundStory"
                    :rows="5"
                    placeholder="角色的背景故事"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="童年经历">
                <a-textarea
                    v-model:value="currentCharacter.childhoodExperience"
                    :rows="3"
                    placeholder="角色的童年经历"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="价值观信念">
                <a-textarea
                    v-model:value="currentCharacter.valuesBeliefs"
                    :rows="3"
                    placeholder="角色的价值观和信念"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="恐惧">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="fear in currentCharacter.fears || []"
                        :key="fear"
                        closable
                        @close="removeArrayItem(currentCharacter.fears, fear)"
                        color="red"
                    >
                      {{ fear }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newFear"
                        placeholder="添加恐惧"
                        @press-enter="addFear"
                        style="width: calc(100% - 100px)"
                    />
                    <a-button @click="addFear">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="欲望">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="desire in currentCharacter.desires || []"
                        :key="desire"
                        closable
                        @close="removeArrayItem(currentCharacter.desires, desire)"
                        color="orange"
                    >
                      {{ desire }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newDesire"
                        placeholder="添加欲望"
                        @press-enter="addDesire"
                        style="width: calc(100% - 100px)"
                    />
                    <a-button @click="addDesire">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="目标">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="goal in currentCharacter.goals || []"
                        :key="goal"
                        closable
                        @close="removeArrayItem(currentCharacter.goals, goal)"
                        color="green"
                    >
                      {{ goal }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newGoal"
                        placeholder="添加目标"
                        @press-enter="addGoal"
                        style="width: calc(100% - 100px)"
                    />
                    <a-button @click="addGoal">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
        <!-- 行为特征层 -->
        <a-card title="行为特征层" :bordered="false">
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="说话方式">
                <a-textarea
                    v-model:value="currentCharacter.speechPattern"
                    :rows="3"
                    placeholder="描述角色的说话方式和语言习惯"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="行为习惯">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="habit in currentCharacter.behavioralHabits || []"
                        :key="habit"
                        closable
                        @close="removeArrayItem(currentCharacter.behavioralHabits, habit)"
                    >
                      {{ habit }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newHabit"
                        placeholder="添加行为习惯"
                        @press-enter="addHabit"
                        style="width: calc(100% - 100px)"
                    />
                    <a-button @click="addHabit">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="口癖">
                <div class="traits-input-group">
                  <a-space wrap>
                    <a-tag
                        v-for="phrase in currentCharacter.catchphrases || []"
                        :key="phrase"
                        closable
                        @close="removeArrayItem(currentCharacter.catchphrases, phrase)"
                    >
                      {{ phrase }}
                    </a-tag>
                  </a-space>
                  <a-input-group compact style="margin-top: 8px;">
                    <a-input
                        v-model:value="newCatchphrase"
                        placeholder="添加口癖"
                        @press-enter="addCatchphrase"
                        style="width: calc(100% - 100px)"
                    />
                    <a-button @click="addCatchphrase">添加</a-button>
                  </a-input-group>
                </div>
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { message, Modal } from 'ant-design-vue'
import {
  SaveOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'


// 多角色数据
const store = useStore()

// 从store读取数据
const characterList = computed(() => store.getters['character/characters'])
const loading = computed(() => store.getters['character/loading'])
const currentProjectId = computed(() => store.getters.currentProjectId)

const selectedCharacterIndex = ref(0)
const currentCharacter = computed(() => characterList.value[selectedCharacterIndex.value])

// 各种输入框的临时值
const newTrait = ref('')
const newFear = ref('')
const newDesire = ref('')
const newGoal = ref('')
const newHabit = ref('')
const newCatchphrase = ref('')

// 初始化时加载数据
onMounted(async () => {
  if (!currentProjectId.value) {
    message.warning('请先在侧边栏选择一个项目')
    return
  }
  try {
    await store.dispatch('character/fetchCharacters')
  } catch (error) {
    message.error('加载角色列表失败')
  }
})

// 选择角色
const selectCharacter = (index) => {
  selectedCharacterIndex.value = index
}

// 通用的数组项移除方法
const removeArrayItem = (array, item) => {
  if (!array) return
  const index = array.indexOf(item)
  if (index > -1) {
    array.splice(index, 1)
  }
}

// 添加性格特征
const addTrait = () => {
  if (newTrait.value && currentCharacter.value) {
    if (!currentCharacter.value.personalityTraits) {
      currentCharacter.value.personalityTraits = []
    }
    if (!currentCharacter.value.personalityTraits.includes(newTrait.value)) {
      currentCharacter.value.personalityTraits.push(newTrait.value)
      newTrait.value = ''
    }
  }
}

// 保存角色
const saveCharacter = async () => {
  if (!currentCharacter.value) {
    message.warning('请先选择一个角色')
    return
  }

  if (!currentCharacter.value.name) {
    message.warning('角色姓名不能为空')
    return
  }

  try {
    if (currentCharacter.value.id) {
      // 更新现有角色
      await store.dispatch('character/updateCharacter', {
        id: currentCharacter.value.id,
        data: currentCharacter.value
      })
      message.success('角色保存成功')
    } else {
      // 创建新角色
      await store.dispatch('character/createCharacter', currentCharacter.value)
      message.success('角色创建成功')
      // 刷新列表
      await store.dispatch('character/fetchCharacters')
    }
  } catch (error) {
    message.error('保存失败: ' + (error.message || '未知错误'))
  }
}

// 新建角色
const addNewCharacter = () => {
  if (!currentProjectId.value) {
    message.warning('请先在侧边栏选择一个项目')
    return
  }

  const newChar = {
    name: '',
    age: null,
    gender:'',
    occupation: '',
    appearance: '',
    personalityTraits: [],
    backgroundStory: '',
    childhoodExperience: '',
    valuesBeliefs: '',
    fears: [],
    desires: [],
    goals: [],
    speechPattern: '',
    behavioralHabits: [],
    catchphrases: [],
  }

  characterList.value.push(newChar)
  selectedCharacterIndex.value = characterList.value.length - 1
}

// 删除角色确认
const deleteCharacterConfirm = (id) => {
  if (!id) {
    // 如果是未保存的新角色，直接从列表删除
    characterList.value.splice(selectedCharacterIndex.value, 1)
    if (selectedCharacterIndex.value >= characterList.value.length) {
      selectedCharacterIndex.value = Math.max(0, characterList.value.length - 1)
    }
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个角色吗？此操作不可恢复。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await store.dispatch('character/deleteCharacter', id)
        message.success('角色删除成功')
        if (selectedCharacterIndex.value >= characterList.value.length) {
          selectedCharacterIndex.value = Math.max(0, characterList.value.length - 1)
        }
      } catch (error) {
        message.error('删除失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 添加恐惧
const addFear = () => {
  if (newFear.value && currentCharacter.value) {
    if (!currentCharacter.value.fears) {
      currentCharacter.value.fears = []
    }
    if (!currentCharacter.value.fears.includes(newFear.value)) {
      currentCharacter.value.fears.push(newFear.value)
      newFear.value = ''
    }
  }
}

// 添加欲望
const addDesire = () => {
  if (newDesire.value && currentCharacter.value) {
    if (!currentCharacter.value.desires) {
      currentCharacter.value.desires = []
    }
    if (!currentCharacter.value.desires.includes(newDesire.value)) {
      currentCharacter.value.desires.push(newDesire.value)
      newDesire.value = ''
    }
  }
}

// 添加目标
const addGoal = () => {
  if (newGoal.value && currentCharacter.value) {
    if (!currentCharacter.value.goals) {
      currentCharacter.value.goals = []
    }
    if (!currentCharacter.value.goals.includes(newGoal.value)) {
      currentCharacter.value.goals.push(newGoal.value)
      newGoal.value = ''
    }
  }
}

// 添加行为习惯
const addHabit = () => {
  if (newHabit.value && currentCharacter.value) {
    if (!currentCharacter.value.behavioralHabits) {
      currentCharacter.value.behavioralHabits = []
    }
    if (!currentCharacter.value.behavioralHabits.includes(newHabit.value)) {
      currentCharacter.value.behavioralHabits.push(newHabit.value)
      newHabit.value = ''
    }
  }
}

// 添加口癖
const addCatchphrase = () => {
  if (newCatchphrase.value && currentCharacter.value) {
    if (!currentCharacter.value.catchphrases) {
      currentCharacter.value.catchphrases = []
    }
    if (!currentCharacter.value.catchphrases.includes(newCatchphrase.value)) {
      currentCharacter.value.catchphrases.push(newCatchphrase.value)
      newCatchphrase.value = ''
    }
  }
}

</script>

<style scoped>
.character-page {
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.header-actions{
  display: flex;
  gap: 8px;
}

.character-list-wrapper {
  margin-bottom: 24px;
  margin-top: 24px;
}

.character-card-col {
  min-width: 220px;
}

.character-preview-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.character-preview-card.active {
  border: 2px solid #1890ff;
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
}

.character-card-inner {
  text-align: center;
}

.character-name {
  font-size: 18px;
  font-weight: 600;
  margin: 12px 0 4px;
}

.character-basic-info {
  color: #6b7280;
  font-size: 14px;
}

.traits-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.character-form-section {
  margin-top: 32px;
}
</style>
