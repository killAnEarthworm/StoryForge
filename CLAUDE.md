# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**StoryForge** is an AI-powered story generation platform built with Spring Boot and Vue 3. The system leverages OpenAI's GPT-4 for intelligent narrative creation, using sophisticated prompt engineering, vector-based character consistency, and memory management systems to generate coherent, engaging stories.

**Current Status**: Early development stage with:
- ✅ Backend: Complete entity models, repositories, services, and REST controllers for all six core modules
- ✅ Frontend: Vue 3 framework with routing, state management, and UI scaffolding
- 🚧 AI Integration: Configuration and templates ready, implementation in progress
- 🚧 Database: Schema defined, migrations available

## Build & Development Commands

### Backend (Spring Boot)

**Windows (Command Prompt)**:
```cmd
REM Build the project
mvnw.cmd clean package

REM Run the application
mvnw.cmd spring-boot:run

REM Run with specific profile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

REM Run tests
mvnw.cmd test

REM Run specific test class
mvnw.cmd test -Dtest=StoryForgeApplicationTests

REM Skip tests during build
mvnw.cmd clean package -DskipTests
```

**Unix/Linux/Mac**:
```bash
# Use ./mvnw instead of mvnw.cmd
./mvnw clean package
./mvnw spring-boot:run
```

### Frontend (Vue 3 + Vite)

```bash
# Navigate to frontend directory
cd front

# Install dependencies (first time only)
npm install

# Run development server
npm run dev
# Access at: http://localhost:8888

# Build for production
npm run build
```

### Required Environment Variables

**Windows (Command Prompt)**:
```cmd
set DB_PASSWORD=123456
set QIANFAN_API_KEY=bce-v3/ALTAK-xxxxx/xxxxxx
set REDIS_PASSWORD=123456
set JWT_SECRET=your-secret-key-change-in-production
```

**Windows (PowerShell)**:
```powershell
$env:DB_PASSWORD="123456"
$env:QIANFAN_API_KEY="bce-v3/ALTAK-xxxxx/xxxxxx"
$env:REDIS_PASSWORD="123456"
$env:JWT_SECRET="your-secret-key-change-in-production"
```

**Unix/Linux/Mac**:
```bash
export DB_PASSWORD=123456
export QIANFAN_API_KEY=bce-v3/ALTAK-xxxxx/xxxxxx
export REDIS_PASSWORD=123456
export JWT_SECRET=your-secret-key-change-in-production
```

**Note**: 百度千帆 V2 API 只需要 API Key，不需要 Secret Key。API Key 格式为 `bce-v3/ALTAK-xxxxx/xxxxxx`。

### Database Setup

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE storyforge;
CREATE USER storyforge WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE storyforge TO storyforge;

