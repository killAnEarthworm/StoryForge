# Week 2 Phase 1: 世界观AI生成系统实现文档

## 📋 概述

本文档记录了StoryForge项目第二周第一阶段的开发成果：**世界观AI生成系统**。该系统允许用户通过提供关键词和故事类型，自动生成完整、详细、内部一致的世界观设定。

### 实现范围

- ✅ 12种世界观类型枚举（奇幻、科幻、武侠、修真等）
- ✅ 世界观生成请求DTO
- ✅ AI驱动的世界观生成服务
- ✅ 世界观一致性验证器
- ✅ 5个REST API端点

### 代码统计

| 文件 | 行数 | 说明 |
|------|------|------|
| WorldviewGenre.java | 203 | 世界观类型枚举 |
| WorldviewGenerationRequest.java | 99 | 生成请求DTO |
| WorldviewGenerationService.java | 376 | 核心生成服务 |
| WorldviewConsistencyValidator.java | 354 | 一致性验证器 |
| WorldviewGenerationController.java | 210 | REST API控制器 |
| **总计** | **1242** | **5个新文件** |

---

## 🏗️ 架构设计

### 系统流程图

```
用户请求
    ↓
WorldviewGenerationController
    ↓
WorldviewGenerationService
    ├─→ buildWorldviewPrompt()          # 构建提示词
    ├─→ AiGenerationService.chat()      # 调用AI
    ├─→ parseWorldviewResponse()        # 解析响应
    └─→ WorldviewService.create()       # 保存数据库
    ↓
返回WorldviewDTO
    ↓
（可选）WorldviewConsistencyValidator  # 后续内容验证
```

### 核心组件

#### 1. WorldviewGenre（枚举）

**位置**: `src/main/java/com/linyuan/storyforge/enums/WorldviewGenre.java`

**功能**: 定义12种世界观类型，每种类型有特定的生成要求

**支持的类型**:

| 代码 | 中文名称 | 核心特征 |
|------|----------|----------|
| `fantasy` | 奇幻 | 魔法体系、多种族、神话元素、古代文明 |
| `sci-fi` | 科幻 | 科技发展、太空探索、未来社会、人工智能 |
| `wuxia` | 武侠 | 江湖门派、武功体系、侠义精神、恩怨情仇 |
| `xianxia` | 修真 | 修炼境界、法宝丹药、宗门势力、天道规则 |
| `modern` | 现代 | 真实社会、现代科技、城市生活、当代文化 |
| `historical` | 历史 | 历史真实、时代特征、文化习俗、社会制度 |
| `mystery` | 悬疑 | 神秘氛围、环境细节、隐藏线索、心理描写 |
| `horror` | 恐怖 | 恐怖元素、压抑氛围、超自然现象、心理惊悚 |
| `post-apocalyptic` | 末世 | 废土环境、资源稀缺、生存法则、变异生物 |
| `cyberpunk` | 赛博朋克 | 高科技低生活、超级企业、虚拟现实、身体改造 |
| `steampunk` | 蒸汽朋克 | 蒸汽动力、维多利亚风格、机械美学、工业革命 |
| `urban-fantasy` | 都市异能 | 超能力体系、现代社会、隐秘组织、日常与非凡 |

**关键方法**:

```java
// 根据代码获取枚举
WorldviewGenre genre = WorldviewGenre.fromCode("fantasy");

// 获取类型的生成要求（用于提示词构建）
String requirements = genre.getGenerationRequirements();
```

#### 2. WorldviewGenerationRequest（DTO）

**位置**: `src/main/java/com/linyuan/storyforge/dto/WorldviewGenerationRequest.java`

**功能**: 封装世界观生成请求的所有参数

**核心字段**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `projectId` | UUID | ✅ | - | 所属项目ID |
| `genre` | WorldviewGenre | ✅ | - | 故事类型 |
| `keywords` | List&lt;String&gt; | ✅ | - | 关键词列表 |
| `worldScale` | String | ❌ | null | 世界规模（"单一星球"、"星系"等） |
| `powerLevel` | String | ❌ | null | 魔法/科技水平 |
| `civilizationStage` | String | ❌ | null | 文明发展阶段 |
| `additionalRequirements` | String | ❌ | null | 额外要求 |
| `includeDetailedGeography` | Boolean | ❌ | true | 是否生成详细地理 |
| `includeDetailedHistory` | Boolean | ❌ | true | 是否生成详细历史 |
| `includeTerminology` | Boolean | ❌ | true | 是否生成专有名词词典 |
| `creativity` | Double | ❌ | 0.8 | AI创意度（0.0-1.0） |

