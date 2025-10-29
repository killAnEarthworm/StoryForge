# 场景生成系统 - 使用示例和测试指南

## 📝 概述

本文档提供了 `SceneType` 枚举和 `SceneGenerationRequest` DTO 的详细使用示例和测试指南。

---

## 1. SceneType 枚举使用示例

### 1.1 基础使用

```java
// 获取场景类型
SceneType type = SceneType.ACTION;

// 获取类型信息
String code = type.getCode();                    // "action"
String displayName = type.getDisplayName();      // "动作"
String features = type.getFeatures();            // "节奏紧凑、动作描写、紧张刺激"
String atmosphere = type.getAtmosphereKeywords(); // "紧张、激烈、快节奏"
String sensory = type.getSensoryFocus();         // "视觉、听觉、触觉"

// 获取生成要求
String requirements = type.getGenerationRequirements();
System.out.println(requirements);
/*
输出:
- 快节奏的动作描写，使用短句增强紧张感
- 详细描写动作细节（招式、移动、碰撞）
- 突出视觉和触觉感受（速度感、冲击感）
- 使用动态动词，避免静态描写
- 注意动作的连贯性和合理性
- 适当加入环境互动（利用地形、物品）
*/

// 获取感官描写指导
String guidance = type.getSensoryGuidance();
System.out.println(guidance);
/*
输出:
视觉: 动作轨迹、身体姿态、武器光影、环境破坏
听觉: 兵器碰撞、呼吸喘息、脚步声、环境响动
触觉: 冲击力、疼痛感、肌肉紧绷、风压
*/

// 获取写作风格建议
String style = type.getStyleGuidance();
System.out.println(style);
// 输出: "短句为主，节奏紧凑，动词丰富，避免冗长描写"
```

### 1.2 根据代码获取枚举

```java
// 从字符串代码获取枚举
SceneType dialogue = SceneType.fromCode("dialogue");
System.out.println(dialogue.getDisplayName()); // "对话"

// 错误处理
try {
    SceneType unknown = SceneType.fromCode("unknown");
} catch (IllegalArgumentException e) {
    System.err.println(e.getMessage()); // "Unknown scene type code: unknown"
}
```

### 1.3 遍历所有场景类型

```java
// 获取所有场景类型
for (SceneType type : SceneType.values()) {
    System.out.printf("%s (%s) - %s%n",
            type.getDisplayName(),
            type.getCode(),
            type.getFeatures());
}
/*
输出:
动作 (action) - 节奏紧凑、动作描写、紧张刺激
对话 (dialogue) - 角色互动、语言交锋、信息传递
描写 (description) - 环境细节、氛围营造、意境渲染
情感 (emotional) - 情绪表达、内心独白、情感起伏
冲突 (conflict) - 矛盾激化、对立冲突、紧张对峙
过渡 (transition) - 承上启下、时空转换、节奏调整
高潮 (climax) - 情节顶点、情绪爆发、重大转折
开场 (opening) - 引入情境、设定基调、吸引注意
结尾 (ending) - 收束情节、余韵留存、情感升华
日常 (daily) - 生活细节、日常互动、平淡真实
*/
```

### 1.4 在控制器中使用

```java
@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    @GetMapping("/types")
    public ApiResponse<List<SceneTypeInfo>> getAllSceneTypes() {
        List<SceneTypeInfo> types = Arrays.stream(SceneType.values())
                .map(type -> SceneTypeInfo.builder()
                        .code(type.getCode())
                        .displayName(type.getDisplayName())
                        .features(type.getFeatures())
                        .atmosphereKeywords(type.getAtmosphereKeywords())
                        .sensoryFocus(type.getSensoryFocus())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(types);
    }

    @GetMapping("/types/{code}/requirements")
    public ApiResponse<SceneTypeDetails> getSceneTypeRequirements(
            @PathVariable String code) {
        try {
            SceneType type = SceneType.fromCode(code);
            SceneTypeDetails details = SceneTypeDetails.builder()
                    .code(type.getCode())
                    .displayName(type.getDisplayName())
                    .requirements(type.getGenerationRequirements())
                    .sensoryGuidance(type.getSensoryGuidance())
                    .styleGuidance(type.getStyleGuidance())
                    .build();

            return ApiResponse.success(details);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("未找到场景类型: " + code);
        }
    }
}
```

---

## 2. SceneGenerationRequest 使用示例

### 2.1 最简请求（只提供必填字段）

