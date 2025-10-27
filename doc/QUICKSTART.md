# StoryForge 快速启动指南

## 环境要求

✅ **已验证可用**:
- JDK 21
- PostgreSQL 数据库
- Maven (通过 mvnw 包装器)

## 启动步骤

### 1. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE storyforge;

-- 创建用户（可选）
CREATE USER storyforge WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE storyforge TO storyforge;

-- 连接到数据库并执行初始化脚本
\c storyforge
\i src/main/resources/_sql/init.sql
```

### 2. 配置环境变量

在运行前设置以下环境变量（或修改 `application.yml`）：

**Windows (CMD)**:
```cmd
set DB_PASSWORD=123456
set REDIS_PASSWORD=
set JWT_SECRET=your-secret-key
```

**Windows (PowerShell)**:
```powershell
$env:DB_PASSWORD="123456"
$env:REDIS_PASSWORD=""
$env:JWT_SECRET="your-secret-key"
```

### 3. 构建项目

```bash
# 清理并构建
mvnw.cmd clean package -DskipTests

# 查看构建结果
# JAR 文件位置: target/StoryForge-0.0.1-SNAPSHOT.jar
```

### 4. 运行应用

**方式一：使用 Maven 运行**
```bash
mvnw.cmd spring-boot:run
```

**方式二：直接运行 JAR**
```bash
java -jar target/StoryForge-0.0.1-SNAPSHOT.jar
```

### 5. 验证运行

应用启动后，访问：
- **基础 URL**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health (如果启用了 actuator)

## 测试 API

### 创建项目

```bash
curl -X POST http://localhost:8080/api/projects ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"我的第一个故事\",\"genre\":\"科幻\",\"status\":\"draft\"}"
```

### 获取所有项目

```bash
curl http://localhost:8080/api/projects
```

### 创建角色

```bash
curl -X POST http://localhost:8080/api/characters ^
  -H "Content-Type: application/json" ^
  -d "{\"projectId\":\"<项目ID>\",\"name\":\"张三\",\"age\":25,\"occupation\":\"宇航员\"}"
```

## 可用的 API 端点

### 项目管理
- `GET    /api/projects` - 获取所有项目
- `POST   /api/projects` - 创建项目
- `GET    /api/projects/{id}` - 获取指定项目
- `PUT    /api/projects/{id}` - 更新项目
- `DELETE /api/projects/{id}` - 删除项目
- `GET    /api/projects/status/{status}` - 按状态查询
- `GET    /api/projects/genre/{genre}` - 按类型查询
- `GET    /api/projects/search?name=xxx` - 搜索项目

### 角色管理
- `GET    /api/characters` - 获取所有角色
- `POST   /api/characters` - 创建角色
- `GET    /api/characters/{id}` - 获取指定角色
- `PUT    /api/characters/{id}` - 更新角色
- `DELETE /api/characters/{id}` - 删除角色
- `GET    /api/characters/project/{projectId}` - 获取项目的所有角色

### 章节管理
- `GET    /api/chapters` - 获取所有章节
- `POST   /api/chapters` - 创建章节
- `GET    /api/chapters/{id}` - 获取指定章节
- `PUT    /api/chapters/{id}` - 更新章节
- `DELETE /api/chapters/{id}` - 删除章节
- `GET    /api/chapters/project/{projectId}` - 获取项目的所有章节
- `GET    /api/chapters/status/{status}` - 按状态查询章节

## 常见问题

### 编译错误

如果遇到编译错误，确保：
1. 使用的是 JDK 21
2. Maven 版本 >= 3.6.0
3. 已执行 `mvnw.cmd clean` 清理缓存

### 数据库连接错误

检查：
1. PostgreSQL 服务是否启动
2. 数据库 `storyforge` 是否已创建
3. 用户名和密码是否正确
4. `application.yml` 中的连接配置

### Redis 连接错误

如果没有 Redis，可以临时注释掉 `pom.xml` 中的 Redis 依赖：
```xml
<!-- 注释掉这个依赖 -->
<!--
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
-->
```

## 开发提示

### 热重载

使用 Spring Boot DevTools 实现热重载（已在依赖中）：
```bash
mvnw.cmd spring-boot:run
```

### 查看日志

日志级别在 `application.yml` 中配置：
```yaml
logging:
  level:
    com.linyuan.storyforge: DEBUG
```

### 数据库迁移

修改数据库结构后：
1. 更新 `src/main/resources/_sql/init.sql`
2. 在开发环境可以使用 `ddl-auto: update`（生产环境慎用）

## 下一步

1. ✅ 基础 CRUD 已完成
2. 🚧 添加更多实体的 API（Worldview, Scene, Timeline）
3. 🚧 实现 AI 生成功能（需要升级到 Java 17+）
4. 🚧 添加用户认证
5. 🚧 添加单元测试

## 技术栈

- **框架**: Spring Boot 2.7.18
- **数据库**: PostgreSQL + JPA
- **ORM**: Hibernate 5.6.x
- **构建工具**: Maven
- **Java 版本**: 21
- **安全**: Spring Security (开发环境已禁用)

---

**成功构建日期**: 2025-10-17
**构建状态**: ✅ BUILD SUCCESS