#### 3. WorldviewGenerationService（服务）

**位置**: `src/main/java/com/linyuan/storyforge/service/WorldviewGenerationService.java`

**功能**: 核心生成逻辑，负责提示词构建、AI调用、响应解析

**核心方法**:

##### generateWorldview()

```java
@Transactional
public WorldviewDTO generateWorldview(WorldviewGenerationRequest request)
```

**流程**:
1. 验证项目存在
2. 调用 `buildWorldviewPrompt()` 构建详细提示词
3. 调用 `aiService.chatWithOptions()` 进行AI生成
4. 调用 `parseWorldviewResponse()` 解析JSON响应
5. 补充必要字段（projectId、默认名称）
6. 调用 `worldviewService.createWorldview()` 保存数据库
7. 返回生成的WorldviewDTO

##### buildWorldviewPrompt()

**位置**: WorldviewGenerationService.java:86-229

**功能**: 构建多段式提示词，包含：

1. **角色定位**: "你是一位专业的世界观设计师..."
2. **任务说明**: "请基于以下信息，创建一个完整、详细、内部一致的世界观设定"
3. **输入信息**: 用户提供的所有参数（类型、关键词、世界规模等）
4. **类型特定要求**: 根据WorldviewGenre动态生成（例如奇幻需要"详细的魔法体系"）
5. **生成要求**: 详细说明需要生成的结构：
   - 基础信息（名称、概要）
   - 宇宙法则（physics, magic_or_tech, power_system）
   - 社会结构（politics, economy, culture, hierarchy）
   - 地理环境（可选，regions, climate, special_locations）
   - 历史背景（可选，origin, major_events, legends）
   - 规则与约束（8-12条规则，5-8条约束）
   - 专有名词词典（可选，15-20个名词）
6. **输出格式**: JSON Schema示例
7. **质量要求**: 内部一致性、细节丰富、逻辑合理、创意独特、可扩展性

**提示词长度**: 约1500-2500字符（取决于选项）

##### parseWorldviewResponse()

**位置**: WorldviewGenerationService.java:238-330

**功能**: 解析AI返回的JSON响应

**处理步骤**:
1. 清理Markdown代码块标记（```json...```）
2. 使用Jackson ObjectMapper解析JSON
3. 将各个JSON节点转换为DTO字段：
   - universeLaws → Map&lt;String, Object&gt;
   - socialStructure → Map&lt;String, Object&gt;
   - geography → Map&lt;String, Object&gt;
   - historyBackground → Map&lt;String, Object&gt;
   - terminology → Map&lt;String, Object&gt;
   - rules → List&lt;String&gt;
   - constraints → List&lt;String&gt;
4. 错误处理：如果解析失败，返回fallback DTO

##### generateMultipleWorldviews()

```java
@Transactional
public List<WorldviewDTO> generateMultipleWorldviews(WorldviewGenerationRequest request, int count)
```

**功能**: 批量生成多个世界观方案供用户选择

**策略**: 每次生成时逐渐提高创意度（creativity + i * 0.05），以获得更多样化的结果

#### 4. WorldviewConsistencyValidator（验证器）

**位置**: `src/main/java/com/linyuan/storyforge/validator/WorldviewConsistencyValidator.java`

**功能**: 验证生成的内容是否符合世界观设定

**核心方法**:

##### validateContent()

```java
public ConsistencyResult validateContent(
    UUID worldviewId,
    String generatedContent,
    ContentType contentType)
```

**验证维度**:

1. **规则验证** (`checkRulesCompliance`)
   - 检查禁止性规则（包含"禁止"、"不能"、"不允许"）
   - 提取被禁止的概念，检查内容中是否出现