```java
SceneGenerationRequest request = SceneGenerationRequest.builder()
        .projectId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
        .sceneType(SceneType.ACTION)
        .location("废弃的工厂")
        .mood("紧张")
        .build();

// 验证请求
String validationError = request.validate();
if (validationError != null) {
    System.err.println("请求验证失败: " + validationError);
} else {
    System.out.println("请求验证通过");
}

// 获取简化描述
System.out.println(request.getSimpleDescription());
// 输出: "[动作] 废弃的工厂场景 - 紧张 (600字)"
```

### 2.2 完整的动作场景请求

```java
SceneGenerationRequest actionScene = SceneGenerationRequest.builder()
        // 基础信息
        .projectId(projectId)
        .worldviewId(worldviewId)
        .chapterId(chapterId)

        // 场景类型和定位
        .sceneType(SceneType.ACTION)
        .scenePurpose("展示主角的战斗技巧和决心")
        .sceneTitle("废墟中的决战")

        // 场景设定
        .location("废弃工厂的锅炉房")
        .timeOfDay("深夜")
        .weather("暴雨")
        .season("秋天")

        // 氛围和情绪
        .mood("紧张")
        .atmosphere("压迫、危险")
        .emotionalIntensity(8)

        // 参与角色
        .characterIds(List.of(protagonistId, antagonistId))
        .characterRelations("主角与黑帮老大的最终对决")
        .perspectiveCharacterId(protagonistId)

        // 情节要素
        .plotContext("主角追踪黑帮老大到废弃工厂，准备决一死战")
        .keyEvents(List.of(
                "主角和反派短暂对话",
                "激烈的近身搏斗",
                "工厂机械被破坏，造成危险",
                "主角险胜"
        ))
        .conflict("生死对决，只有一人能活着离开")

        // 感官细节要求
        .includeVisualDetails(true)
        .includeAuditoryDetails(true)
        .includeOlfactory(true)  // 工厂的锈蚀气味
        .includeTactile(true)    // 雨水、疼痛
        .sensoryFocus(List.of("视觉", "触觉", "听觉"))

        // 长度和风格
        .targetWordCount(1200)
        .writingStyle("紧凑")
        .narrativePace("快速")
        .descriptionDensity(SceneGenerationRequest.DescriptionDensity.MEDIUM)

        // AI参数
        .creativity(0.7)

        // 约束条件
        .mustInclude(List.of(
                "\"这是你最后的机会\"这句话",
                "工厂天窗破碎，雨水倾泻而下"
        ))
        .mustAvoid(List.of("过度血腥的描写"))

        // 其他选项
        .includeInnerMonologue(true)
        .includeEnvironmentInteraction(true)
        .includeDialogue(true)
        .dialogueRatio(0.2)  // 20%对话，80%动作和描写
        .extractTimelineEvents(true)

        .build();
```

### 2.3 情感场景请求

```java
SceneGenerationRequest emotionalScene = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.EMOTIONAL)
        .location("母亲的墓前")
        .timeOfDay("黄昏")
        .weather("阴天")

        .mood("悲伤")
        .atmosphere("沉重、怀念")
        .emotionalIntensity(9)

        .characterIds(List.of(protagonistId))
        .perspectiveCharacterId(protagonistId)

        .plotContext("主角在完成复仇后，来到母亲墓前")
        .scenePurpose("表达主角内心的释然和悲伤")

        .includeVisualDetails(true)
        .includeAuditoryDetails(false)  // 强调寂静
        .includeInnerMonologue(true)

        .targetWordCount(800)
        .writingStyle("细腻")
        .descriptionDensity(SceneGenerationRequest.DescriptionDensity.HIGH)

        .creativity(0.8)

        .build();
```

### 2.4 对话场景请求

```java
SceneGenerationRequest dialogueScene = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.DIALOGUE)
        .location("咖啡馆")
        .timeOfDay("下午")

        .mood("轻松")
        .atmosphere("温馨")

        .characterIds(List.of(character1Id, character2Id))
        .characterRelations("老朋友久别重逢")

        .plotContext("两位老朋友十年后再次相遇")
        .keyEvents(List.of(
                "回忆往事",
                "谈论各自的生活",
                "约定继续保持联系"
        ))

        .includeDialogue(true)
        .dialogueRatio(0.7)  // 70%对话

        .targetWordCount(1000)
        .writingStyle("自然")

        .creativity(0.6)

        .build();
```

### 2.5 描写场景请求

