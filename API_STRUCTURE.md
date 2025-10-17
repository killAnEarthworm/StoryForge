# StoryForge API Structure

## Project Architecture

项目采用标准的分层架构：

```
Controller (API 层)
    ↓
Service (业务逻辑层)
    ↓
Repository (数据访问层)
    ↓
Entity (实体/数据库映射层)
```

## Package Structure

```
com.linyuan.storyforge
├── entity/              # 实体类 (对应数据库表)
│   ├── BaseEntity.java
│   ├── Project.java
│   ├── Character.java
│   ├── Worldview.java
│   ├── Scene.java
│   ├── StoryChapter.java
│   ├── CharacterRelationship.java
│   └── PromptTemplate.java
│
├── repository/          # Repository 接口 (数据访问)
│   ├── ProjectRepository.java
│   ├── CharacterRepository.java
│   ├── WorldviewRepository.java
│   ├── SceneRepository.java
│   └── StoryChapterRepository.java
│
├── service/             # Service 层 (业务逻辑)
│   ├── ProjectService.java
│   ├── CharacterService.java
│   └── StoryChapterService.java
│
├── controller/          # REST Controllers (API 接口)
│   ├── ProjectController.java
│   ├── CharacterController.java
│   └── StoryChapterController.java
│
├── dto/                 # Data Transfer Objects
│   ├── ProjectDTO.java
│   ├── CharacterDTO.java
│   └── StoryChapterDTO.java
│
├── common/              # 通用类
│   └── ApiResponse.java
│
├── exception/           # 异常处理
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
│
└── config/              # 配置类
    └── SecurityConfig.java
```

## API Endpoints

### Project APIs (项目管理)

**Base URL:** `/api/projects`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | 获取所有项目 |
| GET | `/api/projects/{id}` | 根据ID获取项目 |
| POST | `/api/projects` | 创建新项目 |
| PUT | `/api/projects/{id}` | 更新项目 |
| DELETE | `/api/projects/{id}` | 删除项目 |
| GET | `/api/projects/status/{status}` | 根据状态获取项目 |
| GET | `/api/projects/genre/{genre}` | 根据类型获取项目 |
| GET | `/api/projects/search?name=xxx` | 搜索项目 |

**创建项目示例:**
```json
POST /api/projects
{
  "name": "星际迷航",
  "description": "一个关于太空探险的故事",
  "genre": "科幻",
  "theme": "探索与发现",
  "writingStyle": "第三人称全知视角",
  "status": "draft"
}
```

### Character APIs (角色管理)

**Base URL:** `/api/characters`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/characters` | 获取所有角色 |
| GET | `/api/characters/{id}` | 根据ID获取角色 |
| GET | `/api/characters/project/{projectId}` | 获取项目的所有角色 |
| POST | `/api/characters` | 创建新角色 |
| PUT | `/api/characters/{id}` | 更新角色 |
| DELETE | `/api/characters/{id}` | 删除角色 |

**创建角色示例:**
```json
POST /api/characters
{
  "projectId": "uuid-of-project",
  "name": "张三",
  "age": 25,
  "occupation": "宇航员",
  "appearance": "高大英俊，黑色短发",
  "personalityTraits": ["勇敢", "果断", "善良"],
  "backgroundStory": "从小梦想成为宇航员...",
  "goals": ["探索未知星系", "保护地球"],
  "fears": ["失去亲人"]
}
```

### Story Chapter APIs (章节管理)

**Base URL:** `/api/chapters`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chapters` | 获取所有章节 |
| GET | `/api/chapters/{id}` | 根据ID获取章节 |
| GET | `/api/chapters/project/{projectId}` | 获取项目的所有章节 |
| GET | `/api/chapters/status/{status}` | 根据状态获取章节 |
| POST | `/api/chapters` | 创建新章节 |
| PUT | `/api/chapters/{id}` | 更新章节 |
| DELETE | `/api/chapters/{id}` | 删除章节 |

**创建章节示例:**
```json
POST /api/chapters
{
  "projectId": "uuid-of-project",
  "chapterNumber": 1,
  "title": "启航",
  "outline": "主角接到任务，准备出发",
  "mainConflict": "时间紧迫，准备不足",
  "targetWordCount": 3000,
  "tone": "紧张",
  "pacing": "快节奏",
  "status": "outline"
}
```

## Response Format

所有 API 响应都使用统一格式：

**成功响应:**
```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2025-10-17T10:30:00"
}
```

**错误响应:**
```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "timestamp": "2025-10-17T10:30:00"
}
```

## Status Codes

- `200 OK` - 成功
- `201 Created` - 创建成功
- `204 No Content` - 删除成功
- `400 Bad Request` - 请求参数错误
- `404 Not Found` - 资源不存在
- `500 Internal Server Error` - 服务器错误

## Database Setup

在运行之前，请确保：

1. PostgreSQL 数据库已启动
2. 已创建数据库 `storyforge`
3. 已执行 `src/main/resources/_sql/init.sql` 初始化数据库表
4. 已配置环境变量（参考 CLAUDE.md）

## Running the Application

```bash
# 构建项目
mvnw clean package -DskipTests

# 运行应用
mvnw spring-boot:run

# 应用将在 http://localhost:8080 启动
```

## Testing APIs

可以使用 Postman、cURL 或任何 HTTP 客户端测试 API：

```bash
# 获取所有项目
curl http://localhost:8080/api/projects

# 创建新项目
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"测试项目","genre":"科幻","status":"draft"}'
```

## Next Steps

基础 CRUD 已完成，接下来可以：

1. 添加更多实体的 CRUD（Worldview, Scene, Timeline 等）
2. 实现复杂的业务逻辑（AI 生成功能）
3. 添加用户认证和授权
4. 添加数据验证和业务规则
5. 编写单元测试和集成测试