# Initialize schema
\c storyforge
\i src/main/resources/_sql/init.sql
```

## Architecture Overview

### Technology Stack

**Backend**:
- **Framework**: Spring Boot 3.3.5
- **Java Version**: 21
- **AI Integration**: Spring AI 1.0.0-M4 with OpenAI GPT-4 Turbo Preview
- **Database**: PostgreSQL with pgvector extension for embeddings
- **Cache/Session**: Redis with Spring Session
- **Security**: Spring Security with JWT authentication (currently disabled in dev mode)
- **ORM**: JPA (Hibernate) + MyBatis (planned for complex queries)
- **Build Tool**: Maven

**Frontend**:
- **Framework**: Vue 3
- **Build Tool**: Vite 1.0
- **UI Library**: Ant Design Vue 4.0
- **State Management**: Vuex 4
- **Routing**: Vue Router 4
- **HTTP Client**: Axios 1.12

### Project Structure

```
StoryForge/
├── src/main/java/com/linyuan/storyforge/
│   ├── entity/          # JPA entities (Character, Worldview, Timeline, Scene, etc.)
│   ├── dto/             # Data Transfer Objects
│   ├── repository/      # Spring Data JPA repositories
│   ├── service/         # Business logic layer
│   ├── controller/      # REST API controllers
│   ├── config/          # Configuration classes (Security, Prompts)
│   ├── exception/       # Exception handlers
│   ├── common/          # Common utilities (ApiResponse)
│   └── enums/           # Enumerations
├── src/main/resources/
│   ├── application.yml           # Main configuration
│   ├── application-dev.yml       # Development profile
│   ├── application-prod.yml      # Production profile
│   └── _sql/                     # Database migration scripts
├── front/                        # Vue 3 frontend
│   ├── src/
│   │   ├── views/       # Page components (character, worldview, timeline, etc.)
│   │   ├── layouts/     # Layout components (MainLayout)
│   │   ├── store/       # Vuex modules
│   │   ├── router/      # Vue Router configuration
│   │   ├── http/        # Axios configuration and API definitions
│   │   ├── components/  # Reusable components
│   │   └── assets/      # Static assets
│   ├── package.json
│   └── vite.config.js
└── doc/                 # Architecture and design documents
```

### Six Core Modules

All modules follow the same layered architecture: **Entity → Repository → Service → Controller → Frontend View**

1. **Character Module** (`/api/characters`, `/character`)
   - Character creation and management with personality vectors
   - Character memory system with hierarchical types
   - Character relationships with dynamic tracking
   - AI-assisted character generation

2. **Worldview Module** (`/api/worldviews`, `/worldview`)
   - Universe laws and magic systems (JSONB storage)
   - Social structures and geography
   - Knowledge graph for entity relationships
   - Terminology dictionary

3. **Timeline Module** (`/api/timelines`, `/timeline`)
   - Event sequence management
   - Causal relationship tracking
   - Paradox detection system
   - Critical point identification

4. **Scene Module** (`/api/scenes`, `/scene`)
   - Scene creation with atmosphere and mood
   - Physical environment descriptions
   - Scene element and prop management
   - Character associations

5. **Story Generation Module** (`/api/chapters`, `/story`)
   - Multiple generation modes (chapter, short story, dialogue)
   - Quality control pipeline
   - Generation history tracking
   - Export functionality

6. **Inspiration Module** (`/api/inspiration`, `/inspiration`)
   - Creative recommendations
   - Trend analysis
   - Idea combination engine
   - Theme suggestions

## Core Data Models

### Key Entities

**Character** (`src/main/java/com/linyuan/storyforge/entity/Character.java`):
- Basic info: name, age, appearance, occupation
- Deep settings: backgroundStory, childhoodExperience, importantExperiences (JSONB)
- Personality: personalityTraits (array), personalityVector (float array for AI)
- Behavior: speechPattern, behavioralHabits, catchphrases
- Dynamic state: emotionalState (JSONB), relationships (JSONB)
- Belongs to: Project, optionally Worldview

**CharacterMemory**:
- Five-tier hierarchy: core → emotional → skill → episodic → semantic
- Accessibility decay system (Ebbinghaus forgetting curve)
- Vector embeddings for similarity search

**CharacterRelationship**:
- Many-to-many between characters
- Relationship type and intensity tracking

**Worldview**:
- JSONB fields for flexible schema (universe laws, social structures, geography, terminology)
- Knowledge graph support

**Timeline & TimelineEvent**:
- Event sequences with causal relationships
- Character state tracking across events

**StoryChapter, Scene, Dialogue**:
- Content entities with metadata
- Quality metrics tracking

**PromptTemplate**:
- Template versioning
- Effectiveness scoring
- Type enumeration for categorization

**GenerationHistory**:
- Tracks all AI generations
- Quality metrics for optimization

## Prompt Engineering System

The system uses a centralized prompt configuration in `application.yml` under `ai.prompt`:

### Template Structure

Templates are stored in `ai.prompt.templates` as multi-line YAML strings with variable placeholders:

```yaml
ai:
  prompt:
    templates:
      character-creation: |
        你是一位专业的小说角色创作助手...

        ## 输入信息
        {input}

        ## 要求
        [具体要求]

        ## 输出格式
        [JSON格式定义]
```

### Template Configuration

Each template has corresponding settings in `ai.prompt.settings`:

```yaml
ai:
  prompt:
    settings:
      character-creation:
        temperature: 0.7          # Creativity vs consistency (0.0-2.0)
        maxTokens: 2000          # Max output length
        topP: 0.95               # Nucleus sampling
        frequencyPenalty: 0.3    # Reduce repetition
        presencePenalty: 0.3     # Encourage topic diversity
        enableCoT: true          # Chain of Thought reasoning
        maxContextTokens: 3000   # Context window limit
        retryCount: 3            # Retry on failure
        timeoutSeconds: 60       # API timeout
```

### Available Templates

- `character-creation`: Generate complete character profiles
- `worldview-creation`: Build comprehensive world settings
- `chapter-generation`: Generate story chapters with context
- `scene-generation`: Create immersive scene descriptions
- `dialogue-generation`: Generate character-appropriate dialogue

### Accessing Templates in Code

Use `PromptConfiguration` bean (auto-configured from application.yml):

```java
@Autowired
private PromptConfiguration promptConfig;

// Get template content
String template = promptConfig.getTemplate("character-creation");

// Get settings
PromptConfiguration.PromptSettings settings =
    promptConfig.getSettingsOrDefault("character-creation");

// Check existence
boolean exists = promptConfig.hasTemplate("character-creation");
```

## REST API Structure

All APIs follow RESTful conventions:

**Base URL**: `http://localhost:8080/api`