```java
SceneGenerationRequest descriptionScene = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.DESCRIPTION)
        .location("雪山之巅")
        .timeOfDay("黎明")
        .weather("晴朗")
        .season("冬天")

        .mood("宁静")
        .atmosphere("壮丽、圣洁")

        .scenePurpose("营造雪山的壮丽景象，为后续情节做铺垫")

        .includeVisualDetails(true)
        .includeAuditoryDetails(true)
        .includeOlfactory(true)
        .includeTactile(true)

        .targetWordCount(600)
        .writingStyle("诗意")
        .descriptionDensity(SceneGenerationRequest.DescriptionDensity.HIGH)

        .creativity(0.85)

        .includeDialogue(false)  // 纯描写，不包含对话

        .build();
```

### 2.6 开场场景请求

```java
SceneGenerationRequest openingScene = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.OPENING)
        .location("繁华的都市街道")
        .timeOfDay("正午")

        .mood("热闹")
        .atmosphere("充满活力")

        .characterIds(List.of(protagonistId))
        .perspectiveCharacterId(protagonistId)

        .plotContext("故事的开端，主角正常的一天即将被打破")
        .scenePurpose("引入主角，设定故事基调，埋下悬念")

        .keyEvents(List.of(
                "展示主角的日常生活",
                "偶然发现异常事件的线索"
        ))

        .targetWordCount(800)
        .writingStyle("吸引人")

        .mustInclude(List.of("一张神秘的传单"))

        .creativity(0.75)

        .build();
```

### 2.7 高潮场景请求

```java
SceneGenerationRequest climaxScene = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.CLIMAX)
        .location("古老的神殿中心")
        .timeOfDay("午夜")

        .mood("震撼")
        .atmosphere("神秘、危险、史诗感")
        .emotionalIntensity(10)

        .characterIds(allMainCharacterIds)
        .characterRelations("所有主要角色汇聚，面对最终挑战")

        .plotContext("所有线索汇聚，真相即将揭晓")
        .keyEvents(List.of(
                "真相揭示",
                "重大抉择",
                "惊天逆转",
                "巨大牺牲"
        ))

        .includeVisualDetails(true)
        .includeAuditoryDetails(true)
        .includeOlfactory(true)
        .includeTactile(true)
        .sensoryFocus(List.of("全感官"))

        .targetWordCount(1500)
        .writingStyle("戏剧性")
        .narrativePace("跌宕起伏")
        .descriptionDensity(SceneGenerationRequest.DescriptionDensity.HIGH)

        .creativity(0.85)

        .includeInnerMonologue(true)
        .includeDialogue(true)
        .dialogueRatio(0.3)

        .extractTimelineEvents(true)

        .build();
```

---

## 3. 验证和错误处理

### 3.1 请求验证

```java
SceneGenerationRequest request = SceneGenerationRequest.builder()
        .projectId(projectId)
        .sceneType(SceneType.ACTION)
        .location("战场")
        .mood("紧张")
        .targetWordCount(50)  // 错误：低于最小值100
        .creativity(1.5)      // 错误：超过最大值1.0
        .build();

// 验证
String error = request.validate();
if (error != null) {
    System.err.println("验证失败: " + error);
    // 输出: "验证失败: 目标字数必须在100-3000之间"
}
```

### 3.2 使用Jakarta Validation

```java
@Service
public class SceneGenerationService {

    @Autowired
    private Validator validator;

    public SceneDTO generateScene(SceneGenerationRequest request) {
        // 使用Jakarta Validation验证
        Set<ConstraintViolation<SceneGenerationRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("请求验证失败: " + errors);
        }

        // 自定义验证
        String customError = request.validate();
        if (customError != null) {
            throw new IllegalArgumentException(customError);
        }

        // 继续生成逻辑...
    }
}
```

---

## 4. 在控制器中使用

### 4.1 场景生成接口

```java
@RestController
@RequestMapping("/api/scenes")
@Slf4j
public class SceneGenerationController {

    @Autowired
    private SceneGenerationService sceneGenerationService;

    @PostMapping("/generate")
    public ApiResponse<SceneDTO> generateScene(
            @Valid @RequestBody SceneGenerationRequest request) {

        log.info("收到场景生成请求: {}", request.getSimpleDescription());

        // 自定义验证
        String validationError = request.validate();
        if (validationError != null) {
            return ApiResponse.error(validationError);
        }

        try {
            SceneDTO scene = sceneGenerationService.generateScene(request);
            return ApiResponse.success(scene, "场景生成成功");
        } catch (Exception e) {
            log.error("场景生成失败", e);
            return ApiResponse.error("场景生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate/batch")
    public ApiResponse<List<SceneDTO>> generateMultipleScenes(
            @Valid @RequestBody SceneGenerationRequest request,
            @RequestParam(defaultValue = "3") int count) {

        if (count < 1 || count > 5) {
            return ApiResponse.error("批量生成数量必须在1-5之间");
        }

        log.info("批量生成{}个场景方案: {}", count, request.getSimpleDescription());

        try {
            List<SceneDTO> scenes =
                    sceneGenerationService.generateMultipleScenes(request, count);
            return ApiResponse.success(scenes,
                    String.format("成功生成%d个场景方案", count));
        } catch (Exception e) {
            log.error("批量场景生成失败", e);
            return ApiResponse.error("批量场景生成失败: " + e.getMessage());
        }
    }
}
```

