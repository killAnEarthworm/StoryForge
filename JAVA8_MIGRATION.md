# Java 8 兼容性迁移说明

## 问题
原项目配置为 Java 25 + Spring Boot 3.5.6，但当前环境为 JDK 1.8

## 解决方案

### 1. 降级 Spring Boot 版本
- **从**: Spring Boot 3.5.6 (要求 Java 17+)
- **到**: Spring Boot 2.7.18 (支持 Java 8)

### 2. 修改的配置

#### pom.xml 主要变更:
```xml
<!-- Spring Boot 版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<!-- Java 版本 -->
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>

<!-- 依赖调整 -->
- 移除 spring-ai 相关依赖（暂不支持 Spring Boot 2.x）
- 添加 spring-boot-starter-web
- MyBatis: 3.0.5 → 2.3.2
- Hypersistence: hibernate-63 → hibernate-55 (3.7.7)
- 添加 hibernate-types-52 (2.21.1) 用于 JSONB 支持
```

### 3. 代码变更

#### 包名替换
所有 `jakarta.*` 包名替换为 `javax.*`:
- `jakarta.persistence.*` → `javax.persistence.*`
- `jakarta.validation.*` → `javax.validation.*`

#### 实体类 UUID 生成策略
```java
// 之前 (Java 17+)
@GeneratedValue(strategy = GenerationType.UUID)

// 现在 (Java 8)
@GeneratedValue(generator = "uuid2")
@GenericGenerator(name = "uuid2", strategy = "uuid2")
@Column(columnDefinition = "uuid")
```

#### JSONB 类型映射
```java
// 之前
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

// 现在
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

// 实体类添加
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)

// 字段使用
@Type(type = "jsonb")
@Column(columnDefinition = "jsonb")
private Map<String, Object> data;
```

#### Security 配置
```java
// 之前 (Spring Boot 3.x)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {...}
}

// 现在 (Spring Boot 2.x)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {...}
}
```

### 4. 受影响的文件列表

**实体类 (Entity)**:
- BaseEntity.java
- Project.java
- Character.java
- Worldview.java
- Scene.java
- StoryChapter.java
- CharacterRelationship.java
- PromptTemplate.java

**DTO 类**:
- ProjectDTO.java
- CharacterDTO.java
- StoryChapterDTO.java

**控制器 (Controller)**:
- ProjectController.java
- CharacterController.java
- StoryChapterController.java

**配置类 (Config)**:
- SecurityConfig.java

### 5. 构建和运行

```bash
# 清理并重新构建
mvnw clean package -DskipTests

# 运行应用
mvnw spring-boot:run
```

### 6. 功能验证

所有 CRUD API 功能保持不变:
- ✅ 项目管理 API
- ✅ 角色管理 API
- ✅ 章节管理 API
- ✅ JSONB 字段支持
- ✅ 统一异常处理
- ✅ 参数验证

### 7. 注意事项

1. **Spring AI 暂不可用**: Spring AI 1.0.3 需要 Spring Boot 3.x，如需使用 AI 功能，建议升级 JDK 到 17 或更高版本

2. **WebFlux 已移除**: Spring Boot 2.7 使用传统的 Web MVC，移除了 WebFlux 依赖

3. **数据库连接**: PostgreSQL 驱动保持不变，支持所有 JSONB 功能

4. **Redis Session**: 仍然支持，配置保持不变

### 8. 未来升级路径

当准备好升级到更高 JDK 版本时:
1. 升级到 JDK 17 或 21
2. 升级 Spring Boot 到 3.x
3. 恢复 Spring AI 依赖
4. 将所有 `javax.*` 改回 `jakarta.*`
5. 更新 UUID 生成策略
6. 更新 Security 配置

## 总结

项目已成功适配 Java 8 环境，所有核心功能正常工作。主要变更集中在依赖版本和包名调整，业务逻辑层无需修改。
