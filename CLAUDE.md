# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**StoryForge** is an AI-powered story generation platform built with Spring Boot 3.3.5 and Vue 3. The system uses Baidu Qianfan API (DeepSeek model) for intelligent narrative creation with sophisticated prompt engineering and character consistency systems.

**Current Status**: Active development
- ✅ Backend: Complete REST APIs for all six core modules
- ✅ AI Integration: Baidu Qianfan API with QianfanDirectService
- ✅ Database: PostgreSQL with pgvector, full schema implemented
- ✅ Frontend: Vue 3 + Vite + Ant Design Vue 4.0
- ✅ Core Features: Character/Worldview/Scene/Timeline/Chapter generation, Memory & Consistency system

## Build & Run Commands

### Backend (Spring Boot 3.3.5 + Java 21)

**Windows**:
```cmd
REM Build
mvnw.cmd clean package -DskipTests

REM Run
mvnw.cmd spring-boot:run

REM Run tests
mvnw.cmd test

REM Run specific test
mvnw.cmd test -Dtest=ClassName
```

**Unix/Linux/Mac**: Use `./mvnw` instead of `mvnw.cmd`

### Frontend (Vue 3 + Vite)

```bash
cd front
npm install          # First time only
npm run dev          # Dev server at http://localhost:8888
npm run build        # Production build
```

### Required Environment Variables

**Windows PowerShell**:
```powershell
$env:DB_PASSWORD="123456"
$env:QIANFAN_API_KEY="bce-v3/ALTAK-xxxxx/xxxxxx"
$env:JWT_SECRET="your-secret-key"
```

**Note**: Redis is optional. If not available, comment out `spring-session-data-redis` in pom.xml.

### Database Setup

```sql
CREATE DATABASE storyforge;
CREATE USER storyforge WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE storyforge TO storyforge;

\c storyforge
\i src/main/resources/_sql/init.sql
\i src/main/resources/_sql/add_updated_at_columns.sql
```

## Project Architecture

### Technology Stack

**Backend**:
- Spring Boot 3.3.5, Java 21
- PostgreSQL with pgvector extension
- JPA/Hibernate + MyBatis (for complex queries)
- Baidu Qianfan API via QianfanDirectService
- Spring Security (disabled in dev mode)

**Frontend**:
- Vue 3, Vite 1.0, Ant Design Vue 4.0
- Vuex 4 (state management)
- Vue Router 4
- Axios 1.12

### Layered Architecture

```
Controller (REST API)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access - JPA)
    ↓
Entity (Database Tables)
```

### Package Structure

```
com.linyuan.storyforge/
├── entity/              # JPA entities (Character, Worldview, Scene, etc.)
├── dto/                 # Data Transfer Objects
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic
│   ├── QianfanDirectService      # AI API client
│   ├── AiGenerationService       # AI generation orchestration
│   ├── GenerationPipeline        # Chapter generation pipeline
│   └── *Service                  # Domain services
├── controller/          # REST controllers
├── config/              # Configuration (Security, Prompts, AI)
├── exception/           # Exception handlers
├── validator/           # Consistency validators
├── common/              # Common utilities (ApiResponse)
└── enums/               # Enumerations
```

### Six Core Modules

All modules follow: **Entity → Repository → Service → Controller → Frontend**

1. **Character Module** - Character creation, memory system, relationships, consistency validation
2. **Worldview Module** - Universe laws, social structures, JSONB-based flexible schema
3. **Timeline Module** - Event management, causal relationships, paradox detection
4. **Scene Module** - Scene generation with atmosphere, sensory details, AI-enhanced
5. **Story Generation Module** - Chapter generation pipeline, quality control, regeneration
6. **Inspiration Module** - (Planned - not yet implemented)

### Key Features

**AI Integration**:
- Direct Baidu Qianfan API calls via QianfanDirectService
- Model: `deepseek-v3.1-250821`
- Prompt templates in `application.yml` under `ai.prompt.templates`
- Automatic retry and timeout handling

**Character Consistency**:
- Personality vectors (10 dimensions)
- Memory system with 5-tier hierarchy (core/emotional/skill/episodic/semantic)
- Consistency validation via CharacterConsistencyValidator
- Character relationships tracking

**Data Models**:
- JSONB fields for flexible schemas (Worldview, Character.importantExperiences)
- PostgreSQL arrays (personalityTraits, fears, desires, etc.)
- Float arrays for personality vectors
- BaseEntity with UUID primary keys

## REST API Structure

**Base URL**: `http://localhost:8080/api`

**Main Endpoints**:
- `/api/projects` - Project management
- `/api/characters` - Character CRUD
- `/api/worldviews` - Worldview CRUD
- `/api/scenes` - Scene CRUD
- `/api/timelines` - Timeline management
- `/api/chapters` - Chapter CRUD
- `/api/character-memories` - Memory management
- `/api/character-relationships` - Relationship tracking
- `/api/dialogues` - Dialogue management
- `/api/generation-history` - Generation tracking
- `/api/prompt-templates` - Template management

**AI Generation Endpoints**:
- `/api/ai/test` - Test AI connection
- `/api/ai/worldview/generate` - Generate worldview
- `/api/ai/scene/generate` - Generate scene
- `/api/ai/chapter/generate` - Generate chapter
- `/api/ai/story/generate` - Generate story