**Standard Endpoints** (pattern for all modules):
- `GET    /api/{resource}` - List all
- `POST   /api/{resource}` - Create new
- `GET    /api/{resource}/{id}` - Get by ID
- `PUT    /api/{resource}/{id}` - Update
- `DELETE /api/{resource}/{id}` - Delete

**Module-Specific Endpoints**:
- Projects: `/api/projects`, `/api/projects/status/{status}`, `/api/projects/genre/{genre}`
- Characters: `/api/characters`, `/api/characters/project/{projectId}`
- Chapters: `/api/chapters`, `/api/chapters/project/{projectId}`, `/api/chapters/status/{status}`
- Worldviews: `/api/worldviews`
- Scenes: `/api/scenes`
- Timelines: `/api/timelines`
- Character Memories: `/api/character-memories`
- Character Relationships: `/api/character-relationships`
- Dialogues: `/api/dialogues`
- Generation History: `/api/generation-history`
- Prompt Templates: `/api/prompt-templates`

**Response Format**: All responses use `ApiResponse<T>` wrapper:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## Frontend Architecture

### Routing

Routes defined in `front/src/router/router.config.js`:
- `/character` - Character creation and management
- `/worldview` - World-building interface
- `/timeline` - Timeline visualization
- `/scene` - Scene composition
- `/story` - Story generation
- `/inspiration` - Creative assistance

### State Management

Vuex modules in `front/src/store/modules/`:
- `character.js` - Character state
- `worldview.js` - Worldview state
- `timeline.js` - Timeline state
- `scene.js` - Scene state
- `story.js` - Story generation state
- `inspiration.js` - Inspiration state

### API Layer

API functions defined in `front/src/http/api.js`:
- `characterApi` - Character CRUD operations
- `worldviewApi` - Worldview operations
- `timelineApi` - Timeline operations
- `sceneApi` - Scene operations
- `storyApi` - Story generation
- `inspirationApi` - Inspiration features
- `userApi` - Authentication
- `uploadApi` - File uploads

Axios instance configured in `front/src/http/axios.config.js` with interceptors for:
- Request/response transformation
- Error handling
- Authentication token injection

### Proxy Configuration

Frontend dev server proxies `/api` requests to backend:
```javascript
// vite.config.js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

## Database Schema

### PostgreSQL Extensions Required

```sql
CREATE EXTENSION IF NOT EXISTS vector;  -- pgvector for embeddings
```

### JSONB Usage

Extensive use of JSONB for flexible schemas:
- `Character.importantExperiences` - Structured life events
- `Character.emotionalState` - Dynamic emotional tracking
- `Character.relationships` - Relationship metadata
- `Worldview` - All world-building data
- `Scene` details and atmosphere
- Generation metadata

### Array Types

PostgreSQL arrays for collections:
- `Character.personalityTraits` - `varchar[]`
- `Character.fears` - `text[]`
- `Character.desires` - `text[]`
- `Character.catchphrases` - `text[]`
- `Character.behavioralHabits` - `text[]`

### Vector Types

Float arrays for AI embeddings:
- `Character.personalityVector` - `float[]` (10 dimensions planned)
- `CharacterMemory` embeddings for similarity search

## Development Workflows

### Adding a New Feature Module

1. **Create Entity** in `src/main/java/com/linyuan/storyforge/entity/`
   - Extend `BaseEntity` for ID, createdAt, updatedAt
   - Use `@Type(JsonBinaryType.class)` for JSONB fields
   - Define relationships with `@ManyToOne`, `@OneToMany`

2. **Create DTO** in `src/main/java/com/linyuan/storyforge/dto/`
   - Mirror entity structure but simplified for API
   - Use validation annotations if needed

3. **Create Repository** in `src/main/java/com/linyuan/storyforge/repository/`
   - Extend `JpaRepository<Entity, Long>`
   - Add custom query methods as needed

4. **Create Service** in `src/main/java/com/linyuan/storyforge/service/`
   - Implement business logic
   - Handle DTO ↔ Entity conversion
   - Call repositories for data access

5. **Create Controller** in `src/main/java/com/linyuan/storyforge/controller/`
   - Annotate with `@RestController` and `@RequestMapping("/api/...")`
   - Inject service via constructor
   - Return `ApiResponse<T>` for consistency

6. **Add Frontend View** in `front/src/views/[module]/Index.vue`
   - Create component with UI
   - Use Ant Design Vue components
   - Connect to Vuex store

7. **Add Vuex Module** in `front/src/store/modules/[module].js`
   - Define state, mutations, actions
   - Call API functions from `http/api.js`

8. **Add API Functions** in `front/src/http/api.js`
   - Export module-specific API object
   - Define CRUD methods using axios instance

### Modifying Prompt Templates

Templates are in `src/main/resources/application.yml` under `ai.prompt.templates`.

**To add a new template**:
1. Add template content under `ai.prompt.templates.[template-name]`
2. Add settings under `ai.prompt.settings.[template-name]`
3. Access in code via `PromptConfiguration` bean
4. Consider adding enum value in `PromptTemplateType` for type safety

**Template best practices**:
- Use clear section headers (## Input, ## Requirements, ## Output Format)
- Include variable placeholders in `{curlyBraces}`
- Specify expected output format (JSON, plain text, etc.)
- Set appropriate temperature (0.7 for balanced, 0.8+ for creative, 0.5- for consistent)
- Enable `enableCoT: true` for complex reasoning tasks

### Working with Character Entities

Characters have rich nested structures:

```java
Character character = Character.builder()
    .name("张三")
    .age(25)
    .personalityTraits(List.of("勇敢", "冲动"))
    .importantExperiences(List.of(
        Map.of("time", "童年", "event", "...", "impact", "...")
    ))
    .emotionalState(Map.of("mood", "平静", "stress", 3))
    .personalityVector(List.of(0.7f, 0.3f, ...)) // 10 dimensions
    .build();