---

## 5. 单元测试示例

### 5.1 测试SceneType枚举

```java
@Test
public void testSceneTypeBasics() {
    // 测试所有类型都有正确的属性
    for (SceneType type : SceneType.values()) {
        assertNotNull(type.getCode());
        assertNotNull(type.getDisplayName());
        assertNotNull(type.getFeatures());
        assertNotNull(type.getAtmosphereKeywords());
        assertNotNull(type.getSensoryFocus());
        assertNotNull(type.getGenerationRequirements());
        assertNotNull(type.getSensoryGuidance());
        assertNotNull(type.getStyleGuidance());
    }
}

@Test
public void testSceneTypeFromCode() {
    // 测试fromCode方法
    assertEquals(SceneType.ACTION, SceneType.fromCode("action"));
    assertEquals(SceneType.DIALOGUE, SceneType.fromCode("dialogue"));
    assertEquals(SceneType.CLIMAX, SceneType.fromCode("climax"));

    // 测试大小写不敏感
    assertEquals(SceneType.ACTION, SceneType.fromCode("ACTION"));
    assertEquals(SceneType.DIALOGUE, SceneType.fromCode("DiAlOgUe"));

    // 测试错误代码
    assertThrows(IllegalArgumentException.class, () -> {
        SceneType.fromCode("invalid");
    });
}

@Test
public void testSceneTypeCount() {
    // 确保有10种场景类型
    assertEquals(10, SceneType.values().length);
}
```

### 5.2 测试SceneGenerationRequest

```java
@Test
public void testSceneGenerationRequestBuilder() {
    UUID projectId = UUID.randomUUID();

    SceneGenerationRequest request = SceneGenerationRequest.builder()
            .projectId(projectId)
            .sceneType(SceneType.ACTION)
            .location("测试地点")
            .mood("紧张")
            .build();

    assertNotNull(request);
    assertEquals(projectId, request.getProjectId());
    assertEquals(SceneType.ACTION, request.getSceneType());
    assertEquals("测试地点", request.getLocation());
    assertEquals("紧张", request.getMood());

    // 测试默认值
    assertEquals(600, request.getTargetWordCount());
    assertEquals(0.75, request.getCreativity());
    assertTrue(request.getIncludeVisualDetails());
    assertTrue(request.getIncludeAuditoryDetails());
    assertFalse(request.getIncludeOlfactory());
}

@Test
public void testSceneGenerationRequestValidation() {
    UUID projectId = UUID.randomUUID();

    // 测试有效请求
    SceneGenerationRequest validRequest = SceneGenerationRequest.builder()
            .projectId(projectId)
            .sceneType(SceneType.ACTION)
            .location("地点")
            .mood("情绪")
            .build();

    assertNull(validRequest.validate());

    // 测试字数超出范围
    SceneGenerationRequest invalidWordCount = SceneGenerationRequest.builder()
            .projectId(projectId)
            .sceneType(SceneType.ACTION)
            .location("地点")
            .mood("情绪")
            .targetWordCount(50)  // 低于100
            .build();

    assertNotNull(invalidWordCount.validate());
    assertTrue(invalidWordCount.validate().contains("目标字数"));

    // 测试创意度超出范围
    SceneGenerationRequest invalidCreativity = SceneGenerationRequest.builder()
            .projectId(projectId)
            .sceneType(SceneType.ACTION)
            .location("地点")
            .mood("情绪")
            .creativity(1.5)  // 超过1.0
            .build();

    assertNotNull(invalidCreativity.validate());
    assertTrue(invalidCreativity.validate().contains("创意度"));

    // 测试视角角色不在参与角色列表中
    UUID char1 = UUID.randomUUID();
    UUID char2 = UUID.randomUUID();

    SceneGenerationRequest invalidPerspective = SceneGenerationRequest.builder()
            .projectId(projectId)
            .sceneType(SceneType.ACTION)
            .location("地点")
            .mood("情绪")
            .characterIds(List.of(char1))
            .perspectiveCharacterId(char2)  // char2不在列表中
            .build();

    assertNotNull(invalidPerspective.validate());
    assertTrue(invalidPerspective.validate().contains("视角角色"));
}

@Test
public void testGetSimpleDescription() {
    SceneGenerationRequest request = SceneGenerationRequest.builder()
            .projectId(UUID.randomUUID())
            .sceneType(SceneType.EMOTIONAL)
            .location("墓地")
            .mood("悲伤")
            .targetWordCount(800)
            .build();

    String description = request.getSimpleDescription();
    assertTrue(description.contains("情感"));
    assertTrue(description.contains("墓地"));
    assertTrue(description.contains("悲伤"));
    assertTrue(description.contains("800字"));
}
```