**Standard REST Pattern**:
- `GET /{resource}` - List all
- `POST /{resource}` - Create
- `GET /{resource}/{id}` - Get by ID
- `PUT /{resource}/{id}` - Update
- `DELETE /{resource}/{id}` - Delete

**Response Format**: All responses use `ApiResponse<T>`:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## Development Workflows

### Adding a New Feature

1. **Create Entity** in `entity/` - Extend BaseEntity, use `@Type(JsonBinaryType.class)` for JSONB
2. **Create DTO** in `dto/` - Simplified API representation
3. **Create Repository** in `repository/` - Extend `JpaRepository<Entity, UUID>`
4. **Create Service** in `service/` - Business logic
5. **Create Controller** in `controller/` - REST API with `@RestController`
6. **Frontend** - Add view in `front/src/views/`, Vuex module in `store/modules/`, API in `http/api.js`

### Working with AI Generation

**Access Prompt Templates**:
```java
@Autowired
private PromptConfiguration promptConfig;

String template = promptConfig.getTemplate("character-creation");
PromptConfiguration.PromptSettings settings =
    promptConfig.getSettingsOrDefault("character-creation");
```

**Use QianfanDirectService**:
```java
@Autowired
private QianfanDirectService qianfanService;

String result = qianfanService.chatCompletion(
    "Your prompt here",
    0.7,      // temperature
    2000      // maxTokens
);
```

### Database Considerations

- Use JSONB for flexible schemas: `@Type(JsonBinaryType.class)` + `columnDefinition = "jsonb"`
- Use PostgreSQL arrays: `columnDefinition = "varchar(50)[]"` or `"text[]"`
- For vectors: `columnDefinition = "float[]"`
- Complex queries: Use MyBatis mappers (planned, not yet implemented)

## Documentation Rules

**IMPORTANT - Follow these rules strictly**:

1. **Implementation Documentation** → `doc/updateByClaude/`
   - Write detailed implementation summaries for NEW features only
   - Include technical decisions, code structure, API changes
   - Format: `[Feature]_implementation_summary.md` or `Phase[X]_[module]_[description].md`

2. **Usage Documentation** → `doc/module_doc/`
   - Write user-facing guides (quickstart, test guides, usage examples)
   - Include API examples, curl commands, expected outputs
   - Format: `[module]_[type]_guide.md` (e.g., `scene_generation_usage_examples.md`)

3. **When to Write Documentation**:
   - ✅ DO: New feature development
   - ❌ DON'T: Bug fixes, error corrections, refactoring

4. **Communication Rule**:
   - After writing documentation, DO NOT report work content to user
   - User will read documentation directly
   - Only ask questions if blocked or need clarification

5. **Documentation Length**:
   - Keep concise and focused
   - Avoid redundancy
   - Focus on "what changed" and "how to use"

## Testing

```bash
# Run all tests
mvnw.cmd test

# Run specific test class
mvnw.cmd test -Dtest=CharacterServiceTest

# Skip tests during build
mvnw.cmd clean package -DskipTests
```

## Common Issues

**Lombok not working**:
- Install Lombok plugin in IDE
- Enable annotation processing in IDE settings

**Database connection failed**:
- Check PostgreSQL is running
- Verify database `storyforge` exists
- Check `DB_PASSWORD` environment variable

**AI API errors**:
- Verify `QIANFAN_API_KEY` is set correctly
- Check API key format: `bce-v3/ALTAK-xxxxx/xxxxxx`
- See logs for detailed error messages

**Frontend proxy issues**:
- Ensure backend runs on port 8080
- Check `vite.config.js` proxy configuration

## Design Decisions

**Why JSONB?** - Flexible schema evolution without migrations (Worldview structures, Character experiences)

**Why Baidu Qianfan?** - DeepSeek model provides good Chinese language generation, cost-effective

**Why Direct API calls?** - Spring AI OpenAI compatibility layer was unstable with Qianfan, direct HTTP more reliable

**Why PostgreSQL arrays?** - Native array support for lists (personality traits, fears, desires) with better query performance than JSON

## Key Files Reference

**Backend Configuration**:
- `src/main/resources/application.yml` - Main config, prompt templates
- `src/main/resources/application-dev.yml` - Dev environment config
- `src/main/resources/_sql/init.sql` - Database schema

**Frontend Configuration**:
- `front/vite.config.js` - Vite config, proxy setup
- `front/src/router/router.config.js` - Routes
- `front/src/http/axios.config.js` - Axios interceptors

**Core Services**:
- `QianfanDirectService.java` - AI API client
- `GenerationPipeline.java` - Story generation orchestration
- `CharacterConsistencyValidator.java` - Character consistency checking
- `PromptConfiguration.java` - Prompt template management

**Existing Documentation**:
- `doc/module_doc/QUICKSTART.md` - Quick start guide
- `doc/module_doc/快速测试指南.md` - Testing guide (Chinese)
- `doc/module_doc/chapter_generation_guide.md` - Chapter generation usage
- See `doc/updateByClaude/` for implementation details

---

**Last Updated**: 2025-10-30 (Project in active development)