2. **约束验证** (`checkConstraintsCompliance`)
   - 检查是否违反世界观约束条件

3. **物理规则验证** (`validatePhysicsCompliance`)
   - 检查是否违反世界观的物理规律设定

4. **术语验证** (`checkTerminologyUsage`)
   - 使用正则提取内容中的专有名词
   - 检查是否使用了未定义的专有名词

5. **整体得分计算** (`calculateOverallScore`)
   - 基础分1.0
   - 每个违规扣0.15分（最多扣0.6）
   - 物理规则不符合扣0.2分
   - 最终得分范围：0.0-1.0

6. **AI深度验证** (`performAIValidation`)
   - 当得分 < 0.7 或有违规时触发
   - 调用AI进行深度验证，提供修改建议

**验证结果**: 返回 `ConsistencyResult` 对象，包含：
- `passed`: 是否通过（得分≥0.7且无违规）
- `overallScore`: 整体得分
- `violations`: 违规列表
- `aiSuggestions`: AI验证建议

##### quickValidate()

```java
public boolean quickValidate(UUID worldviewId, String content)
```

**功能**: 快速验证，只检查规则和约束，不进行AI深度验证

#### 5. WorldviewGenerationController（控制器）

**位置**: `src/main/java/com/linyuan/storyforge/controller/WorldviewGenerationController.java`

**功能**: 提供REST API端点

---

## 🔌 REST API文档

### 基础URL

```
http://localhost:8080/api/worldviews
```

### API端点列表

#### 1. 生成世界观

**端点**: `POST /api/worldviews/generate`

**功能**: 基于用户输入生成单个世界观