```

**Important considerations**:
- Always set `project` relationship
- Personality vectors should be 10-dimensional (Big Five + extensions)
- JSONB fields are flexible but should follow consistent structures
- Use `characterSummary` for AI prompt context

## Testing

### Running Tests

```bash
# All tests
mvnw.cmd test

# Specific test class
mvnw.cmd test -Dtest=StoryForgeApplicationTests

# Skip tests
mvnw.cmd clean package -DskipTests
```

### Test Strategy

- **Unit Tests**: Service layer with mocked repositories
- **Integration Tests**: Full controller → service → repository → database flow
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller-only tests
- Database tests use H2 in-memory or test PostgreSQL instance

## Common Issues

### Build Errors

**Lombok not working**:
- Ensure Lombok plugin is installed in IDE
- Check annotation processor is enabled in IDE settings
- Maven plugin configuration at pom.xml:126-136 should handle compilation

**JPA/Hibernate errors**:
- Verify `ddl-auto: validate` matches actual database schema
- Check PostgreSQL extensions are installed (`vector`)
- For JSONB issues, verify `hypersistence-utils-hibernate-63` dependency

### Database Connection

**PostgreSQL connection failed**:
1. Verify PostgreSQL service is running
2. Check database `storyforge` exists
3. Verify credentials in `application-dev.yml`
4. Ensure environment variable `DB_PASSWORD` is set

**Redis connection failed**:
- If Redis not available, comment out Spring Session dependency in `pom.xml`
- Or disable Redis in application.yml

### Frontend Issues

**Vite dev server won't start**:
- Check Node.js version (16+ recommended)
- Run `npm install` in `front/` directory
- Verify port 8888 is not in use

**API calls failing**:
- Ensure backend is running on port 8080
- Check proxy configuration in `vite.config.js`
- Verify CORS is allowed (currently handled by Spring Security config)

## Design Decisions

### Why JSONB?

Allows rapid iteration on data models without migrations. Particularly useful for:
- Worldview structures (laws, geography, social systems vary widely)
- Character emotional states (dynamic, game-like attributes)
- Generation metadata (flexible tracking of AI generation params)

**Trade-off**: Less type safety, harder to query complex nested data.

### Why Dual Persistence (JPA + MyBatis)?

- **JPA**: Simple CRUD operations, entity lifecycle management
- **MyBatis** (planned): Complex queries with CTEs, vector similarity searches, JSON aggregations

Currently only JPA is implemented. MyBatis will be added for performance-critical queries.

### Why Spring AI?

- Unified abstraction over AI providers (OpenAI, Azure, etc.)
- Built-in retry logic and error handling
- Vector store integration (pgvector)
- Spring Boot auto-configuration

### Why Monorepo Structure?

Frontend and backend in same repository for:
- Easier coordination during rapid development
- Shared documentation and design artifacts
- Single source of truth for API contracts

**Trade-off**: Larger repo size, need to manage two build systems.

## References

**Documentation** (in `doc/`):
- `API_STRUCTURE.md` - Detailed API specifications
- `enhanced_feature_modules.md` - Feature module design
- `spring_ai_prompt_engineering.md` - AI integration patterns
- `JDK21_UPGRADE_GUIDE.md` - Migration notes

**Configuration Files**:
- `pom.xml` - Maven dependencies and build config
- `src/main/resources/application.yml` - Main app config
- `front/package.json` - NPM dependencies
- `front/vite.config.js` - Vite build config

**Key Classes**:
- `PromptConfiguration.java:19` - Prompt template management
- `SecurityConfig.java` - Security configuration (dev mode: permitAll)
- `GlobalExceptionHandler.java` - Centralized error handling
- `ApiResponse.java` - Standardized response wrapper