---

## 6. 集成测试示例

```java
@SpringBootTest
@AutoConfigureMockMvc
public class SceneGenerationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateScene_Success() throws Exception {
        SceneGenerationRequest request = SceneGenerationRequest.builder()
                .projectId(UUID.randomUUID())
                .sceneType(SceneType.ACTION)
                .location("战场")
                .mood("紧张")
                .build();

        mockMvc.perform(post("/api/scenes/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testGenerateScene_ValidationError() throws Exception {
        SceneGenerationRequest invalidRequest = SceneGenerationRequest.builder()
                .projectId(UUID.randomUUID())
                .sceneType(SceneType.ACTION)
                .location("战场")
                .mood("紧张")
                .targetWordCount(50)  // 错误：低于100
                .build();

        mockMvc.perform(post("/api/scenes/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
```

---

## 7. 使用建议

### 7.1 场景类型选择指南

| 场景目的 | 推荐类型 | 创意度建议 | 字数建议 |
|---------|---------|-----------|---------|
| 战斗打斗 | ACTION | 0.6-0.7 | 800-1200 |
| 角色对话 | DIALOGUE | 0.5-0.6 | 600-1000 |
| 环境描写 | DESCRIPTION | 0.8-0.9 | 400-800 |
| 情感抒发 | EMOTIONAL | 0.7-0.8 | 500-900 |
| 矛盾冲突 | CONFLICT | 0.6-0.7 | 700-1000 |
| 场景切换 | TRANSITION | 0.5 | 200-400 |
| 剧情高潮 | CLIMAX | 0.8-0.9 | 1000-1500 |
| 章节开头 | OPENING | 0.7-0.8 | 600-900 |
| 章节结尾 | ENDING | 0.7-0.8 | 500-800 |
| 日常生活 | DAILY | 0.6-0.7 | 400-700 |

### 7.2 感官细节使用建议

- **动作场景**: 视觉 + 听觉 + 触觉
- **情感场景**: 视觉（微表情）+ 内心独白
- **描写场景**: 视觉 + 听觉 + 嗅觉
- **对话场景**: 听觉 + 视觉（动作表情）
- **高潮场景**: 全感官冲击

### 7.3 性能优化建议

1. **缓存场景类型信息**: `SceneType.values()` 的结果可以缓存
2. **分段生成长场景**: 超过1000字的场景建议分段生成
3. **批量生成时调整参数**: 每次生成略微调整 `creativity` 和 `writingStyle`

---

## 8. 常见问题

### Q1: 如何生成多个风格不同的场景方案？

```java
List<SceneGenerationRequest> variants = List.of(
    baseRequest.toBuilder().creativity(0.6).writingStyle("简洁").build(),
    baseRequest.toBuilder().creativity(0.8).writingStyle("细腻").build(),
    baseRequest.toBuilder().creativity(0.9).writingStyle("诗意").build()
);
```

### Q2: 如何确保场景符合世界观？

设置 `worldviewId` 字段，系统会自动验证一致性：

```java
request.setWorldviewId(worldviewId);
```

### Q3: 如何控制对话和描写的比例？

使用 `dialogueRatio` 字段：

```java
.dialogueRatio(0.7)  // 70%对话，30%描写
```

---

## ✅ 总结

本文档提供了 `SceneType` 和 `SceneGenerationRequest` 的完整使用示例，涵盖：

- ✅ 10种场景类型的详细使用
- ✅ 各种场景的请求示例（动作、情感、对话、描写等）
- ✅ 验证和错误处理
- ✅ 控制器集成
- ✅ 单元测试和集成测试
- ✅ 使用建议和最佳实践

**下一步**: 实现 `SceneGenerationService` 服务层逻辑！