**请求体**:

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "genre": "FANTASY",
  "keywords": ["龙", "魔法学院", "元素魔法"],
  "worldScale": "单一大陆",
  "powerLevel": "高魔",
  "civilizationStage": "中世纪",
  "additionalRequirements": "需要包含四大学院的设定",
  "includeDetailedGeography": true,
  "includeDetailedHistory": true,
  "includeTerminology": true,
  "creativity": 0.8
}
```

**响应** (HTTP 201 Created):

```json
{
  "code": 200,
  "message": "成功生成世界观: 艾泽拉斯魔法大陆",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "艾泽拉斯魔法大陆",
    "summary": "一个充满魔法元素的大陆，由四大魔法学院统治，龙族作为古老的守护者与人类共存...",
    "universeLaws": {
      "physics": "魔力是宇宙的基本粒子，可以通过咒语和符文操控",
      "magic_or_tech": "元素魔法体系，分为火、水、风、土四大元素...",
      "power_system": "魔法师分为学徒、法师、大法师、魔导师四个等级..."
    },
    "socialStructure": {
      "politics": "四大学院联盟统治，每个学院掌管一种元素",
      "economy": "魔石作为货币，魔法物品交易繁荣",
      "culture": "崇尚魔法教育，每个孩子10岁时接受魔法天赋测试",
      "hierarchy": "魔法师阶层 > 贵族 > 平民 > 无魔力者"
    },
    "geography": {
      "scale": "单一大陆，面积约1000万平方公里",
      "regions": [
        {"name": "火焰高原", "description": "火元素魔法学院所在地，终年高温"},
        {"name": "深海群岛", "description": "水元素魔法学院所在地，海洋文化发达"}
      ],
      "climate": "四季分明，但各地区受元素魔法影响气候差异极大",
      "special_locations": ["龙骨山脉", "元素交汇点", "禁忌之森"]
    },
    "historyBackground": {
      "origin": "传说中，四位上古龙神将元素之力注入大陆，创造了魔法",
      "major_events": [
        "纪元前1000年：龙神创世",
        "纪元0年：四大学院建立",
        "纪元500年：第一次元素战争"
      ],
      "legends": [
        "龙神归来预言：当四元素失衡时，龙神将再次降临"
      ],
      "current_era": "魔法黄金时代，但元素失衡的迹象开始显现"
    },
    "rules": [
      "每个人只能主修一种元素魔法",
      "禁止使用禁忌魔法（死灵术、灵魂操控）",
      "魔法师必须遵守学院法典",
      "龙族享有特殊地位，不受人类法律约束"
    ],
    "constraints": [
      "禁止跨元素魔法融合（会导致元素暴走）",
      "禁止伤害龙族",
      "禁止在圣地使用魔法",
      "未经授权不得进入元素交汇点"
    ],
    "terminology": {
      "魔石": "含有魔力的晶体，可作为货币或魔法材料",
      "元素交汇点": "四种元素魔法汇聚的神秘地点",
      "龙骨山脉": "龙族的栖息地，人类禁区",
      "法典": "魔法学院的最高法律"
    },
    "createdAt": "2025-10-27T10:30:00",
    "updatedAt": "2025-10-27T10:30:00"
  }
}
```

#### 2. 批量生成世界观

**端点**: `POST /api/worldviews/generate/batch?count=3`

**功能**: 批量生成多个世界观方案供用户选择

**查询参数**:
- `count` (int, 可选): 生成数量，默认3，范围1-5

**请求体**: 与单个生成相同

**响应** (HTTP 201 Created):

```json
{
  "code": 200,
  "message": "成功生成 3 个世界观方案",
  "data": [
    { "id": "...", "name": "艾泽拉斯魔法大陆", ... },
    { "id": "...", "name": "元素之心世界", ... },
    { "id": "...", "name": "龙息帝国", ... }
  ]
}
```

#### 3. 验证内容一致性

**端点**: `POST /api/worldviews/{worldviewId}/validate`

**功能**: 验证生成的内容是否符合世界观设定

**路径参数**:
- `worldviewId` (UUID): 世界观ID

**请求体**:

```json
{
  "content": "在艾泽拉斯，主角学会了同时使用火焰和冰霜魔法...",
  "contentType": "NARRATIVE"
}
```

**ContentType枚举值**:
- `CHARACTER` - 角色描述
- `DIALOGUE` - 对话
- `SCENE` - 场景
- `NARRATIVE` - 叙述
- `ACTION` - 动作
- `INNER_MONOLOGUE` - 内心独白
- `CHAPTER` - 章节
- `WORLDVIEW` - 世界观

**响应** (HTTP 200 OK):

```json
{
  "code": 200,
  "message": "验证未通过 - 得分: 0.55，发现 1 个问题",
  "data": {
    "passed": false,
    "overallScore": 0.55,
    "violations": [
      "违反约束: 禁止跨元素魔法融合（会导致元素暴走） (内容中出现了被禁止的概念)"
    ],
    "validationLevel": "STRICT",
    "speechPatternValid": true,
    "aiSuggestions": "内容违反了世界观的核心约束。建议修改：主角可以精通一种元素魔法，但若要使用另一种，需要通过特殊物品（如双元素法杖）或付出代价（如身体负荷）。建议改为：主角掌握火焰魔法，但借助龙骨山脉的冰龙之力，短暂使用冰霜能力..."
  }
}
```

#### 4. 快速验证

**端点**: `GET /api/worldviews/{worldviewId}/quick-validate`

**功能**: 快速验证，只检查规则和约束，不进行AI深度分析

**查询参数**:
- `content` (String, 必填): 要验证的内容

**响应** (HTTP 200 OK):

```json
{
  "code": 200,
  "message": "验证未通过",
  "data": {
    "worldviewId": "123e4567-e89b-12d3-a456-426614174000",
    "passed": false,
    "message": "快速验证未通过，请使用完整验证获取详情"
  }
}
```

#### 5. 获取所有世界观类型

**端点**: `GET /api/worldviews/genres`

**功能**: 获取所有支持的世界观类型列表

**响应** (HTTP 200 OK):

```json
{
  "code": 200,
  "message": "成功获取类型列表",
  "data": [
    {
      "code": "fantasy",
      "displayName": "奇幻",
      "features": "魔法体系、多种族、神话元素、古代文明"
    },
    {
      "code": "sci-fi",
      "displayName": "科幻",
      "features": "科技发展、太空探索、未来社会、人工智能"
    },
    ...
  ]
}
```

#### 6. 获取类型生成要求

**端点**: `GET /api/worldviews/genres/{genreCode}/requirements`

**功能**: 获取特定类型的详细生成要求

**路径参数**:
- `genreCode` (String): 类型代码（如"fantasy"）

**响应** (HTTP 200 OK):

```json
{
  "code": 200,
  "message": "成功获取类型要求",
  "data": {
    "code": "fantasy",
    "displayName": "奇幻",
    "features": "魔法体系、多种族、神话元素、古代文明",
    "requirements": "- 详细的魔法体系（魔力来源、施法规则、魔法分类）\n- 至少3个主要种族及其特征\n- 神话传说和创世故事\n- 魔法物品和神器设定\n"
  }
}
```

---

## 💡 使用示例

### 示例1: 生成奇幻世界观

```bash
# 使用curl
curl -X POST http://localhost:8080/api/worldviews/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "genre": "FANTASY",
    "keywords": ["魔法", "精灵", "矮人"],
    "worldScale": "多大陆",
    "powerLevel": "高魔",
    "creativity": 0.85
  }'
