# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**StoryForge** is an AI-powered story generation platform built with Spring Boot 3.5.6 and Spring AI 1.0.3. The system leverages GPT-4 for intelligent narrative creation, using sophisticated prompt engineering, vector-based character consistency, and memory management systems to generate coherent, engaging stories.

**Current Status**: Early MVP stage - only bootstrap application exists, but comprehensive architecture design documents and database schemas are defined.

## Build & Development Commands

### Maven Commands
```bash
# Build the project
mvnw clean package

# Run the application
mvnw spring-boot:run

# Run with specific profile (dev/prod)
mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvnw test

# Run specific test class
mvnw test -Dtest=StoryForgeApplicationTests

# Skip tests during build
mvnw clean package -DskipTests
```

### Required Environment Variables
```bash
# Database
DB_PASSWORD=123456

# OpenAI API
OPENAI_API_KEY=your-openai-key

# Redis (optional, defaults to empty)
REDIS_PASSWORD=123456

# JWT Secret (required for production)
JWT_SECRET=your-secret-key-change-in-production
```

## Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.5.6 with Spring WebFlux (reactive programming)
- **AI Integration**: Spring AI 1.0.3 with OpenAI GPT-4 Turbo Preview
- **Database**: PostgreSQL with pgvector extension for embeddings
- **Cache/Session**: Redis with Spring Session
- **Security**: Spring Security with JWT authentication
- **ORM**: JPA + MyBatis (dual persistence approach)
- **Build Tool**: Maven
- **Java Version**: 25

### Planned Module Structure

The system is designed with six major feature modules:

1. **Character Module** - AI-assisted character generation with personality vectors, memory management, and consistency validation
2. **Worldview Module** - World-building with knowledge graphs, rule systems, and consistency engines
3. **Timeline Module** - Event management with causality tracking, paradox detection, and branching scenarios
4. **Scene Module** - Layered scene generation with sensory details, atmosphere, and dynamic transitions
5. **Story Generation Module** - Multi-mode generation (chapter-by-chapter, dialogue-driven, interactive, collaborative) with quality control
6. **Inspiration Module** - AI creative assistance with trend analysis, idea combination, and recommendation engines

### Key Architectural Patterns

**Layered Architecture**: Controller → Service → Repository → Database

**AI-First Design**:
- Every module integrates GPT-4 for intelligent generation
- Dynamic context building for consistency
- Prompt templates with versioning and A/B testing
- Chain-of-Thought (CoT) injection for better reasoning

**Consistency Assurance**:
- 10-dimensional personality vectors (Big Five + extensions)
- Character memory system with Ebbinghaus forgetting curves
- Behavior anchors for validation
- Worldview rule constraints

**Quality Control Pipeline**:
- Multi-dimensional evaluation (consistency, narrative quality, character performance, pacing, style)
- Automatic repair system for detected issues
- Regeneration with feedback loops

## Core Data Models

### Character System
- **Character**: Basic info + personality vector + behavior patterns
- **CharacterMemory**: Hierarchical memory types (core → emotional → skill → episodic → semantic) with accessibility decay
- **CharacterRelationship**: Many-to-many with dynamic tension tracking

### Worldview System
- **Worldview**: JSONB fields for universe laws, social structures, geography, terminology
- **KnowledgeGraph**: Entity-relationship network with rule constraints

### Timeline System
- **Timeline**: Event sequences with causal relationships
- **TimelineEvent**: Individual events with impact tracking
- **Paradox Detection**: Validates character state continuity across events

### Generation System
- **PromptTemplate**: Versioned templates with effectiveness scoring
- **GenerationHistory**: Tracks quality metrics for ML optimization
- **StoryChapter/Scene/Dialogue**: Content entities with metadata

## Prompt Engineering Strategy

The system uses a sophisticated prompt engineering approach:

1. **Template Management**: YAML-based templates with variable substitution
2. **Context Optimization**: Token counting, truncation, and relevance filtering
3. **Multi-Stage Generation**: Outline → Scene → Dialogue → Refinement
4. **Dynamic Context Injection**:
   - Character personalities from vectors
   - Relevant memories via similarity search
   - Worldview constraints and rules
   - Relationship dynamics

### Memory Retrieval Algorithm
Multi-dimensional relevance scoring:
- Scene relevance (30%)
- Emotional resonance (20%)
- Time decay factor (15%)
- Keyword matching (15%)
- Access frequency (10%)