```

### 示例2: 生成科幻世界观

```bash
curl -X POST http://localhost:8080/api/worldviews/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "genre": "SCI_FI",
    "keywords": ["星际殖民", "AI革命", "量子跃迁"],
    "worldScale": "星系",
    "powerLevel": "星际文明三级",
    "civilizationStage": "星际时代",
    "creativity": 0.9
  }'
```

### 示例3: 批量生成并选择最佳方案

```bash
# 生成3个方案
curl -X POST "http://localhost:8080/api/worldviews/generate/batch?count=3" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "genre": "WUXIA",
    "keywords": ["江湖", "门派", "神功"],
    "creativity": 0.8
  }'

# 返回3个不同的武侠世界观方案，用户可选择最喜欢的
```

### 示例4: 验证故事内容

```bash
# 完整验证
curl -X POST http://localhost:8080/api/worldviews/123e4567-e89b-12d3-a456-426614174000/validate \
  -H "Content-Type: application/json" \
  -d '{
    "content": "张三修炼了火焰掌，成为江湖高手...",
    "contentType": "NARRATIVE"
  }'

# 快速验证
curl -X GET "http://localhost:8080/api/worldviews/123e4567-e89b-12d3-a456-426614174000/quick-validate?content=张三修炼了火焰掌"
```

---

## 🔧 集成指南

### 后端集成

#### 在其他服务中调用

```java
@Service
@RequiredArgsConstructor
public class StoryGenerationService {

    private final WorldviewGenerationService worldviewGenerationService;
    private final WorldviewConsistencyValidator consistencyValidator;

    public void generateStoryWithWorldview(UUID projectId) {
        // 1. 生成世界观
        WorldviewGenerationRequest request = WorldviewGenerationRequest.builder()
            .projectId(projectId)
            .genre(WorldviewGenre.FANTASY)
            .keywords(List.of("魔法", "冒险"))
            .build();

        WorldviewDTO worldview = worldviewGenerationService.generateWorldview(request);

        // 2. 生成故事章节（假设有这个方法）
        String chapter = generateChapter(worldview);

        // 3. 验证章节是否符合世界观
        ConsistencyResult validation = consistencyValidator.validateContent(
            worldview.getId(),
            chapter,
            ContentType.CHAPTER
        );

        if (!validation.getPassed()) {
            log.warn("章节不符合世界观: {}", validation.getViolations());
            // 重新生成或修改
        }
    }
}
```

#### 在项目创建时自动生成世界观

```java
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final WorldviewGenerationService worldviewGenerationService;

    @Transactional
    public ProjectDTO createProjectWithWorldview(ProjectCreateRequest request) {
        // 1. 创建项目
        Project project = projectRepository.save(new Project(...));

        // 2. 如果用户提供了世界观关键词，自动生成
        if (request.getWorldviewKeywords() != null) {
            WorldviewGenerationRequest worldviewRequest = WorldviewGenerationRequest.builder()
                .projectId(project.getId())
                .genre(request.getWorldviewGenre())
                .keywords(request.getWorldviewKeywords())
                .creativity(0.8)
                .build();

            worldviewGenerationService.generateWorldview(worldviewRequest);
        }

        return convertToDTO(project);
    }
}
```

### 前端集成

#### Vuex Store模块（待实现）

```javascript
// front/src/store/modules/worldview.js
import { worldviewApi } from '@/http/api'

export default {
  namespaced: true,

  state: {
    currentWorldview: null,
    worldviewList: [],
    genres: [],
    generationLoading: false
  },

  mutations: {
    SET_CURRENT_WORLDVIEW(state, worldview) {
      state.currentWorldview = worldview
    },
    SET_WORLDVIEW_LIST(state, list) {
      state.worldviewList = list
    },
    SET_GENRES(state, genres) {
      state.genres = genres
    },
    SET_GENERATION_LOADING(state, loading) {
      state.generationLoading = loading
    }
  },

  actions: {
    // 生成世界观
    async generateWorldview({ commit }, request) {
      commit('SET_GENERATION_LOADING', true)
      try {
        const response = await worldviewApi.generate(request)
        commit('SET_CURRENT_WORLDVIEW', response.data)
        return response.data
      } finally {
        commit('SET_GENERATION_LOADING', false)
      }
    },

    // 批量生成
    async generateMultiple({ commit }, { request, count }) {
      commit('SET_GENERATION_LOADING', true)
      try {
        const response = await worldviewApi.generateBatch(request, count)
        commit('SET_WORLDVIEW_LIST', response.data)
        return response.data
      } finally {
        commit('SET_GENERATION_LOADING', false)
      }
    },

    // 验证内容
    async validateContent({ state }, { worldviewId, content, contentType }) {
      const response = await worldviewApi.validate(worldviewId, {
        content,
        contentType
      })
      return response.data
    },

    // 获取类型列表
    async fetchGenres({ commit }) {
      const response = await worldviewApi.getGenres()
      commit('SET_GENRES', response.data)
    }
  }
}
```

#### API定义（待实现）

```javascript
// front/src/http/api.js
export const worldviewApi = {
  // 生成世界观
  generate(request) {
    return axios.post('/api/worldviews/generate', request)
  },

  // 批量生成
  generateBatch(request, count = 3) {
    return axios.post(`/api/worldviews/generate/batch?count=${count}`, request)
  },

  // 验证内容
  validate(worldviewId, data) {
    return axios.post(`/api/worldviews/${worldviewId}/validate`, data)
  },

  // 快速验证
  quickValidate(worldviewId, content) {
    return axios.get(`/api/worldviews/${worldviewId}/quick-validate`, {
      params: { content }
    })
  },

  // 获取类型列表
  getGenres() {
    return axios.get('/api/worldviews/genres')
  },

  // 获取类型要求
  getGenreRequirements(genreCode) {
    return axios.get(`/api/worldviews/genres/${genreCode}/requirements`)
  }
}
```

#### Vue组件示例（待实现）

```vue
<!-- front/src/views/worldview/Generate.vue -->
<template>
  <div class="worldview-generator">
    <a-form :model="form" @submit="handleGenerate">
      <!-- 故事类型选择 -->
      <a-form-item label="故事类型">
        <a-select v-model:value="form.genre">
          <a-select-option
            v-for="genre in genres"
            :key="genre.code"
            :value="genre.code">
            {{ genre.displayName }} - {{ genre.features }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- 关键词输入 -->
      <a-form-item label="关键词">
        <a-select
          v-model:value="form.keywords"
          mode="tags"
          placeholder="输入关键词，按回车添加">
        </a-select>
      </a-form-item>

      <!-- 世界规模 -->
      <a-form-item label="世界规模">
        <a-input v-model:value="form.worldScale" placeholder="如：单一星球、星系" />
      </a-form-item>

      <!-- 创意度滑块 -->
      <a-form-item label="创意度">
        <a-slider v-model:value="form.creativity" :min="0" :max="1" :step="0.1" />
        <span>{{ form.creativity }}</span>
      </a-form-item>

      <!-- 生成按钮 -->
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading">
          生成世界观
        </a-button>
        <a-button @click="handleBatchGenerate" :loading="loading" style="margin-left: 10px;">
          批量生成（3个方案）
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 显示结果 -->
    <div v-if="currentWorldview" class="worldview-result">
      <h2>{{ currentWorldview.name }}</h2>
      <p>{{ currentWorldview.summary }}</p>
      <!-- 更多详情展示... -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { message } from 'ant-design-vue'

const store = useStore()
const loading = ref(false)
const genres = ref([])

const form = ref({
  projectId: '', // 从路由或全局状态获取
  genre: 'FANTASY',
  keywords: [],
  worldScale: '',
  creativity: 0.8
})

const currentWorldview = computed(() => store.state.worldview.currentWorldview)

onMounted(async () => {
  await store.dispatch('worldview/fetchGenres')
  genres.value = store.state.worldview.genres
})

const handleGenerate = async () => {
  loading.value = true
  try {
    await store.dispatch('worldview/generateWorldview', form.value)
    message.success('世界观生成成功！')
  } catch (error) {
    message.error('生成失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleBatchGenerate = async () => {
  loading.value = true
  try {
    await store.dispatch('worldview/generateMultiple', {
      request: form.value,
      count: 3
    })
    message.success('已生成3个方案，请选择')
  } catch (error) {
    message.error('生成失败：' + error.message)
  } finally {
    loading.value = false
  }
}
</script>
```

---

## 🧪 测试建议

### 单元测试

#### 测试WorldviewGenerationService

```java
@SpringBootTest
class WorldviewGenerationServiceTest {

    @Autowired
    private WorldviewGenerationService generationService;

    @MockBean
    private AiGenerationService aiService;

    @Test
    void testGenerateWorldview_Success() {
        // Mock AI响应
        String mockAiResponse = """
            {
              "name": "测试世界",
              "summary": "这是一个测试世界观",
              "rules": ["规则1", "规则2"],
              "constraints": ["约束1"]
            }
            """;

        when(aiService.chatWithOptions(anyString(), anyDouble(), anyInt()))
            .thenReturn(mockAiResponse);

        // 准备请求
        WorldviewGenerationRequest request = WorldviewGenerationRequest.builder()
            .projectId(UUID.randomUUID())
            .genre(WorldviewGenre.FANTASY)
            .keywords(List.of("魔法", "龙"))
            .build();

        // 执行生成
        WorldviewDTO result = generationService.generateWorldview(request);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试世界", result.getName());
        assertEquals(2, result.getRules().size());
    }

    @Test
    void testBuildWorldviewPrompt_ContainsGenreRequirements() {
        WorldviewGenerationRequest request = WorldviewGenerationRequest.builder()
            .projectId(UUID.randomUUID())
            .genre(WorldviewGenre.SCI_FI)
            .keywords(List.of("AI", "太空"))
            .build();

        // 使用反射调用private方法
        String prompt = invokePrivateMethod(generationService, "buildWorldviewPrompt", request);

        // 验证提示词包含科幻的特定要求
        assertTrue(prompt.contains("科技发展"));
        assertTrue(prompt.contains("星际政治"));
    }
}
```

#### 测试WorldviewConsistencyValidator

```java
@SpringBootTest
class WorldviewConsistencyValidatorTest {

    @Autowired
    private WorldviewConsistencyValidator validator;

    @Autowired
    private WorldviewRepository worldviewRepository;

    @Test
    void testValidateContent_PassesWhenNoViolations() {
        // 创建测试世界观
        Worldview worldview = new Worldview();
        worldview.setName("测试世界");
        worldview.setRules(List.of("必须使用魔法"));
        worldview.setConstraints(List.of("禁止科技"));
        worldviewRepository.save(worldview);

        // 测试符合规则的内容
        String validContent = "主角使用魔法打败了敌人";
        ConsistencyResult result = validator.validateContent(
            worldview.getId(),
            validContent,
            ContentType.NARRATIVE
        );

        assertTrue(result.getPassed());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void testValidateContent_FailsWhenViolatesConstraints() {
        Worldview worldview = new Worldview();
        worldview.setName("测试世界");
        worldview.setConstraints(List.of("禁止科技"));
        worldviewRepository.save(worldview);

        // 测试违反约束的内容
        String invalidContent = "主角使用激光枪科技武器";
        ConsistencyResult result = validator.validateContent(
            worldview.getId(),
            invalidContent,
            ContentType.NARRATIVE
        );

        assertFalse(result.getPassed());
        assertFalse(result.getViolations().isEmpty());
    }
}
```

### 集成测试

#### 测试完整API流程

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WorldviewGenerationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGenerateWorldview_EndToEnd() throws Exception {
        WorldviewGenerationRequest request = WorldviewGenerationRequest.builder()
            .projectId(UUID.randomUUID())
            .genre(WorldviewGenre.FANTASY)
            .keywords(List.of("魔法", "冒险"))
            .build();

        mockMvc.perform(post("/api/worldviews/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").exists())
            .andExpect(jsonPath("$.data.summary").exists());
    }

    @Test
    void testGetGenres_ReturnsAllGenres() throws Exception {
        mockMvc.perform(get("/api/worldviews/genres"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(12));
    }
}
```

### 手动测试场景

1. **基础生成测试**
   - 测试每种genre是否都能成功生成
   - 测试不同创意度（0.3、0.5、0.8、1.0）的输出差异

2. **批量生成测试**
   - 测试count=1到5的批量生成
   - 验证每个生成的结果是否有差异

3. **验证测试**
   - 创建一个有严格规则的世界观
   - 测试明显违反规则的内容是否被检测
   - 测试边缘情况的内容

4. **性能测试**
   - 测试单次生成的响应时间（预期15-30秒）
   - 测试批量生成count=5的响应时间（预期60-120秒）

---

## ⚠️ 注意事项

### AI生成相关

1. **响应时间**:
   - 单次生成通常需要15-30秒
   - 批量生成时间会成倍增加
   - 考虑添加前端加载动画和进度提示

2. **JSON解析容错**:
   - AI可能返回格式不完全符合的JSON
   - 已实现fallback机制，生成失败时返回基础DTO
   - 生产环境建议增加重试逻辑

3. **Token限制**:
   - 当前设置maxTokens=3500
   - 如果生成内容不完整，可能需要调高此值
   - 注意API成本

### 数据库相关

1. **JSONB字段**:
   - universeLaws、socialStructure等使用JSONB存储
   - 灵活但查询性能较差
   - 如需频繁查询，考虑建立GIN索引

2. **事务管理**:
   - generateWorldview()方法使用@Transactional
   - 如果AI调用失败，会回滚数据库操作

### 验证器限制

1. **规则检测局限性**:
   - 基于关键词匹配，可能误判
   - 复杂的语义违规需要AI深度验证

2. **性能考虑**:
   - 完整验证（含AI）较慢，适合最终验证
   - 快速验证适合实时反馈

---

## 🚀 下一步计划（Phase 2）

Phase 1完成后，下一阶段将实现：

### Phase 2: 场景AI生成系统

**核心功能**:
1. SceneGenerationService - 场景生成服务
2. SceneGenerationRequest DTO - 场景生成请求
3. 场景与世界观的联动（验证场景是否符合世界观）
4. 感官细节提取（视觉、听觉、嗅觉、触觉、味觉）
5. SceneGenerationController - 场景生成API

**预计工作量**: 约800行代码，2-3个工作日

---

## 📊 总结

Phase 1实现了完整的世界观AI生成系统，包括：

✅ **12种世界观类型**: 覆盖奇幻、科幻、武侠等主流类型
✅ **智能提示词构建**: 根据类型动态生成详细提示词
✅ **灵活的生成参数**: 支持世界规模、力量水平、文明阶段等自定义
✅ **批量生成**: 一次生成多个方案供选择
✅ **多维度验证**: 规则、约束、物理法则、术语一致性检查
✅ **AI辅助验证**: 低分内容自动触发AI深度分析
✅ **5个REST API端点**: 完整的前后端交互接口

**代码质量**:
- 使用Lombok简化代码
- 事务管理保证数据一致性
- 完善的错误处理和fallback机制
- 详细的日志记录便于调试

**可扩展性**:
- 易于添加新的世界观类型
- 提示词模板化，便于优化
- 验证维度可灵活扩展

---

**文档版本**: 1.0
**创建日期**: 2025-10-27
**作者**: StoryForge Development Team