## Database Considerations

### PostgreSQL Extensions Required
- **pgvector**: For character personality and memory vectors

### Key Indexes (Planned)
- Timeline: `(character_id, timestamp)` for performance
- Character Memory: GIN index on keywords for full-text search
- Vector similarity indexes for pgvector operations

### JSONB Usage
Extensive use of JSONB for flexible schemas:
- Worldview rules and structures
- Character emotional states
- Scene details and atmosphere
- Generation metadata

## AI Integration Notes

### OpenAI Configuration
- **Model**: gpt-4-turbo-preview
- **Base Temperature**: 0.7 (configurable per template)
- **Context Window Management**: Max 3000 tokens per context
- **Rate Limiting**: Implement retry logic with exponential backoff

### Prompt Template Structure
```yaml
ai:
  prompt:
    templates:
      character-creation: |
        [System instruction]

        ## Input
        {variables}

        ## Requirements
        [Specific constraints]

        ## Output Format
        [Structured format]

    settings:
      character-creation:
        temperature: 0.7
        maxTokens: 2000
        topP: 0.95
```

### Generation Pipeline Pattern
All major generation follows this pattern:
1. Prepare context from database
2. Build prompt with template + variables
3. Call AI with retry logic
4. Parse and validate response
5. Quality check against consistency rules
6. Save with generation metadata
7. Update related entity states

## Development Guidelines

### When Adding New Features

**Character-Related Features**:
- Always validate against personality vectors
- Update memory accessibility on character interactions
- Check behavior anchor constraints
- Maintain relationship tension tracking

**Story Generation Features**:
- Use the `GenerationPipeline` pattern
- Implement quality checks before saving
- Track generation history for optimization
- Consider impact on timeline causality

**Worldview Features**:
- Validate against existing rule constraints
- Update knowledge graph relationships
- Ensure terminology consistency

### Performance Considerations

1. **Caching Strategy**: Frequently used prompts and contexts should be cached (Redis)
2. **Batch Operations**: Character memory retrieval uses single SQL query with CTEs
3. **Vector Operations**: Use pgvector indexes for similarity searches
4. **Async Generation**: Long-running AI calls should use reactive patterns (WebFlux)

### Security Notes
- JWT tokens expire after 24 hours
- All AI API keys must be environment variables
- Session data stored in Redis
- Validate user ownership before generation operations

## Common Workflows

### Creating a New Character
1. Call `CharacterGenerationService.generateCharacter()`
2. Service stages: BasicInfo → DeepSettings → PersonalityVector → BehaviorPattern → InitialMemories
3. Validate consistency with `CharacterConsistencySystem`
4. Save to database with generated ID

### Generating a Story Chapter
1. `ChapterGenerationPipeline.generateChapter()`
2. Stages: Context prep → Outline → Scenes → Dialogue → Quality check
3. Each scene updates GenerationContext
4. Quality score < 0.7 triggers regeneration
5. Update character states and memories after generation

### Adding a Timeline Event
1. Create event via `TimelineManagementService`
2. System runs topological sort for causality
3. Detects paradoxes (e.g., dead character acting)
4. Identifies critical points using graph algorithms
5. Updates all affected character states

## Testing Strategy

### Unit Tests
- Service layer logic with mocked repositories
- Prompt template rendering and variable substitution
- Vector similarity calculations
- Memory relevance scoring algorithms

### Integration Tests
- End-to-end generation pipelines
- Database constraint validation
- AI service integration (use test API keys)
- Quality control system evaluation

### Test Data
- Use `@Sql` annotations for database setup
- Character fixtures with known personality vectors
- Worldview templates for consistent testing

## Known Design Decisions

1. **Dual Persistence (JPA + MyBatis)**: JPA for standard CRUD, MyBatis for complex queries with CTEs and vector operations
2. **JSONB for Flexibility**: Allows rapid iteration on data models without migrations
3. **Reactive Stack**: WebFlux supports long-running AI operations without blocking
4. **Memory Hierarchy**: Five-tier system balances realism with computational efficiency
5. **Vector Dimensions**: 10 dimensions chosen as optimal balance between expressiveness and performance

## Future Expansion Points

Based on design documents:
- Interactive branching story support (timeline merge logic exists)
- Multi-perspective narrative generation
- Emotion-driven content generation with arc tracking
- Real-time collaborative writing features
- Advanced analytics dashboard for generation quality trends
